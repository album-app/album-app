package mdc.ida.album.model;

public class LogEntry {
	final String msg;
	final String ascTime;
	final String name;
	final String levelName;

	public LogEntry(String msg, String ascTime, String name, String levelName) {
		this.msg = msg;
		this.ascTime = ascTime;
		this.name = name;
		this.levelName = levelName;
	}

	public String getMsg() {
		return msg;
	}

	public String getAscTime() {
		return ascTime;
	}

	public String getName() {
		return name;
	}

	public String getLevelName() {
		return levelName;
	}
}
