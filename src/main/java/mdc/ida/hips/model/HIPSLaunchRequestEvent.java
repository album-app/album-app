package mdc.ida.hips.model;

import org.scijava.event.SciJavaEvent;

public class HIPSLaunchRequestEvent extends SciJavaEvent {
	private final HIPSolution solution;
	private final boolean tutorial;
	private final HIPSInstallation installation;

	public HIPSLaunchRequestEvent(HIPSInstallation installation, HIPSolution solution, boolean tutorial) {
		this.solution = solution;
		this.tutorial = tutorial;
		this.installation = installation;
	}

	public HIPSolution getSolution() {
		return solution;
	}

	public boolean launchAsTutorial() {
		return tutorial;
	}

	public HIPSInstallation getInstallation() {
		return installation;
	}
}
