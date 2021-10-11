package mdc.ida.album.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import mdc.ida.album.DefaultValues;
import mdc.ida.album.UITextValues;
import mdc.ida.album.control.AlbumClient;
import mdc.ida.album.io.CollectionReader;
import mdc.ida.album.model.AlbumInstallation;
import mdc.ida.album.model.Catalog;
import mdc.ida.album.model.CollectionUpdates;
import mdc.ida.album.model.event.CatalogListEvent;
import mdc.ida.album.model.event.CollectionIndexEvent;
import mdc.ida.album.model.LocalAlbumInstallation;
import mdc.ida.album.model.event.CollectionUpgradeEvent;
import mdc.ida.album.model.event.CollectionUpgradePreviewEvent;
import mdc.ida.album.model.event.LocalInstallationLoadedEvent;
import mdc.ida.album.model.LogAddedEvent;
import mdc.ida.album.model.LogEntry;
import mdc.ida.album.model.event.RecentlyInstalledUpdatedEvent;
import mdc.ida.album.model.event.RecentlyLaunchedUpdatedEvent;
import mdc.ida.album.model.RemoteAlbumInstallation;
import mdc.ida.album.model.ServerProperties;
import mdc.ida.album.model.Solution;
import mdc.ida.album.model.SolutionArgument;
import mdc.ida.album.model.SolutionCollection;
import mdc.ida.album.model.event.SolutionLaunchFinishedEvent;
import mdc.ida.album.model.event.SolutionLaunchRequestEvent;
import mdc.ida.album.model.Task;
import mdc.ida.album.service.conda.CondaExecutableMissingEvent;
import mdc.ida.album.service.conda.CondaService;
import mdc.ida.album.utils.StreamGobbler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.scijava.Disposable;
import org.scijava.app.StatusService;
import org.scijava.command.DynamicCommandInfo;
import org.scijava.command.Inputs;
import org.scijava.display.DisplayService;
import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.event.SciJavaEvent;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@Plugin(type = Service.class)
public class DefaultAlbumServerService extends AbstractService implements AlbumServerService, Disposable {

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

	@Parameter
	private UIService uiService;

	@Parameter
	private DisplayService displayService;

	private final Map<AlbumInstallation, AlbumClient> clients = new HashMap<>();

	private Thread serverThread;
	final AtomicReference<Boolean> serverException = new AtomicReference<>(false);
	private SolutionCollection catalogList;

	@Override
	public LocalAlbumInstallation loadLocalInstallation() {
		int port = prefService.getInt(DefaultAlbumServerService.class, DefaultValues.ALBUM_PREF_LOCAL_PORT, 8080);
		log.info("Local album installation: default port: " + port);
//		log.info("Local album installation: default catalog URL: " + catalog);
		LocalAlbumInstallation installation = new LocalAlbumInstallation(port);
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
	public void index(LocalAlbumInstallation installation, Consumer<CollectionIndexEvent> callback) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		String actionName = "index";
		AlbumClient client = getClient(installation);
		JsonNode response = client.send(client.createAlbumRequest(mapper, actionName));
		if(response == null) return;
		statusService.showStatus("Updated album collection.");
		CollectionIndexEvent event = new CollectionIndexEvent(CollectionReader.readCollection(installation, response));
		callback.accept(event);
		eventService.publish(event);
	}

