package mdc.ida.album.model;

public class RemoteAlbumInstallation implements AlbumInstallation {

	private final String host;
	private final int port;

	public RemoteAlbumInstallation(String host, int port) {
		this.host = host;
		this.port = port;
	}

	@Override
	public boolean canBeLaunched() {
		return false;
	}

	@Override
	public void launch() {
		throw new RuntimeException("Cannot launch remote album server.");
	}

	@Override
	public String getHost() {
		return host;
	}

	@Override
	public int getPort() {
		return port;
	}
}
