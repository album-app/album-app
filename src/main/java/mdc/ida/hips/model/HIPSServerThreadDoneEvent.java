package mdc.ida.hips.model;

import org.scijava.event.SciJavaEvent;

public class HIPSServerThreadDoneEvent extends SciJavaEvent {
	private final boolean success;
	private final Exception exception;

	public HIPSServerThreadDoneEvent(boolean success) {
		this.success = success;
		exception = null;
	}

	public HIPSServerThreadDoneEvent(Exception e) {
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