	@Override
	public void catalogList(AlbumInstallation installation, Consumer<CatalogListEvent> callback) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		String actionName = "catalogs";
		AlbumClient client = getClient(installation);
		JsonNode response = client.send(client.createAlbumRequest(mapper, actionName));
		if(response == null) return;
		statusService.showStatus("Updated album catalog list.");
		this.catalogList = CollectionReader.readCollection(installation, response);
		CatalogListEvent event = new CatalogListEvent(this.catalogList);
		if(callback != null) callback.accept(event);
		eventService.publish(event);
	}

	private AlbumClient getClient(AlbumInstallation installation) {
//		return clients.getOrDefault(installation, new AlbumClient(installation.getHost(), installation.getPort()));
		return clients.get(installation);
	}

	@Override
	public boolean checkIfRunning(LocalAlbumInstallation installation, Consumer<LocalInstallationLoadedEvent> callback) {
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
			installation.setServerRunning(running);
			if(running) {
				LocalInstallationLoadedEvent event = new LocalInstallationLoadedEvent(installation, true);
				callback.accept(event);
				eventService.publish(event);
			}
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
	public void runWithChecks(LocalAlbumInstallation installation, Consumer<LocalInstallationLoadedEvent> callback) {
		if(checkIfCondaInstalled(installation)
				&& checkIfAlbumEnvironmentExists(installation)) {
			runAsynchronously(installation, callback);
		}
	}

	@Override
	public boolean checkIfAlbumEnvironmentExists(LocalAlbumInstallation installation) {
		boolean exists = condaService.checkIfEnvironmentExists(installation.getCondaPath(), DefaultValues.ALBUM_ENVIRONMENT_NAME);
		installation.setHasAlbumEnvironment(exists);
		return exists;
	}

	@Override
	public void runAsynchronously(LocalAlbumInstallation installation, Consumer<LocalInstallationLoadedEvent> callback) {
		String condaExecutable = condaService.getCondaExecutable(installation.getCondaPath());
		String environmentPath = condaService.getEnvironmentPath(installation.getCondaPath(), DefaultValues.ALBUM_ENVIRONMENT_NAME);
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
		CollectionIndexEvent event = new CollectionIndexEvent(CollectionReader.readCollection(installation, response));
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
		return condaService.getEnvironmentPath(installation.getCondaPath(), DefaultValues.ALBUM_ENVIRONMENT_NAME);
	}

	@Override
	public void launchSolution(AlbumInstallation installation, Solution solution, String action) throws IOException {
		launch(installation, solution, action);
	}

	@EventHandler
	private void launchSolution(SolutionLaunchRequestEvent event) {
		new Thread(() -> {
			try {
				launchSolution(event.getInstallation(), event.getSolution(), event.getAction());
			} catch (IOException e) {
				e.printStackTrace();
			}
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
		condaService.removeEnvironment(installation.getCondaPath(), DefaultValues.ALBUM_ENVIRONMENT_NAME);
		FileUtils.deleteDirectory(new File(getAlbumEnvironmentPath(installation)));
	}

	@Override
	public void removeCatalog(AlbumInstallation installation, Catalog catalog) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		AlbumClient client = getClient(installation);
		String pathEncoded = URLEncoder.encode(catalog.getPath(), StandardCharsets.UTF_8.toString());
		ObjectNode solutionArgs = mapper.createObjectNode();
		solutionArgs.put("path", pathEncoded);
		JsonNode response = client.send(client.createAlbumRequest(mapper, "remove-catalog", solutionArgs));
		//TODO handle response
		catalogList(installation, e -> {});
	}

	@Override
	public void addCatalog(AlbumInstallation installation, String urlOrPath) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		AlbumClient client = getClient(installation);
		String pathEncoded = URLEncoder.encode(urlOrPath, StandardCharsets.UTF_8.toString());
		ObjectNode solutionArgs = mapper.createObjectNode();
		solutionArgs.put("path", pathEncoded);
		JsonNode response = client.send(client.createAlbumRequest(mapper, "add-catalog", solutionArgs));
		//TODO handle response
		catalogList(installation, e -> {});
	}

	@Override
	public void updateRecentlyLaunchedSolutionsList(AlbumInstallation installation, Consumer<RecentlyLaunchedUpdatedEvent> callback) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		String actionName = "recently-launched";
		AlbumClient client = getClient(installation);
		JsonNode response = client.send(client.createAlbumRequest(mapper, actionName));
		if(response == null) return;
		statusService.showStatus("Updated recently launched solutions list.");
		List<Solution> solutions = CollectionReader.readSolutionsList(response);
		resolveCatalogs(solutions);
		RecentlyLaunchedUpdatedEvent event = new RecentlyLaunchedUpdatedEvent(solutions);
		if(callback != null) callback.accept(event);
		eventService.publish(event);
	}

	private void resolveCatalogs(List<Solution> solutions) {
		for(Solution solution : solutions) {
			Catalog catalog = resolveCatalog(solution.getCatalogId());
			if(catalog != null) {
				solution.setCatalogName(catalog.getName());
			}
		}
	}

	private Catalog resolveCatalog(int catalog_id) {
		for(Catalog catalog : catalogList) {
			if(catalog.getId() == catalog_id) {
				return catalog;
			}
		}
		return null;
	}

	@Override
	public void updateRecentlyInstalledSolutionsList(AlbumInstallation installation, Consumer<RecentlyInstalledUpdatedEvent> callback) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		String actionName = "recently-installed";
		AlbumClient client = getClient(installation);
		JsonNode response = client.send(client.createAlbumRequest(mapper, actionName));
		if(response == null) return;
		statusService.showStatus("Updated recently installed solutions list.");
		List<Solution> solutions = CollectionReader.readSolutionsList(response);
		resolveCatalogs(solutions);
		RecentlyInstalledUpdatedEvent event = new RecentlyInstalledUpdatedEvent(solutions);
		if(callback != null) callback.accept(event);
		eventService.publish(event);
	}

	@Override
	public void update(AlbumInstallation installation) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		String actionName = "update";
		AlbumClient client = getClient(installation);
		JsonNode response = client.send(client.createAlbumRequest(mapper, actionName));
		if(response == null) return;
		statusService.showStatus("Updated collection.");
	}

	@Override
	public void upgradeDryRun(AlbumInstallation installation, Consumer<CollectionUpgradePreviewEvent> callback) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode solutionArgs = mapper.createObjectNode();
		solutionArgs.put("dry_run", true);
		String actionName = "upgrade";
		AlbumClient client = getClient(installation);
		JsonNode response = client.send(client.createAlbumRequest(mapper, actionName, solutionArgs));
		if(response == null) return;
		CollectionUpdates updates = CollectionReader.readUpdates(installation, response);
		CollectionUpgradePreviewEvent upgradeEvent = new CollectionUpgradePreviewEvent(updates);
		eventService.publish(upgradeEvent);
		callback.accept(upgradeEvent);
		statusService.showStatus("Done upgrading collection (dry run).");
	}

	@Override
	public void upgrade(AlbumInstallation installation, Consumer<CollectionUpgradeEvent> callback) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		String actionName = "upgrade";
		AlbumClient client = getClient(installation);
		JsonNode response = client.send(client.createAlbumRequest(mapper, actionName));
		if(response == null) return;
		CollectionUpdates updates = CollectionReader.readUpdates(installation, response);
		CollectionUpgradeEvent upgradeEvent = new CollectionUpgradeEvent(updates);
		eventService.publish(upgradeEvent);
		callback.accept(upgradeEvent);
		statusService.showStatus("Done upgrading collection.");
	}

	private void launch(AlbumInstallation installation, Solution solution, String actionName) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		String path = actionName + "/" + solution.getCatalogName() + "/" + solution.getGroup() + "/" + solution.getName() + "/" + solution.getVersion();
		ObjectNode solutionArgs = mapper.createObjectNode();

		if(actionName.equals("run") && solution.getArgs().size() > 0) {
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
		AlbumClient client = getClient(installation);
		JsonNode response = client.send(client.createAlbumRequest(mapper, path, solutionArgs));
		int taskId = response.get("id").asInt();
		if(displayService.getDisplay(UITextValues.TASKS_TAB_NAME) == null) uiService.show(UITextValues.TASKS_TAB_NAME, installation.getTasks());
		handleNewTask(installation, solution, taskId, actionName);
	}

	private void handleNewTask(AlbumInstallation installation, Solution solution, int taskId, String actionName) {
		Task task = new Task(taskId, solution);
		installation.getTasks().put(taskId, task);
		eventService.publish(new LogAddedEvent(task, List.of()));
		//TODO make sure new task is selected in tasks tab
		Timer timer = new Timer();
		TimerTask timerTask = new TimerTask() {
			public void run() {
				try {
					List<LogEntry> newLogs = getNewLogs(installation, task);
					eventService.publish(new LogAddedEvent(task, newLogs));
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(task.status == Task.Status.FINISHED) {
					timer.cancel();
					eventService.publish(new SolutionLaunchFinishedEvent(installation, solution, actionName));
				}
			}
		};
		timer.schedule(timerTask, DefaultValues.TASK_UPDATE_INTERVAL, DefaultValues.TASK_UPDATE_INTERVAL);
	}

	private List<LogEntry> getNewLogs(AlbumInstallation installation, Task task) throws IOException {
		AlbumClient client = getClient(installation);
		JsonNode resRequest = client.send(client.createAlbumRequest(new ObjectMapper(), "status/" + task.getId()));
		System.out.println(resRequest);
		String status = resRequest.get("status").asText();
		JsonNode records = resRequest.get("records");
		List<LogEntry> res = new ArrayList<>();
		if(status.equals("WAITING")) task.setStatus(Task.Status.WAITING);
		if(status.equals("RUNNING")) task.setStatus(Task.Status.RUNNING);
		if(status.equals("FINISHED")) task.setStatus(Task.Status.FINISHED);
		for (JsonNode record : records) {
			String ascTime = record.get("asctime").asText();
			String name = record.get("name").asText();
			String levelName = record.get("levelname").asText();
			String msg = record.get("msg").asText();
			boolean logFound = false;
			for (LogEntry taskLog : task.getLogs()) {
				if (taskLog.getAscTime().equals(ascTime) && taskLog.getMsg().equals(msg)) {
					logFound = true;
					break;
				}
			}
			if(logFound) continue;
			LogEntry logEntry = new LogEntry(msg, ascTime, name, levelName);
			task.getLogs().add(logEntry);
			res.add(logEntry);
		}
		return res;
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
				eventService.publish(new LocalInstallationLoadedEvent(installation, e));
			}
		}

		private void tryToStartServer(LocalAlbumInstallation installation, File condaPath, String environmentPath) throws IOException, InterruptedException {

			log.info("Trying to start the album server locally on port " + installation.getPort() + "..");
			String[] command;
			String commandInCondaEnv = "run --no-capture-output --prefix " + environmentPath + " "
					+ (SystemUtils.IS_OS_WINDOWS ? new File(environmentPath, "python").getAbsolutePath() + " -m " : "")
					+ "album server --port " + installation.getPort();
			command = condaService.createCondaCommand(condaPath, commandInCondaEnv);

			log().info(Arrays.toString(command));

			ProcessBuilder builder = new ProcessBuilder(command);

			Map<String, String> env = builder.environment();

//			env.put("ALBUM_DEFAULT_CATALOG", installation.getDefaultCatalog());
			env.put("ALBUM_CONDA_PATH", condaPath.getAbsolutePath());

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
							eventService.publish(new LocalInstallationLoadedEvent(installation, e));
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
