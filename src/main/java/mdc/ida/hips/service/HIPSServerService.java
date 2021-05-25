package mdc.ida.hips.service;

import mdc.ida.hips.HIPSOptions;
import mdc.ida.hips.model.HIPSCollectionUpdatedEvent;
import mdc.ida.hips.model.HIPSolution;
import org.scijava.service.SciJavaService;

import java.io.IOException;
import java.util.function.Consumer;

public interface HIPSServerService extends SciJavaService {
	void init(HIPSOptions.Values options);
	void launchSolution(HIPSolution solution);
	void launchSolutionAsTutorial(HIPSolution solution);
	void updateIndex(Consumer<HIPSCollectionUpdatedEvent> callback) throws IOException;
}
