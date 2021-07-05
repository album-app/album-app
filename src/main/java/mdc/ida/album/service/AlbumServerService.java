package mdc.ida.album.service;

import mdc.ida.album.model.AlbumInstallation;
import mdc.ida.album.model.CollectionUpdatedEvent;
import mdc.ida.album.model.LocalAlbumInstallation;
import mdc.ida.album.model.RemoteAlbumInstallation;
import mdc.ida.album.model.ServerProperties;
import mdc.ida.album.model.Solution;
import org.scijava.service.SciJavaService;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public interface AlbumServerService extends SciJavaService {
	LocalAlbumInstallation loadLocalInstallation();
	RemoteAlbumInstallation loadRemoteInstallation(String url, int port);
	default boolean checkIfRunning(LocalAlbumInstallation installation) {
		return checkIfRunning(installation, () -> {});
	}
	boolean checkIfRunning(LocalAlbumInstallation installation, Runnable callbackIfRunning);
	void launchSolution(AlbumInstallation installation, Solution solution, String action);
	void updateIndex(LocalAlbumInstallation installation, Consumer<CollectionUpdatedEvent> callback) throws IOException;
	boolean checkIfAlbumEnvironmentExists(LocalAlbumInstallation installation);
	default void runAsynchronously(LocalAlbumInstallation installation) {
		runAsynchronously(installation, () -> {});
	}
	void runAsynchronously(LocalAlbumInstallation installation, Runnable callback);
	void runWithChecks(LocalAlbumInstallation installation);
	File getEnvironmentFile() throws IOException;
	void addCatalog(LocalAlbumInstallation installation, String urlOrPath) throws IOException;
	boolean checkIfCondaInstalled(LocalAlbumInstallation installation);

	void installConda(LocalAlbumInstallation installation) throws IOException;

	void createAlbumEnvironment(LocalAlbumInstallation installation) throws IOException, InterruptedException;

	ServerProperties getServerProperties(LocalAlbumInstallation installation) throws IOException;

	String getAlbumEnvironmentPath(LocalAlbumInstallation installation);

	void shutdownServer(AlbumInstallation installation);

	void removeAlbumEnvironment(LocalAlbumInstallation installation) throws IOException, InterruptedException;
}
