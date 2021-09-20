package mdc.ida.album.service;

import mdc.ida.album.model.AlbumInstallation;
import mdc.ida.album.model.Catalog;
import mdc.ida.album.model.CatalogListUpdatedEvent;
import mdc.ida.album.model.CollectionUpdatedEvent;
import mdc.ida.album.model.LocalAlbumInstallation;
import mdc.ida.album.model.LocalInstallationLoadedEvent;
import mdc.ida.album.model.RecentlyInstalledUpdatedEvent;
import mdc.ida.album.model.RecentlyLaunchedUpdatedEvent;
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
		return checkIfRunning(installation, e -> {});
	}
	boolean checkIfRunning(LocalAlbumInstallation installation, Consumer<LocalInstallationLoadedEvent> callbackIfRunning);
	void launchSolution(AlbumInstallation installation, Solution solution, String action) throws IOException;
	void updateIndex(LocalAlbumInstallation installation, Consumer<CollectionUpdatedEvent> callback) throws IOException;
	void updateCatalogList(AlbumInstallation installation, Consumer<CatalogListUpdatedEvent> callback) throws IOException;
	boolean checkIfAlbumEnvironmentExists(LocalAlbumInstallation installation);
	default void runAsynchronously(LocalAlbumInstallation installation) {
		runAsynchronously(installation, e -> {});
	}
	void runAsynchronously(LocalAlbumInstallation installation, Consumer<LocalInstallationLoadedEvent> callback);
	void runWithChecks(LocalAlbumInstallation installation);
	void runWithChecks(LocalAlbumInstallation installation, Consumer<LocalInstallationLoadedEvent> callback);
	File getEnvironmentFile() throws IOException;
	void addCatalog(LocalAlbumInstallation installation, String urlOrPath) throws IOException;
	boolean checkIfCondaInstalled(LocalAlbumInstallation installation);

	void installConda(LocalAlbumInstallation installation) throws IOException;

	String getAlbumEnvironmentPath(LocalAlbumInstallation installation);

	void createAlbumEnvironment(LocalAlbumInstallation installation) throws IOException, InterruptedException;

	void removeAlbumEnvironment(LocalAlbumInstallation installation) throws IOException, InterruptedException;

	ServerProperties getServerProperties(LocalAlbumInstallation installation) throws IOException;

	void shutdownServer(AlbumInstallation installation);

	void removeCatalog(AlbumInstallation installation, Catalog catalog) throws IOException;
	void addCatalog(AlbumInstallation installation, String urlOrPath) throws IOException;

	void updateRecentlyLaunchedSolutionsList(LocalAlbumInstallation installation, Consumer<RecentlyLaunchedUpdatedEvent> callback) throws IOException;
	void updateRecentlyInstalledSolutionsList(LocalAlbumInstallation installation, Consumer<RecentlyInstalledUpdatedEvent> callback) throws IOException;
}
