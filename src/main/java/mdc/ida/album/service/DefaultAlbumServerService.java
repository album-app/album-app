package mdc.ida.album.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import mdc.ida.album.AlbumClient;
import mdc.ida.album.io.CollectionReader;
import mdc.ida.album.model.AlbumInstallation;
import mdc.ida.album.model.CollectionUpdatedEvent;
import mdc.ida.album.model.LocalAlbumInstallation;
import mdc.ida.album.model.RemoteAlbumInstallation;
import mdc.ida.album.model.ServerProperties;
import mdc.ida.album.model.ServerThreadDoneEvent;
import mdc.ida.album.model.Solution;
import mdc.ida.album.model.SolutionArgument;
import mdc.ida.album.model.SolutionLaunchRequestEvent;
import mdc.ida.album.service.conda.CondaExecutableMissingEvent;
import mdc.ida.album.service.conda.CondaService;
import mdc.ida.album.utils.StreamGobbler;
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
public class DefaultAlbumServerService extends AbstractService implements AlbumServerService, Disposable {

	private final String ALBUM_ENVIRONMENT_NAME = "album";
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

	private final Map<AlbumInstallation, AlbumClient> clients = new HashMap<>();

	private static final String ALBUM_PREF_LOCAL_PORT = "album.local.port";
	private static final String ALBUM_PREF_LOCAL_DEFAULT_CATALOG = "album.local.default_catalog";
	private static final String ALBUM_DEFAULT_CATALOG_URL = "https://gitlab.com/ida-mdc/capture-knowledge";

	private Thread serverThread;
	final AtomicReference<Boolean> serverException = new AtomicReference<>(false);
	private static final String environmentName = "album";

	@Override
	public LocalAlbumInstallation loadLocalInstallation() {
		int port = prefService.getInt(DefaultAlbumServerService.class, ALBUM_PREF_LOCAL_PORT, 8080);
		String catalog = prefService.get(DefaultAlbumServerService.class, ALBUM_PREF_LOCAL_DEFAULT_CATALOG, ALBUM_DEFAULT_CATALOG_URL);
		log.info("Local album installation: default port: " + port);
		log.info("Local album installation: default catalog URL: " + catalog);
		LocalAlbumInstallation installation = new LocalAlbumInstallation(port, catalog);
		File defaultCondaPath = condaService.getDefaultCondaPath();
		if(defaultCondaPath != null) installation.setCondaPath(defaultCondaPath);
		else {
			log.info("No conda path associated with this installation, please run the initial setup in the UI.");
		}
		return installation;
	}

	@Override
	public RemoteAlbumInstallation loadRemoteInstallation(String url, int port) {
		return new RemoteAlbumInstallation(url, port);
	}

