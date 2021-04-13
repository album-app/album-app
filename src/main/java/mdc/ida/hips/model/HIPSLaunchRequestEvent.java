package mdc.ida.hips.model;

import org.scijava.event.SciJavaEvent;

public class HIPSLaunchRequestEvent extends SciJavaEvent {
	private final HIPSolution solution;

	public HIPSLaunchRequestEvent(HIPSolution solution) {
		this.solution = solution;
	}

	public HIPSolution getSolution() {
		return solution;
	}
}
