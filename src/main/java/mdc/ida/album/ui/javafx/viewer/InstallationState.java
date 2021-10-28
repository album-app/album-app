package mdc.ida.album.ui.javafx.viewer;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import mdc.ida.album.model.event.CollectionIndexEvent;
import mdc.ida.album.model.LocalAlbumInstallation;
import mdc.ida.album.model.ServerProperties;
import mdc.ida.album.model.event.CollectionUpgradePreviewEvent;
import mdc.ida.album.model.event.LocalInstallationLoadedEvent;
import mdc.ida.album.control.AlbumServerService;
import mdc.ida.album.model.event.CondaEnvironmentDetectedEvent;
import mdc.ida.album.model.event.CondaExecutableMissingEvent;
import mdc.ida.album.model.event.CondaPathMissingEvent;
import mdc.ida.album.control.conda.CondaService;
import mdc.ida.album.model.event.HasCondaInstalledEvent;
import org.scijava.event.EventHandler;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class InstallationState {

	@Parameter
	private AlbumServerService albumService;
	@Parameter
	private CondaService condaService;
	@Parameter
	private LogService logService;

	private final LocalAlbumInstallation installation;
	private final BooleanProperty albumRunning = new SimpleBooleanProperty(false);
	private final BooleanProperty condaInstalled = new SimpleBooleanProperty(false);
	private final BooleanProperty condaMissing = new SimpleBooleanProperty(false);
	private final BooleanProperty condaShouldExist = new SimpleBooleanProperty(false);
	private final BooleanProperty initialSetupRunning = new SimpleBooleanProperty(false);
	private final BooleanProperty hasAlbumEnvironment = new SimpleBooleanProperty(false);
	private final StringProperty catalog = new SimpleStringProperty();

	public InstallationState(LocalAlbumInstallation installation) {
		this.installation = installation;
		catalog.set(installation.getDefaultCatalog());
		albumRunning.set(installation.isServerRunning());
		condaInstalled.set(installation.isCondaInstalled());
		condaMissing.set(!installation.isCondaInstalled());
		condaShouldExist.set(installation.getCondaPath() != null);
		hasAlbumEnvironment.set(installation.isHasAlbumEnvironment());
	}

	@EventHandler
	private void albumServerThreadDone(LocalInstallationLoadedEvent e) {
		albumRunning.set(e.isSuccess());
		if(e.isSuccess()) {
			new Thread(() -> {
				try {
					albumService.catalogList(installation, null);
					albumService.updateRecentlyLaunchedSolutionsList(installation, null);
					albumService.updateRecentlyInstalledSolutionsList(installation, null);
				} catch (IOException ioException) {
					ioException.printStackTrace();
				}
			}).start();
		}
	}

	@EventHandler
	private void hasCondaInstalled(HasCondaInstalledEvent e) {
		condaInstalled.set(true);
		condaMissing.set(false);
	}

	@EventHandler
	private void condaExecutableMissing(CondaExecutableMissingEvent e) {
		condaMissing.set(true);
		condaShouldExist.set(true);
	}

	@EventHandler
	private void condaPathMissing(CondaPathMissingEvent e) {
		condaMissing.set(true);
		condaShouldExist.set(false);
	}

	@EventHandler
	private void hasAlbumEnvironment(CondaEnvironmentDetectedEvent e) {
		hasAlbumEnvironment.set(true);
	}

	public LocalAlbumInstallation getInstallation() {
		return installation;
	}

	public boolean isAlbumRunning() {
		return albumRunning.get();
	}

	public BooleanProperty albumRunningProperty() {
		return albumRunning;
	}

	public boolean isCondaInstalled() {
		return condaInstalled.get();
	}

	public BooleanProperty condaInstalledProperty() {
		return condaInstalled;
	}

	public boolean isCondaMissing() {
		return condaMissing.get();
	}

	public BooleanProperty condaMissingProperty() {
		return condaMissing;
	}

	public boolean isCondaShouldExist() {
		return condaShouldExist.get();
	}

	public BooleanProperty condaShouldExistProperty() {
		return condaShouldExist;
	}

	public boolean isInitialSetupRunning() {
		return initialSetupRunning.get();
	}

	public BooleanProperty initialSetupRunningProperty() {
		return initialSetupRunning;
	}

	public boolean isHasAlbumEnvironment() {
		return hasAlbumEnvironment.get();
	}

	public BooleanProperty hasAlbumEnvironmentProperty() {
		return hasAlbumEnvironment;
	}

	public String getCatalog() {
		return catalog.get();
	}

	public StringProperty catalogProperty() {
		return catalog;
	}


	void initAlbumInstallation(boolean downloadConda, StringProperty downloadTarget) throws IOException, InterruptedException {
		initialSetupRunning.set(true);
		if(downloadConda) {
			downloadAndInstallConda(downloadTarget);
			File condaPath = new File(downloadTarget.get());
			setCondaPath(condaPath);
		} else {
			checkCondaLocation();
		}
		if(!albumService.checkIfCondaInstalled(installation)) {
			logService.error("Cannot find conda in " + installation.getCondaPath());
			return;
		}
		if(!hasAlbumEnvironment.get()) {
			albumService.createAlbumEnvironment(installation);
		}
		if(albumService.checkIfAlbumEnvironmentExists(installation)) {
			installation.setDefaultCatalog(catalog.get());
			albumService.runAsynchronously(installation);
		} else {
			logService.error("Could not install album environment using conda " + installation.getCondaPath());
		}
		initialSetupRunning.set(false);
	}

	void setCondaPath(File path) {
		installation.setCondaPath(path);
		condaService.setDefaultCondaPath(path);
	}

	public String getCondaPath() {
		String condaPath = "";
		if(installation.getCondaPath() != null && installation.getCondaPath().exists()) {
			condaPath = installation.getCondaPath().getAbsolutePath();
		}
		return condaPath;
	}

	String getCondaExecutable() {
		return condaService.getCondaExecutable(installation.getCondaPath());
	}

	void downloadAndInstallConda(StringProperty downloadTarget) throws IOException {
		if(downloadTarget.isEmpty().get()) return;
		File condaPath = new File(downloadTarget.get());
		setCondaPath(condaPath);
		albumService.installConda(installation);
		condaInstalled.set(albumService.checkIfCondaInstalled(installation));
	}

	void checkCondaLocation() {
		condaInstalled.set(albumService.checkIfCondaInstalled(installation));
		if(condaInstalled.get()) {
			hasAlbumEnvironment.set(albumService.checkIfAlbumEnvironmentExists(installation));
		}
	}

	public void updateIndex(Consumer<CollectionIndexEvent> collectionUpdated) throws IOException {
		albumService.index(installation, collectionUpdated);
	}

	public void createEnvironment() throws IOException, InterruptedException {
		albumService.createAlbumEnvironment(installation);
		albumService.checkIfAlbumEnvironmentExists(installation);
	}

	public void runServer() {
		albumService.runAsynchronously(installation);
	}

	void validateCondaTarget(StringProperty downloadTarget, File file) throws IOException {
		if (file == null) return;
		if (file.exists() && !isDirEmpty(file.toPath())) {
			file = new File(file, "miniconda");
		}
		downloadTarget.set(file.getAbsolutePath());
	}

	private static boolean isDirEmpty(final Path directory) throws IOException {
		try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
			return !dirStream.iterator().hasNext();
		}
	}

	public ServerProperties getServerProperties() throws IOException {
		return albumService.getServerProperties(installation);
	}

	public File getDefaultCondaDownloadTarget() {
		return condaService.getDefaultCondaDownloadTarget();
	}

	public String getAlbumEnvironmentPath() {
		return albumService.getAlbumEnvironmentPath(installation);
	}

	public void closeServer() {
		albumService.shutdownServer(installation);
	}

	public void removeEnvironment() throws IOException, InterruptedException {
		albumService.removeAlbumEnvironment(installation);
	}

	public void resetCondaPath() {
		condaService.removeDefaultCondaPath();
	}

	public void startUpdate(Consumer<CollectionUpgradePreviewEvent> callback) throws IOException {
		albumService.update(this.installation);
		albumService.upgradeDryRun(this.installation, callback);
	}
}
