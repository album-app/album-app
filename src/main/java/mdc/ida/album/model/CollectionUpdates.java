package mdc.ida.album.model;

import java.util.HashMap;
import java.util.List;

public class CollectionUpdates extends HashMap<String, List<CatalogUpdate>> {
	private final AlbumInstallation albumInstallation;

	public CollectionUpdates(AlbumInstallation albumInstallation) {
		this.albumInstallation = albumInstallation;
	}

	public AlbumInstallation getAlbumInstallation() {
		return albumInstallation;
	}
}
