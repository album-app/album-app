package mdc.ida.hips.service;

import mdc.ida.hips.model.HIPSCollectionUpdatedEvent;
import mdc.ida.hips.model.HIPSInstallation;
import mdc.ida.hips.model.HIPSolution;
import mdc.ida.hips.model.LocalHIPSInstallation;
import mdc.ida.hips.model.RemoteHIPSInstallation;
import mdc.ida.hips.model.ServerProperties;
import org.scijava.service.SciJavaService;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public interface HIPSServerService extends SciJavaService {
	LocalHIPSInstallation loadLocalInstallation();
	RemoteHIPSInstallation loadRemoteInstallation(String url, int port);
	default boolean checkIfRunning(LocalHIPSInstallation installation) {
		return checkIfRunning(installation, () -> {});
	}
	boolean checkIfRunning(LocalHIPSInstallation installation, Runnable callbackIfRunning);
	void launchSolution(HIPSInstallation installation, HIPSolution solution);
	void launchSolutionAsTutorial(HIPSInstallation installation, HIPSolution solution);
	void updateIndex(LocalHIPSInstallation installation, Consumer<HIPSCollectionUpdatedEvent> callback) throws IOException;
	boolean checkIfHIPSEnvironmentExists(LocalHIPSInstallation installation);
	default void runAsynchronously(LocalHIPSInstallation installation) {
		runAsynchronously(installation, () -> {});
	}
	void runAsynchronously(LocalHIPSInstallation installation, Runnable callback);
	void runWithChecks(LocalHIPSInstallation installation);
	File getEnvironmentFile() throws IOException;
	void addCatalog(LocalHIPSInstallation installation, String urlOrPath) throws IOException;
	boolean checkIfCondaInstalled(LocalHIPSInstallation installation);

	void installConda(LocalHIPSInstallation installation) throws IOException;

	void createHIPSEnvironment(LocalHIPSInstallation installation) throws IOException, InterruptedException;

	ServerProperties getServerProperties(LocalHIPSInstallation installation) throws IOException;

	String getHIPSEnvironmentPath(LocalHIPSInstallation installation);

	void shutdownServer(HIPSInstallation installation);

	void removeHIPSEnvironment(LocalHIPSInstallation installation) throws IOException, InterruptedException;
}
