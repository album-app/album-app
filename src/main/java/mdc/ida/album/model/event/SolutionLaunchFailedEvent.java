package mdc.ida.album.model.event;

import mdc.ida.album.model.AlbumInstallation;
import mdc.ida.album.model.Solution;
import org.scijava.event.SciJavaEvent;

public class SolutionLaunchFailedEvent extends SciJavaEvent {
	private final Solution solution;
	private final String action;
	private final AlbumInstallation installation;

	public SolutionLaunchFailedEvent(AlbumInstallation installation, Solution solution, String action) {
		this.solution = solution;
		this.action = action;
		this.installation = installation;
	}

	public Solution getSolution() {
		return solution;
	}

	public String getAction() {
		return action;
	}

	public AlbumInstallation getInstallation() {
		return installation;
	}
}
