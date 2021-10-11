package mdc.ida.album.model;

public class CatalogUpdate {
	public enum Action {
		ADDED, REMOVED, MODIFIED;

	}
	private final Coordinates coordinates;
	private final String changelog;
	public Action action;
	public CatalogUpdate(Coordinates coordinates, Action action, String changelog) {
		this.coordinates = coordinates;
		this.action = action;
		this.changelog = changelog;
	}

	public Action getAction() {
		return action;
	}

	public Coordinates getCoordinates() {
		return coordinates;
	}

	public String getChangelog() {
		return changelog;
	}
}
