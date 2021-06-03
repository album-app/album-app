package mdc.ida.hips.model;

import java.io.File;

public class LocalHIPSInstallation implements HIPSInstallation {

	private int port;
	private File condaPath;
	private String defaultCatalog;

	public LocalHIPSInstallation(int port, String defaultCatalog) {
		this.port = port;
		this.defaultCatalog = defaultCatalog;
	}

	@Override
	public boolean canBeLaunched() {
		return true;
	}

	@Override
	public void launch() {

	}

	@Override
	public String getHost() {
		return "http://127.0.0.1";
	}

	@Override
	synchronized public int getPort() {
		return port;
	}

	public File getCondaPath() {
		return condaPath;
	}

	public void setCondaPath(File condaPath) {
		this.condaPath = condaPath;
	}

	synchronized public void setPort(int port) {
		this.port = port;
	}

	public String getDefaultCatalog() {
		return defaultCatalog;
	}

	public void setDefaultCatalog(String defaultCatalog) {
		this.defaultCatalog = defaultCatalog;
	}
}
