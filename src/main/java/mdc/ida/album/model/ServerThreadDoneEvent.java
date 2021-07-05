package mdc.ida.album.model;

import org.scijava.event.SciJavaEvent;

public class ServerThreadDoneEvent extends SciJavaEvent {
	private final boolean success;
	private final Exception exception;

	public ServerThreadDoneEvent(boolean success) {
		this.success = success;
		exception = null;
	}

	public ServerThreadDoneEvent(Exception e) {
		this.success = false;
		this.exception = e;
	}

	public boolean isSuccess() {
		return success;
	}

	public Exception getException() {
		return exception;
	}
}