	@Override
	public void updateIndex(LocalAlbumInstallation installation, Consumer<CollectionUpdatedEvent> callback) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		String actionName = "index";
		AlbumClient client = getClient(installation);
		JsonNode response = client.send(client.createAlbumRequest(mapper, actionName));
		if(response == null) return;
		statusService.showStatus("Updated album collection.");
		CollectionUpdatedEvent event = new CollectionUpdatedEvent(CollectionReader.readCollection(installation, response));
		callback.accept(event);
		eventService.publish(event);
	}

	private AlbumClient getClient(AlbumInstallation installation) {
//		return clients.getOrDefault(installation, new AlbumClient(installation.getHost(), installation.getPort()));
		return clients.get(installation);
	}

	@Override
	public boolean checkIfRunning(LocalAlbumInstallation installation, Runnable callback) {
		AlbumClient client = getClient(installation);
		if(client == null) {
			installation.setServerRunning(false);
			return false;
		}
		ObjectMapper mapper = new ObjectMapper();
		String actionName = "";
		try {
			JsonNode response = client.send(client.createAlbumRequest(mapper, actionName));
			boolean running = response != null;
			if(running) {
				callback.run();
				eventService.publish(new ServerThreadDoneEvent(true));
			}
			installation.setServerRunning(true);
			return running;
		} catch (AlbumClient.ServerNotAvailableException | IOException e) {
			installation.setServerRunning(false);
			return false;
		}
	}

	@Override
	public void runWithChecks(LocalAlbumInstallation installation) {
		if(checkIfCondaInstalled(installation)
				&& checkIfAlbumEnvironmentExists(installation)) {
			runAsynchronously(installation);
		}
	}

	@Override
	public boolean checkIfAlbumEnvironmentExists(LocalAlbumInstallation installation) {
		boolean exists = condaService.checkIfEnvironmentExists(installation.getCondaPath(), ALBUM_ENVIRONMENT_NAME);
		installation.setHasAlbumEnvironment(exists);
		return exists;
	}

	@Override
	public void runAsynchronously(LocalAlbumInstallation installation, Runnable callback) {
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
		File tmpFile = Files.createTempFile("album", ".yml").toFile();
		FileUtils.copyInputStreamToFile(getClass().getResourceAsStream("album.yml"), tmpFile);
		return tmpFile;
	}

	@Override
	public void addCatalog(LocalAlbumInstallation installation, String urlOrPath) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		String actionName = "catalogs/add";
		ObjectNode solutionArgs = mapper.createObjectNode();
		solutionArgs.put("url", urlOrPath);
		AlbumClient client = getClient(installation);
		JsonNode response = client.send(client.createAlbumRequest(mapper, actionName));
		if(response == null) return;
		statusService.showStatus("Added catalog " + urlOrPath + " to album collection.");
		CollectionUpdatedEvent event = new CollectionUpdatedEvent(CollectionReader.readCollection(installation, response));
		eventService.publish(event);
	}

	@Override
	public boolean checkIfCondaInstalled(LocalAlbumInstallation installation) {
		File condaPath = installation.getCondaPath();
		logService.info("Checking for conda installation: " + condaPath);
		boolean installed = condaService.checkIfCondaInstalled(condaPath);
		if(installed) logService.info("Conda installed to " + condaPath);
		installation.setCondaInstalled(installed);
		return installed;
	}

	@Override
	public void installConda(LocalAlbumInstallation installation) throws IOException {
		condaService.installConda(installation.getCondaPath());
	}

	@Override
	public void createAlbumEnvironment(LocalAlbumInstallation installation) throws IOException, InterruptedException {
		condaService.createEnvironment(installation.getCondaPath(), getEnvironmentFile());
	}

	@Override
	public ServerProperties getServerProperties(LocalAlbumInstallation installation) throws IOException {
		AlbumClient client = getClient(installation);
		if(client == null) return null;
		if(!installation.isServerRunning()) return null;
		ObjectMapper mapper = new ObjectMapper();
		String actionName = "config";
		JsonNode response = client.send(client.createAlbumRequest(mapper, actionName));
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
	public String getAlbumEnvironmentPath(LocalAlbumInstallation installation) {
		return condaService.getEnvironmentPath(installation.getCondaPath(), environmentName);
	}

	@Override
	public void launchSolution(AlbumInstallation installation, Solution solution, String action) {
		launch(installation, solution, action);
	}

	@EventHandler
	private void launchSolution(SolutionLaunchRequestEvent event) {
		new Thread(() -> {
			launchSolution(event.getInstallation(), event.getSolution(), event.getAction());
		}).start();
	}

	@Override
	public void dispose() {
		clients.forEach((albumInstallation, albumClient) -> {
			shutdownServer(albumInstallation);
		});
		if(serverThread != null && serverThread.isAlive()) {
			serverThread.stop();
		}
	}

	@Override
	public void shutdownServer(AlbumInstallation installation) {
		if(!installation.canBeLaunched()) return;
		AlbumClient client = getClient(installation);
		if(client != null) {
			client.dispose();
		}
	}

	@Override
	public void removeAlbumEnvironment(LocalAlbumInstallation installation) throws IOException, InterruptedException {
		condaService.removeEnvironment(installation.getCondaPath(), environmentName);
		FileUtils.deleteDirectory(new File(getAlbumEnvironmentPath(installation)));
	}

	private void launch(AlbumInstallation installation, Solution solution, String actionName) {
		ObjectMapper mapper = new ObjectMapper();
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
			AlbumClient client = getClient(installation);
			JsonNode response = client.send(client.createAlbumRequest(mapper, path, solutionArgs));
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

	private void initClient(AlbumInstallation installation) {
		AlbumClient client = new AlbumClient(installation.getHost(), installation.getPort());
		clients.put(installation, client);
		context().inject(client);
	}

	private class ServerThread extends Thread {
		private final String environmentPath;
		private final LocalAlbumInstallation installation;
		private final File condaPath;

		public ServerThread(LocalAlbumInstallation installation, File condaPath, String environmentPath) {
			this.installation = installation;
			this.condaPath = condaPath;
			this.environmentPath = environmentPath;
		}

		@Override
		public void run() {
			try {
				tryToStartServer(installation, condaPath, environmentPath);
			} catch (IOException | InterruptedException e) {
				eventService.publish(new ServerThreadDoneEvent(e));
			}
		}

		private void tryToStartServer(LocalAlbumInstallation installation, File condaPath, String environmentPath) throws IOException, InterruptedException {

			log.info("Trying to start the album server locally on port " + installation.getPort() + "..");
			String[] command;
			String commandInCondaEnv = "run --no-capture-output --prefix " + environmentPath + " "
					+ (SystemUtils.IS_OS_WINDOWS ? new File(environmentPath, "python").getAbsolutePath() + " -m " : "")
					+ "hips server --port " + installation.getPort();
			command = condaService.createCondaCommand(condaPath, commandInCondaEnv);

			log().info(Arrays.toString(command));

			ProcessBuilder builder = new ProcessBuilder(command);

			Map<String, String> env = builder.environment();

			env.put("HIPS_DEFAULT_CATALOG", installation.getDefaultCatalog());
			env.put("HIPS_CONDA_PATH", condaPath.getAbsolutePath());

			log.info("Server environment variables: " + env);

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
							eventService.publish(new ServerThreadDoneEvent(e));
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
