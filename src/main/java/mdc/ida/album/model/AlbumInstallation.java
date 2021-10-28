package mdc.ida.album.model;

public interface AlbumInstallation {
	boolean canBeLaunched();
	String getHost();
	int getPort();
	InstallationTasks getTasks();
	void setProperties(ServerProperties properties);
	ServerProperties getProperties();
}
