package mdc.ida.album.control;

import mdc.ida.album.model.AlbumInstallation;
import mdc.ida.album.model.Catalog;
import mdc.ida.album.model.LocalAlbumInstallation;
import mdc.ida.album.model.RemoteAlbumInstallation;
import mdc.ida.album.model.ServerProperties;
import mdc.ida.album.model.Solution;
import mdc.ida.album.model.event.CatalogListEvent;
import mdc.ida.album.model.event.CollectionIndexEvent;
import mdc.ida.album.model.event.CollectionUpgradeEvent;
import mdc.ida.album.model.event.CollectionUpgradePreviewEvent;
import mdc.ida.album.model.event.LocalInstallationLoadedEvent;
import mdc.ida.album.model.event.RecentlyInstalledUpdatedEvent;
import mdc.ida.album.model.event.RecentlyLaunchedUpdatedEvent;
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
	void index(LocalAlbumInstallation installation, Consumer<CollectionIndexEvent> callback) throws IOException;
	void catalogList(AlbumInstallation installation, Consumer<CatalogListEvent> callback) throws IOException;
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

	void updateRecentlyLaunchedSolutionsList(AlbumInstallation installation, Consumer<RecentlyLaunchedUpdatedEvent> callback) throws IOException;
	void updateRecentlyInstalledSolutionsList(AlbumInstallation installation, Consumer<RecentlyInstalledUpdatedEvent> callback) throws IOException;

	void update(AlbumInstallation installation) throws IOException;

	void upgrade(AlbumInstallation installation, Consumer<CollectionUpgradeEvent> callback) throws IOException;
	void upgradeDryRun(AlbumInstallation installation, Consumer<CollectionUpgradePreviewEvent> callback) throws IOException;

	String getCoverPath(Solution solution, LocalAlbumInstallation installation);
}
