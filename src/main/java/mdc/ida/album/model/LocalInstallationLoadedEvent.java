package mdc.ida.album.model;

import org.scijava.event.SciJavaEvent;

public class LocalInstallationLoadedEvent extends SciJavaEvent {
	private final boolean success;
	private final Exception exception;
	private final LocalAlbumInstallation installation;

	public LocalInstallationLoadedEvent(LocalAlbumInstallation installation, boolean success) {
		this.success = success;
		exception = null;
		this.installation = installation;
	}

	public LocalInstallationLoadedEvent(LocalAlbumInstallation installation, Exception e) {
		this.success = false;
		this.exception = e;
		this.installation = installation;
	}

	public boolean isSuccess() {
		return success;
	}

	public Exception getException() {
		return exception;
	}

	public LocalAlbumInstallation getInstallation() {
		return installation;
	}
}
