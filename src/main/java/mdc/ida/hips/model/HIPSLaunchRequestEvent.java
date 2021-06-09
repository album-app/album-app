package mdc.ida.hips.model;

import org.scijava.event.SciJavaEvent;

public class HIPSLaunchRequestEvent extends SciJavaEvent {
	private final HIPSolution solution;
	private final String action;
	private final HIPSInstallation installation;

	public HIPSLaunchRequestEvent(HIPSInstallation installation, HIPSolution solution, String action) {
		this.solution = solution;
		this.action = action;
		this.installation = installation;
	}

	public HIPSolution getSolution() {
		return solution;
	}

	public String getAction() {
		return action;
	}

	public HIPSInstallation getInstallation() {
		return installation;
	}
}
