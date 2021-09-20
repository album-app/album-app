package mdc.ida.album.model;

import org.scijava.event.SciJavaEvent;

import java.util.List;

public class RecentlyInstalledUpdatedEvent extends SciJavaEvent {
	private final List<Solution> solutions;

	public RecentlyInstalledUpdatedEvent(List<Solution> solutions) {
		this.solutions = solutions;
	}

	public List<Solution> getSolutions() {
		return solutions;
	}
}
