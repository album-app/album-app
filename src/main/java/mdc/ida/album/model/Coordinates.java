package mdc.ida.album.model;

public class Coordinates {
	private final String group;
	private final String name;
	private final String version;

	public Coordinates(String group, String name, String version) {
		this.group = group;
		this.name = name;
		this.version = version;
	}

	public String getGroup() {
		return group;
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	@Override
	public String toString() {
		return getGroup() + ":" + getName() + ":" + getVersion();
	}
}
