package mdc.ida.hips;

import mdc.ida.hips.model.HIPSCatalog;
import mdc.ida.hips.model.HIPSCollection;
import mdc.ida.hips.model.HIPSServerThreadDoneEvent;
import mdc.ida.hips.model.LocalHIPSInstallation;
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

public class HIPSIntegrationTest {

	@TempDir
	File folder;

	private HIPS hips;
	private CompletableFuture<HIPSServerThreadDoneEvent> futureHipsServerThread;

	@BeforeEach
	void setUp() {
		hips = new HIPS(new Context());
		hips.log().setLevel(LogLevel.INFO);
		hips.setHeadless();
	}

	@AfterEach
	void tearDown() {
		hips.dispose();
	}

	@Test
	void installCondaCreateEnvironment() throws IOException, InterruptedException, ExecutionException {
		File path = new File(folder, "miniconda");
		path.mkdirs();

		hips.conda().setDefaultCondaPath(path);

		LocalHIPSInstallation installation = hips.server().loadLocalInstallation();
		assertNotNull(installation);
		assertEquals(path, installation.getCondaPath());

		// test installing conda
		assertFalse(hips.server().checkIfCondaInstalled(installation));
		assertFalse(installation.isCondaInstalled());
		hips.server().installConda(installation);
		assertTrue(hips.server().checkIfCondaInstalled(installation));
		assertTrue(installation.isCondaInstalled());

		// create environment
//		File envFile = folder.newFile("test.yml");
//		FileUtils.writeStringToFile(envFile,
//				"name: test\n" +
//				"channels:\n" +
//				"  - defaults\n" +
//				"dependencies:\n" +
//				"  - python=3.6\n", Charset.defaultCharset());
//		assertFalse(hips.conda().checkIfEnvironmentExists(path, "hips"));
		assertFalse(hips.server().checkIfHIPSEnvironmentExists(installation));
		assertFalse(installation.isHasHipsEnvironment());
		hips.server().createHIPSEnvironment(installation);
		assertTrue(hips.server().checkIfHIPSEnvironmentExists(installation));
		assertTrue(installation.isHasHipsEnvironment());

		assertFalse(installation.isServerRunning());
		futureHipsServerThread = new CompletableFuture<>();
		hips.event().subscribe(this);
		hips.server().runAsynchronously(installation);
		HIPSServerThreadDoneEvent serverThreadDone = futureHipsServerThread.get();

		assertTrue(serverThreadDone.isSuccess());
		assertTrue(hips.server().checkIfRunning(installation));
		assertTrue(installation.isServerRunning());

		CompletableFuture<HIPSCollection> futureCollectionReturned = new CompletableFuture<>();
		hips.server().updateIndex(installation, e -> futureCollectionReturned.complete(e.getCollection()));
		HIPSCollection collection = futureCollectionReturned.get();
		assertNotNull(collection);
		assertTrue(collection.size() > 0);
		HIPSCatalog catalog = collection.get(0);
		assertNotNull(catalog);

		//TODO add HIPS test solution to default catalog
//		assertTrue(catalog.size() > 0);
//		HIPSolution solution = catalog.get(0);
//		assertNotNull(solution);
//		hips.server().launchSolution(solution);
	}

	@EventHandler
	void hipsServerDone(HIPSServerThreadDoneEvent e) {
		futureHipsServerThread.complete(e);
	}
}
