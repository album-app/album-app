package mdc.ida.hips.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import mdc.ida.hips.HIPSClient;
import mdc.ida.hips.io.CollectionReader;
import mdc.ida.hips.model.HIPSCollectionUpdatedEvent;
import mdc.ida.hips.model.HIPSInstallation;
import mdc.ida.hips.model.HIPSLaunchRequestEvent;
import mdc.ida.hips.model.HIPSServerThreadDoneEvent;
import mdc.ida.hips.model.HIPSolution;
import mdc.ida.hips.model.LocalHIPSInstallation;
import mdc.ida.hips.model.RemoteHIPSInstallation;
import mdc.ida.hips.model.ServerProperties;
import mdc.ida.hips.model.SolutionArgument;
import mdc.ida.hips.service.conda.CondaExecutableMissingEvent;
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
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
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

	private final Map<HIPSInstallation, HIPSClient> clients = new HashMap<>();

	private static final String HIPS_PREF_LOCAL_PORT = "hips.local.port";
	private static final String HIPS_PREF_LOCAL_DEFAULT_CATALOG = "hips.local.default_catalog";
	private static final String HIPS_DEFAULT_CATALOG_URL = "https://gitlab.com/ida-mdc/capture-knowledge";

	private Thread serverThread;
	final AtomicReference<Boolean> serverException = new AtomicReference<>(false);
	private static final String environmentName = "hips";

	@Override
	public LocalHIPSInstallation loadLocalInstallation() {
		int port = prefService.getInt(DefaultHIPSServerService.class, HIPS_PREF_LOCAL_PORT, 8080);
		String catalog = prefService.get(DefaultHIPSServerService.class, HIPS_PREF_LOCAL_DEFAULT_CATALOG, HIPS_DEFAULT_CATALOG_URL);
		log.info("Local HIPS installation: default port: " + port);
		log.info("Local HIPS installation: default catalog URL: " + catalog);
		LocalHIPSInstallation installation = new LocalHIPSInstallation(port, catalog);
		File defaultCondaPath = condaService.getDefaultCondaPath();
		if(defaultCondaPath != null) installation.setCondaPath(defaultCondaPath);
		else {
			log.info("No conda path associated with this installation, please run the initial setup in the UI.");
		}
		return installation;
	}

	@Override
	public RemoteHIPSInstallation loadRemoteInstallation(String url, int port) {
		return new RemoteHIPSInstallation(url, port);
	}

	@Override
	public void updateIndex(LocalHIPSInstallation installation, Consumer<HIPSCollectionUpdatedEvent> callback) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		String actionName = "index";
		HIPSClient client = getClient(installation);
		JsonNode response = client.send(client.createHIPSRequest(mapper, actionName));
		if(response == null) return;
		statusService.showStatus("Updated HIPS collection.");
		HIPSCollectionUpdatedEvent event = new HIPSCollectionUpdatedEvent(CollectionReader.readCollection(installation, response));
		callback.accept(event);
		eventService.publish(event);
	}

	private HIPSClient getClient(HIPSInstallation installation) {
//		return clients.getOrDefault(installation, new HIPSClient(installation.getHost(), installation.getPort()));
		return clients.get(installation);
	}

	@Override
	public boolean checkIfRunning(LocalHIPSInstallation installation, Runnable callback) {
		HIPSClient client = getClient(installation);
		if(client == null) {
			installation.setServerRunning(false);
			return false;
		}
		ObjectMapper mapper = new ObjectMapper();
		String actionName = "";
		try {
			JsonNode response = client.send(client.createHIPSRequest(mapper, actionName));
			boolean running = response != null;
			if(running) {
				callback.run();
				eventService.publish(new HIPSServerThreadDoneEvent(true));
			}
			installation.setServerRunning(true);
			return running;
		} catch (HIPSClient.ServerNotAvailableException | IOException e) {
			installation.setServerRunning(false);
			return false;
		}
	}

	@Override
	public void runWithChecks(LocalHIPSInstallation installation) {
		if(checkIfCondaInstalled(installation)
				&& checkIfHIPSEnvironmentExists(installation)) {
			runAsynchronously(installation);
		}
	}

	@Override
	public boolean checkIfHIPSEnvironmentExists(LocalHIPSInstallation installation) {
		boolean exists = condaService.checkIfEnvironmentExists(installation.getCondaPath(), HIPS_ENVIRONMENT_NAME);
		installation.setHasHipsEnvironment(exists);
		return exists;
	}

	@Override
	public void runAsynchronously(LocalHIPSInstallation installation, Runnable callback) {
		String condaExecutable = condaService.getCondaExecutable(installation.getCondaPath());
		String environmentPath = condaService.getEnvironmentPath(installation.getCondaPath(), environmentName);
		if(new File(condaExecutable).exists()) {
			serverThread = new ServerThread(installation, installation.getCondaPath(), environmentPath);
			serverThread.start();
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					initClient(installation);
					if(checkIfRunning(installation, callback) || serverException.get()) {
						timer.cancel();
					}
				}
			}, 0, 1000);
		} else {
			eventService.publish(new CondaExecutableMissingEvent());
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
		HIPSClient client = getClient(installation);
		JsonNode response = client.send(client.createHIPSRequest(mapper, actionName));
		if(response == null) return;
		statusService.showStatus("Added catalog " + urlOrPath + " to HIPS collection.");
		HIPSCollectionUpdatedEvent event = new HIPSCollectionUpdatedEvent(CollectionReader.readCollection(installation, response));
		eventService.publish(event);
	}

	@Override
	public boolean checkIfCondaInstalled(LocalHIPSInstallation installation) {
		File condaPath = installation.getCondaPath();
		logService.info("Checking for conda installation: " + condaPath);
		boolean installed = condaService.checkIfCondaInstalled(condaPath);
		if(installed) logService.info("Conda installed to " + condaPath);
		installation.setCondaInstalled(installed);
		return installed;
	}

	@Override
	public void installConda(LocalHIPSInstallation installation) throws IOException {
		condaService.installConda(installation.getCondaPath());
	}

	@Override
	public void createHIPSEnvironment(LocalHIPSInstallation installation) throws IOException, InterruptedException {
		condaService.createEnvironment(installation.getCondaPath(), getEnvironmentFile());
	}

	@Override
	public ServerProperties getServerProperties(LocalHIPSInstallation installation) throws IOException {
		HIPSClient client = getClient(installation);
		if(client == null) return null;
		if(!installation.isServerRunning()) return null;
		ObjectMapper mapper = new ObjectMapper();
		String actionName = "config";
		JsonNode response = client.send(client.createHIPSRequest(mapper, actionName));
		if(response == null) return null;
		ServerProperties properties = new ServerProperties();
		Iterator<Map.Entry<String, JsonNode>> props = response.fields();
		while (props.hasNext()) {
			Map.Entry<String, JsonNode> prop = props.next();
			properties.put(prop.getKey(), prop.getValue().asText());
		}
		return properties;
	}

	@Override
	public String getHIPSEnvironmentPath(LocalHIPSInstallation installation) {
		return condaService.getEnvironmentPath(installation.getCondaPath(), environmentName);
	}

	@Override
	public void launchSolutionAsTutorial(HIPSInstallation installation, HIPSolution solution) {
		launch(installation, solution, true);
	}

	@Override
	public void launchSolution(HIPSInstallation installation, HIPSolution solution) {
		launch(installation, solution, false);
	}

	@EventHandler
	private void launchSolution(HIPSLaunchRequestEvent event) {
		new Thread(() -> {
			if(event.launchAsTutorial()) {
				launchSolutionAsTutorial(event.getInstallation(), event.getSolution());
			} else {
				launchSolution(event.getInstallation(), event.getSolution());
			}
		}).start();
	}

	@Override
	public void dispose() {
		clients.forEach((hipsInstallation, hipsClient) -> {
			shutdownServer(hipsInstallation);
		});
		if(serverThread != null && serverThread.isAlive()) {
			serverThread.stop();
		}
	}

	@Override
	public void shutdownServer(HIPSInstallation installation) {
		if(!installation.canBeLaunched()) return;
		HIPSClient client = getClient(installation);
		if(client != null) {
			client.dispose();
		}
	}

	@Override
	public void removeHIPSEnvironment(LocalHIPSInstallation installation) throws IOException, InterruptedException {
		condaService.removeEnvironment(installation.getCondaPath(), environmentName);
		FileUtils.deleteDirectory(new File(getHIPSEnvironmentPath(installation)));
	}

	private void launch(HIPSInstallation installation, HIPSolution solution, boolean asTutorial) {
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
			HIPSClient client = getClient(installation);
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
		HIPSClient client = new HIPSClient(installation.getHost(), installation.getPort());
		clients.put(installation, client);
		context().inject(client);
	}

	private class ServerThread extends Thread {
		private final String environmentPath;
		private final LocalHIPSInstallation installation;
		private final File condaPath;

		public ServerThread(LocalHIPSInstallation installation, File condaPath, String environmentPath) {
			this.installation = installation;
			this.condaPath = condaPath;
			this.environmentPath = environmentPath;
		}

		@Override
		public void run() {
			try {
				tryToStartServer(installation, condaPath, environmentPath);
			} catch (IOException | InterruptedException e) {
				eventService.publish(new HIPSServerThreadDoneEvent(e));
			}
		}

		private void tryToStartServer(LocalHIPSInstallation installation, File condaPath, String environmentPath) throws IOException, InterruptedException {

			log.info("Trying to start the HIPS server locally on port " + installation.getPort() + "..");
			String[] command;
			String commandInCondaEnv = "run --no-capture-output --prefix " + environmentPath + " "
					+ (SystemUtils.IS_OS_WINDOWS ? new File(environmentPath, "python").getAbsolutePath() + " -m " : "")
					+ "hips server --port " + installation.getPort();
			command = condaService.createCondaCommand(condaPath, commandInCondaEnv);
			log().info(Arrays.toString(command));

			ProcessBuilder builder = new ProcessBuilder(command);
			Map<String, String> env = builder.environment();
			env.put("HIPS_DEFAULT_CATALOG", installation.getDefaultCatalog());
			final AtomicReference<Boolean> portInUse = new AtomicReference<>(false);
			serverException.set(false);
			Process process = builder.start();
			String addressInUseError = "[Errno 98] Address already in use";
			StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), log()) {
				@Override
				public void handleLog(String line) {
					if(line.contains(addressInUseError)) {
						portInUse.set(true);
						this.streamClosed = true;
					} else {
						try {
							super.handleLog(line);
						} catch (RuntimeException e) {
							serverException.set(true);
							eventService.publish(new HIPSServerThreadDoneEvent(e));
						}
					}
				}
			};
			StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), log());
			errorGobbler.start();
			outputGobbler.start();
			process.waitFor();
			if(portInUse.get()) {
				installation.setPort(installation.getPort()+1);
				log.warn("Port " + (installation.getPort()-1) + " is already in use, "
						+ " trying port " + installation.getPort() + " next.");
				tryToStartServer(installation, condaPath, environmentPath);
			}
		}
	}
}
