package mdc.ida.album.model;

import java.util.ArrayList;

public class SolutionCollection extends ArrayList<Catalog> {
	AlbumInstallation installation;

	public SolutionCollection(AlbumInstallation installation) {
		this.installation = installation;
	}

	public AlbumInstallation getInstallation() {
		return installation;
	}
}
