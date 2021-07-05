package mdc.ida.album;

import mdc.ida.album.model.Catalog;
import mdc.ida.album.model.SolutionCollection;
import mdc.ida.album.model.LocalAlbumInstallation;
import mdc.ida.album.model.ServerThreadDoneEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.scijava.Context;
import org.scijava.event.EventHandler;
import org.scijava.log.LogLevel;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AlbumIntegrationTest {

	@TempDir
	File folder;

	private Album album;
	private CompletableFuture<ServerThreadDoneEvent> futureAlbumServerThread;

	@BeforeEach
	void setUp() {
		album = new Album(new Context());
		album.log().setLevel(LogLevel.INFO);
		album.setHeadless();
	}

	@AfterEach
	void tearDown() {
		album.dispose();
	}

	@Test
	void installCondaCreateEnvironment() throws IOException, InterruptedException, ExecutionException {
		File path = new File(folder, "miniconda");
		path.mkdirs();

		album.conda().setDefaultCondaPath(path);

		LocalAlbumInstallation installation = album.server().loadLocalInstallation();
		assertNotNull(installation);
		assertEquals(path, installation.getCondaPath());

		// test installing conda
		assertFalse(album.server().checkIfCondaInstalled(installation));
		assertFalse(installation.isCondaInstalled());
		album.server().installConda(installation);
		assertTrue(album.server().checkIfCondaInstalled(installation));
		assertTrue(installation.isCondaInstalled());

		// create environment
//		File envFile = folder.newFile("test.yml");
//		FileUtils.writeStringToFile(envFile,
//				"name: test\n" +
//				"channels:\n" +
//				"  - defaults\n" +
//				"dependencies:\n" +
//				"  - python=3.6\n", Charset.defaultCharset());
//		assertFalse(album.conda().checkIfEnvironmentExists(path, "album"));
		assertFalse(album.server().checkIfAlbumEnvironmentExists(installation));
		assertFalse(installation.isHasAlbumEnvironment());
		album.server().createAlbumEnvironment(installation);
		assertTrue(album.server().checkIfAlbumEnvironmentExists(installation));
		assertTrue(installation.isHasAlbumEnvironment());

		assertFalse(installation.isServerRunning());
		futureAlbumServerThread = new CompletableFuture<>();
		album.event().subscribe(this);
		album.server().runAsynchronously(installation);
		ServerThreadDoneEvent serverThreadDone = futureAlbumServerThread.get();

		assertTrue(serverThreadDone.isSuccess());
		assertTrue(album.server().checkIfRunning(installation));
		assertTrue(installation.isServerRunning());

		CompletableFuture<SolutionCollection> futureCollectionReturned = new CompletableFuture<>();
		album.server().updateIndex(installation, e -> futureCollectionReturned.complete(e.getCollection()));
		SolutionCollection collection = futureCollectionReturned.get();
		assertNotNull(collection);
		assertTrue(collection.size() > 0);
		Catalog catalog = collection.get(0);
		assertNotNull(catalog);

		//TODO add album test solution to default catalog
//		assertTrue(catalog.size() > 0);
//		Solution solution = catalog.get(0);
//		assertNotNull(solution);
//		album.server().launchSolution(solution);
	}

	@EventHandler
	void albumServerDone(ServerThreadDoneEvent e) {
		futureAlbumServerThread.complete(e);
	}
}
