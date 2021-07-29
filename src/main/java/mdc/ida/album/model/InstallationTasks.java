package mdc.ida.album.model;

import java.util.HashMap;

public class InstallationTasks extends HashMap<Integer, Task> {
	private AlbumInstallation installation;

	public InstallationTasks(AlbumInstallation installation) {
		this.installation = installation;
	}
}
