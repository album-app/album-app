package mdc.ida.hips.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import mdc.ida.hips.HIPSClient;
import mdc.ida.hips.io.CollectionReader;
import mdc.ida.hips.model.HIPSCollectionUpdatedEvent;
import mdc.ida.hips.model.HIPSInstallation;
import mdc.ida.hips.model.HIPSLaunchRequestEvent;
import mdc.ida.hips.model.HIPSServerRunningEvent;
import mdc.ida.hips.model.HIPSolution;
import mdc.ida.hips.model.LocalHIPSInstallation;
import mdc.ida.hips.model.RemoteHIPSInstallation;
import mdc.ida.hips.model.SolutionArgument;
import mdc.ida.hips.service.conda.CondaEnvironmentMissingEvent;
import mdc.ida.hips.service.conda.CondaService;
import mdc.ida.hips.utils.StreamGobbler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.scijava.Disposable;
import org.scijava.app.StatusService;
import org.scijava.command.DynamicCommandInfo;
import org.scijava.command.Inputs;
import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.module.DefaultMutableModuleItem;
import org.scijava.module.ModuleItem;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.prefs.PrefService;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.scijava.ui.UIService;
import org.scijava.widget.FileWidget;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@Plugin(type = Service.class)
public class DefaultHIPSServerService extends AbstractService implements HIPSServerService, Disposable {

	private final String HIPS_ENVIRONMENT_NAME = "hips";
	@Parameter
	private LogService log;

	@Parameter
	private UIService ui;

	@Parameter
	private EventService eventService;

	@Parameter
	private StatusService statusService;

	@Parameter
	private LogService logService;

	@Parameter
	private PrefService prefService;

	@Parameter
	private CondaService condaService;

	private HIPSClient client;

	private static final String HIPS_PREF_LOCAL_PORT = "hips.local.port";
	private static final String HIPS_PREF_LOCAL_DEFAULT_CATALOG = "hips.local.default_catalog";
	private static final String HIPS_DEFAULT_CATALOG_URL = "https://gitlab.com/ida-mdc/capture-knowledge";

	private Thread serverThread;

	@Override
	public LocalHIPSInstallation loadLocalInstallation() {
		int port = prefService.getInt(DefaultHIPSServerService.class, HIPS_PREF_LOCAL_PORT, 8080);
		String catalog = prefService.get(DefaultHIPSServerService.class, HIPS_PREF_LOCAL_DEFAULT_CATALOG, HIPS_DEFAULT_CATALOG_URL);
		LocalHIPSInstallation installation = new LocalHIPSInstallation(port, catalog);
		File defaultCondaPath = condaService.getDefaultCondaPath();
		if(defaultCondaPath != null) installation.setCondaPath(defaultCondaPath);
		return installation;
	}

	@Override
	public RemoteHIPSInstallation loadRemoteInstallation(String url, int port) {
		return new RemoteHIPSInstallation(url, port);
	}

