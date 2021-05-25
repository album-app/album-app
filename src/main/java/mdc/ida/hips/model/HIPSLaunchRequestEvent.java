package mdc.ida.hips.model;

import org.scijava.event.SciJavaEvent;

public class HIPSLaunchRequestEvent extends SciJavaEvent {
	private final HIPSolution solution;
	private final boolean tutorial;

	public HIPSLaunchRequestEvent(HIPSolution solution, boolean tutorial) {
		this.solution = solution;
		this.tutorial = tutorial;
	}

	public HIPSolution getSolution() {
		return solution;
	}

	public boolean launchAsTutorial() {
		return tutorial;
	}
}
