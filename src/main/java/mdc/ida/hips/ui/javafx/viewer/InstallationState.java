package mdc.ida.hips.ui.javafx.viewer;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import mdc.ida.hips.model.HIPSCollectionUpdatedEvent;
import mdc.ida.hips.model.HIPSServerThreadDoneEvent;
import mdc.ida.hips.model.LocalHIPSInstallation;
import mdc.ida.hips.model.ServerProperties;
import mdc.ida.hips.service.HIPSServerService;
import mdc.ida.hips.service.conda.CondaEnvironmentDetectedEvent;
import mdc.ida.hips.service.conda.CondaExecutableMissingEvent;
import mdc.ida.hips.service.conda.CondaPathMissingEvent;
import mdc.ida.hips.service.conda.CondaService;
import mdc.ida.hips.service.conda.HasCondaInstalledEvent;
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
	private HIPSServerService hipsService;
	@Parameter
	private CondaService condaService;
	@Parameter
	private LogService logService;

	private LocalHIPSInstallation installation;
	private final BooleanProperty hipsRunning = new SimpleBooleanProperty(false);
	private final BooleanProperty condaInstalled = new SimpleBooleanProperty(false);
	private final BooleanProperty condaMissing = new SimpleBooleanProperty(false);
	private final BooleanProperty condaShouldExist = new SimpleBooleanProperty(false);
	private final BooleanProperty initialSetupRunning = new SimpleBooleanProperty(false);
	private final BooleanProperty hasHipsEnvironment = new SimpleBooleanProperty(false);
	private final StringProperty hipsCatalog = new SimpleStringProperty();

	public InstallationState(LocalHIPSInstallation installation) {
		this.installation = installation;
		hipsCatalog.set(installation.getDefaultCatalog());
		hipsRunning.set(installation.isServerRunning());
		condaInstalled.set(installation.isCondaInstalled());
		condaMissing.set(!installation.isCondaInstalled());
		condaShouldExist.set(installation.getCondaPath() != null);
		hasHipsEnvironment.set(installation.isHasHipsEnvironment());
	}

	@EventHandler
	private void hipsServerThreadDone(HIPSServerThreadDoneEvent e) {
		hipsRunning.set(e.isSuccess());
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
	private void hasHIPSEnvironment(CondaEnvironmentDetectedEvent e) {
		hasHipsEnvironment.set(true);
	}

	public LocalHIPSInstallation getInstallation() {
		return installation;
	}

	public boolean isHipsRunning() {
		return hipsRunning.get();
	}

	public BooleanProperty hipsRunningProperty() {
		return hipsRunning;
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

	public boolean isHasHipsEnvironment() {
		return hasHipsEnvironment.get();
	}

	public BooleanProperty hasHipsEnvironmentProperty() {
		return hasHipsEnvironment;
	}

	public String getHipsCatalog() {
		return hipsCatalog.get();
	}

	public StringProperty hipsCatalogProperty() {
		return hipsCatalog;
	}


	void initHIPSInstallation(boolean downloadConda, StringProperty downloadTarget) throws IOException, InterruptedException {
		initialSetupRunning.set(true);
		if(downloadConda) {
			downloadAndInstallConda(downloadTarget);
			File condaPath = new File(downloadTarget.get());
			setCondaPath(condaPath);
		} else {
			checkCondaLocation();
		}
		if(!hipsService.checkIfCondaInstalled(installation)) {
			logService.error("Cannot find conda in " + installation.getCondaPath());
			return;
		}
		if(!hasHipsEnvironment.get()) {
			hipsService.createHIPSEnvironment(installation);
		}
		if(hipsService.checkIfHIPSEnvironmentExists(installation)) {
			installation.setDefaultCatalog(hipsCatalog.get());
			hipsService.runAsynchronously(installation);
		} else {
			logService.error("Could not install hips environment using conda " + installation.getCondaPath());
		}
		initialSetupRunning.set(false);
	}

	void setCondaPath(File path) {
		installation.setCondaPath(path);
		condaService.setDefaultCondaPath(path);
	}

	String getCondaPath() {
		String condaPath = "";
		if(installation.getCondaPath() != null && installation.getCondaPath().exists()) {
			condaPath = installation.getCondaPath().getAbsolutePath();
		}
		return condaPath;
	}

	void downloadAndInstallConda(StringProperty downloadTarget) throws IOException {
		if(downloadTarget.isEmpty().get()) return;
		File condaPath = new File(downloadTarget.get());
		setCondaPath(condaPath);
		hipsService.installConda(installation);
		condaInstalled.set(hipsService.checkIfCondaInstalled(installation));
	}

	void checkCondaLocation() {
		condaInstalled.set(hipsService.checkIfCondaInstalled(installation));
		if(condaInstalled.get()) {
			hasHipsEnvironment.set(hipsService.checkIfHIPSEnvironmentExists(installation));
		}
	}

	public void updateIndex(Consumer<HIPSCollectionUpdatedEvent> collectionUpdated) throws IOException {
		hipsService.updateIndex(installation, collectionUpdated);
	}

	public void createEnvironment() throws IOException, InterruptedException {
		hipsService.createHIPSEnvironment(installation);
		hipsService.checkIfHIPSEnvironmentExists(installation);
	}

	public void runServer() {
		hipsService.runAsynchronously(installation);
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
		return hipsService.getServerProperties(installation);
	}

	public File getDefaultCondaDownloadTarget() {
		return condaService.getDefaultCondaDownloadTarget();
	}

	public String getHipsEnvironmentPath() {
		return hipsService.getHIPSEnvironmentPath(installation);
	}

	public void closeServer() {
		hipsService.shutdownServer(installation);
	}

	public void removeEnvironment() throws IOException, InterruptedException {
		hipsService.removeHIPSEnvironment(installation);
	}

	public void resetCondaPath() {
		condaService.removeDefaultCondaPath();
	}
}
