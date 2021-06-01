package mdc.ida.hips.model;

public class RemoteHIPSInstallation implements HIPSInstallation {

	private final String host;
	private final int port;

	public RemoteHIPSInstallation(String host, int port) {
		this.host = host;
		this.port = port;
	}

	@Override
	public boolean canBeLaunched() {
		return false;
	}

	@Override
	public void launch() {
		throw new RuntimeException("Cannot launch remote HIPS server.");
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
