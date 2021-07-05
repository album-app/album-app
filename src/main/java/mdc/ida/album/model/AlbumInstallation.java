package mdc.ida.album.model;

public interface AlbumInstallation {
	boolean canBeLaunched();
	void launch();
	String getHost();
	int getPort();
}
