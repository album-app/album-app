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
	boolean checkIfRunning(LocalHIPSInstallation installation);
	void launchSolution(HIPSolution solution);
	void launchSolutionAsTutorial(HIPSolution solution);
	void updateIndex(Consumer<HIPSCollectionUpdatedEvent> callback) throws IOException;

	boolean checkIfHIPSEnvironmentExists(LocalHIPSInstallation installation);

	void runAsynchronously(LocalHIPSInstallation installation) throws IOException, InterruptedException;
	void runWithChecks(LocalHIPSInstallation installation) throws IOException, InterruptedException;
	File getEnvironmentFile() throws IOException;
}
