package mdc.ida.hips.service;

import mdc.ida.hips.HIPSOptions;
import mdc.ida.hips.model.HIPSolution;
import org.scijava.event.EventHandler;
import org.scijava.service.SciJavaService;

public interface HIPSService extends SciJavaService {
	void init(HIPSOptions.Values options);
	void launchSolution(HIPSolution solution);
	void updateAndDisplayIndex();
	void handleServerResponse(String msg, String response);
}
