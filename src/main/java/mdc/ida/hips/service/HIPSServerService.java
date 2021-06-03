package mdc.ida.hips.service;

import mdc.ida.hips.model.HIPSCollectionUpdatedEvent;
import mdc.ida.hips.model.HIPSolution;
import mdc.ida.hips.model.LocalHIPSInstallation;
import mdc.ida.hips.model.RemoteHIPSInstallation;
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
	void launchSolution(HIPSolution solution);
	void launchSolutionAsTutorial(HIPSolution solution);
	void updateIndex(Consumer<HIPSCollectionUpdatedEvent> callback) throws IOException;
	boolean checkIfHIPSEnvironmentExists(LocalHIPSInstallation installation);
	default void runAsynchronously(LocalHIPSInstallation installation) {
		runAsynchronously(installation, () -> {});
	}
	void runAsynchronously(LocalHIPSInstallation installation, Runnable callback);
	void runWithChecks(LocalHIPSInstallation installation) throws IOException, InterruptedException;
	File getEnvironmentFile() throws IOException;
	void addCatalog(LocalHIPSInstallation installation, String urlOrPath) throws IOException;
}
