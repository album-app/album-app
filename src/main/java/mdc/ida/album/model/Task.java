package mdc.ida.album.model;

import java.util.ArrayList;
import java.util.List;

public class Task {
	public enum Status {
		WAITING, RUNNING, FINISHED;

	}
	private final int id;
	private final Solution solution;
	private final List<LogEntry> logs;
	public Status status;
	public Task(int id, Solution solution) {
		this.id = id;
		this.solution = solution;
		this.logs = new ArrayList<>();
	}

	public int getId() {
		return id;
	}

	public Solution getSolution() {
		return solution;
	}

	public List<LogEntry> getLogs() {
		return logs;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Status getStatus() {
		return status;
	}
}