	@Override
	public void updateIndex(Consumer<HIPSCollectionUpdatedEvent> callback) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		String actionName = "index";
		JsonNode response = client.send(client.createHIPSRequest(mapper, actionName));
		if(response == null) return;
		statusService.showStatus("Updated HIPS collection.");
		HIPSCollectionUpdatedEvent event = new HIPSCollectionUpdatedEvent(CollectionReader.readCollection(response));
		callback.accept(event);
		eventService.publish(event);
	}

	@Override
	public boolean checkIfRunning(LocalHIPSInstallation installation, Runnable callback) {
		if(client == null) return false;
		ObjectMapper mapper = new ObjectMapper();
		String actionName = "";
		try {
			JsonNode response = client.send(client.createHIPSRequest(mapper, actionName));
			boolean running = response != null;
			if(running) {
				callback.run();
				eventService.publish(new HIPSServerRunningEvent());
			}
			return running;
		} catch (HIPSClient.ServerNotAvailableException | IOException e) {
			return false;
		}
	}

	@Override
	public void runWithChecks(LocalHIPSInstallation installation) throws IOException, InterruptedException {
		if(condaService.checkIfCondaInstalled(installation.getCondaPath())
				&& checkIfHIPSEnvironmentExists(installation)) {
			runAsynchronously(installation);
		} else {
			eventService.publish(new CondaEnvironmentMissingEvent());
		}
	}

	@Override
	public boolean checkIfHIPSEnvironmentExists(LocalHIPSInstallation installation) {
		return condaService.checkIfEnvironmentExists(installation.getCondaPath(), HIPS_ENVIRONMENT_NAME);
	}

	@Override
	public void runAsynchronously(LocalHIPSInstallation installation, Runnable callback) {
		String condaExecutable = condaService.getCondaExecutable(installation.getCondaPath());
		String environmentPath = condaService.getEnvironmentPath(installation.getCondaPath());
		if(new File(condaExecutable).exists()) {
			serverThread = createServerThread(installation, installation.getCondaPath(), environmentPath);
			serverThread.start();
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					initClient(installation);
					if(checkIfRunning(installation, callback)) {
						timer.cancel();
					}
				}
			}, 0, 1000);
		} else {
			eventService.publish(new CondaEnvironmentMissingEvent());
		}
	}

	@Override
	public File getEnvironmentFile() throws IOException {
		File tmpFile = Files.createTempFile("hips", ".yml").toFile();
		FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("hips.yml"), tmpFile);
		return tmpFile;
	}

	@Override
	public void addCatalog(LocalHIPSInstallation installation, String urlOrPath) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		String actionName = "catalogs/add";
		ObjectNode solutionArgs = mapper.createObjectNode();
		solutionArgs.put("url", urlOrPath);
		JsonNode response = client.send(client.createHIPSRequest(mapper, actionName));
		if(response == null) return;
		statusService.showStatus("Added catalog " + urlOrPath + " to HIPS collection.");
		HIPSCollectionUpdatedEvent event = new HIPSCollectionUpdatedEvent(CollectionReader.readCollection(response));
		eventService.publish(event);
	}

	private File downloadTmpFile(String url, String name) throws IOException {
		Path dir = Files.createTempDirectory("hips-installer");
		File scriptFile = new File(dir.toFile(), name);
		log().info("Downloading " + url + " to " + scriptFile.getAbsolutePath() + "..");
		FileUtils.copyURLToFile(new URL(url), scriptFile, 10000, 1000000);
		return scriptFile;
	}

	private Thread createServerThread(LocalHIPSInstallation installation, File condaPath, String environmentPath) {
		return new Thread(() -> {
			try {
				tryToStartServer(installation, condaPath, environmentPath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	private void tryToStartServer(LocalHIPSInstallation installation, File condaPath, String environmentPath) throws IOException {
		log.info("Trying to start the HIPS server locally on port " + installation.getPort() + "..");
		String[] command;
		String commandInCondaEnv = "run --no-capture-output --prefix " + environmentPath + " "
				+ new File(environmentPath, "python").getAbsolutePath()
				+ " -m hips server --port " + installation.getPort();
		if(SystemUtils.IS_OS_WINDOWS) {
			command = condaService.createCondaCommandWindows(condaPath, commandInCondaEnv);
		} else {
			command = condaService.createCondaCommandLinuxMac(condaPath, commandInCondaEnv);
		}
		log().info(Arrays.toString(command));
		ProcessBuilder builder = new ProcessBuilder(command);
		Map<String, String> env = builder.environment();
		env.put("HIPS_DEFAULT_CATALOG", installation.getDefaultCatalog());
		final AtomicReference<Boolean> portInUse = new AtomicReference<>(false);
		try {
			Process process = builder.start();
			String addressInUseError = "[Errno 98] Address already in use";
			StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), log()) {
				@Override
				public void handleLog(String line) {
					if(line.contains(addressInUseError)) {
//						process.destroy();
						portInUse.set(true);
						this.streamClosed = true;
					} else {
						super.handleLog(line);
					}
				}
			};
			StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), log());
			errorGobbler.start();
			outputGobbler.start();
			process.waitFor();
		} catch (IOException | IllegalStateException | InterruptedException e) {
			e.printStackTrace();
		}
		if(portInUse.get()) {
			installation.setPort(installation.getPort()+1);
			log.warn("Port " + (installation.getPort()-1) + " is already in use, "
					+ " trying port " + installation.getPort() + " next.");
			tryToStartServer(installation, condaPath, environmentPath);
		}
	}

	@Override
	public void launchSolutionAsTutorial(HIPSolution solution) {
		launch(solution, true);
	}

	@Override
	public void launchSolution(HIPSolution solution) {
		launch(solution, false);
	}

	@EventHandler
	private void launchSolution(HIPSLaunchRequestEvent event) {
		new Thread(() -> {
			if(event.launchAsTutorial()) {
				launchSolutionAsTutorial(event.getSolution());
			} else {
				launchSolution(event.getSolution());
			}
		}).start();
	}

	@Override
	public void dispose() {
		if(client != null) {
			client.dispose();
		}
		if(serverThread != null && serverThread.isAlive()) {
			serverThread.stop();
		}
	}

	private void launch(HIPSolution solution, boolean asTutorial) {
		ObjectMapper mapper = new ObjectMapper();
		String actionName = asTutorial? "tutorial" : "run";
		String path = solution.getCatalog() + "/" + solution.getGroup() + "/" + solution.getName() + "/" + solution.getVersion() + "/" + actionName;
		ObjectNode solutionArgs = mapper.createObjectNode();

		if(solution.getArgs().size() > 0) {
			System.out.println("Harvesting inputs for " + solution.getGroup() + ":" + solution.getName() + ":" + solution.getVersion() + "...");
			Inputs inputs = new Inputs(getContext());
			for (SolutionArgument arg : solution.getArgs()) {
				inputs.addInput(createModuleItem(inputs.getInfo(), arg));
			}
			inputs.harvest();
			for (SolutionArgument arg : solution.getArgs()) {
				Object input = inputs.getInput(arg.getName());
				solutionArgs.put(arg.getName(), input.toString());
			}
		}
		System.out.println("launching " + solution.getGroup() + ":" + solution.getName() + ":" + solution.getVersion() + "...");
		try {
			JsonNode response = client.send(client.createHIPSRequest(mapper, path, solutionArgs));
			//TODO handle response if there is one
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private ModuleItem<?> createModuleItem(DynamicCommandInfo info, SolutionArgument arg) {
		if(arg.getType().equals("file")) {
			ModuleItem<File> item = new DefaultMutableModuleItem<>(info, arg.getName(), File.class);
			item.setDescription(arg.getDescription());
			return item;
		}
		if(arg.getType().equals("directory")) {
			ModuleItem<File> item = new DefaultMutableModuleItem<>(info, arg.getName(), File.class);
			item.setDescription(arg.getDescription());
			item.set("style", FileWidget.DIRECTORY_STYLE);
			return item;
		}
		if(arg.getType().equals("string")) {
			ModuleItem<String> item = new DefaultMutableModuleItem<>(info, arg.getName(), String.class);
			item.setDescription(arg.getDescription());
			return item;
		}
		return null;
	}

	private void initClient(HIPSInstallation installation) {
		this.client = new HIPSClient(installation.getHost(), installation.getPort());
		context().inject(client);
	}

}
