package mdc.ida.album.model;

import org.scijava.event.SciJavaEvent;

import java.util.List;

public class LogAddedEvent extends SciJavaEvent {

	private final Task task;
	private final List<LogEntry> logEntries;

	public LogAddedEvent(Task task, List<LogEntry> logEntries) {
		this.task = task;
		this.logEntries = logEntries;
	}

	public Task getTask() {
		return task;
	}

	public List<LogEntry> getLogEntries() {
		return logEntries;
	}
}
