package mdc.ida.album.model;

import java.io.File;

public class LocalAlbumInstallation implements AlbumInstallation {

	private int port;
	private File condaPath;
	private String defaultCatalog;

	private boolean serverRunning;
	private boolean condaInstalled; // conda executable exists
	private boolean condaMissing; // conda
	private boolean hasAlbumEnvironment;
	private final InstallationTasks tasks;
	private ServerProperties properties;

	public LocalAlbumInstallation(int port) {
		this.port = port;
		tasks = new InstallationTasks(this);
	}

	public LocalAlbumInstallation(int port, String defaultCatalog) {
		this(port);
		this.defaultCatalog = defaultCatalog;
	}

	@Override
	public boolean canBeLaunched() {
		return true;
	}

	@Override
	public String getHost() {
		return "http://127.0.0.1";
	}

	@Override
	synchronized public int getPort() {
		return port;
	}

	@Override
	public InstallationTasks getTasks() {
		return tasks;
	}

	@Override
	public void setProperties(ServerProperties properties) {
		this.properties = properties;
	}

	@Override
	public ServerProperties getProperties() {
		return properties;
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

	public boolean isServerRunning() {
		return serverRunning;
	}

	public void setServerRunning(boolean serverRunning) {
		this.serverRunning = serverRunning;
	}

	public boolean isCondaInstalled() {
		return condaInstalled;
	}

	public void setCondaInstalled(boolean condaInstalled) {
		this.condaInstalled = condaInstalled;
	}

	public boolean isCondaMissing() {
		return condaMissing;
	}

	public void setCondaMissing(boolean condaMissing) {
		this.condaMissing = condaMissing;
	}

	public boolean isHasAlbumEnvironment() {
		return hasAlbumEnvironment;
	}

	public void setHasAlbumEnvironment(boolean hasAlbumEnvironment) {
		this.hasAlbumEnvironment = hasAlbumEnvironment;
	}
}
