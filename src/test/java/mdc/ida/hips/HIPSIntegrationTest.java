package mdc.ida.hips;

import mdc.ida.hips.model.HIPSCatalog;
import mdc.ida.hips.model.HIPSCollection;
import mdc.ida.hips.model.HIPSServerRunningEvent;
import mdc.ida.hips.model.HIPSolution;
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
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HIPSIntegrationTest {

	@TempDir
	Path folder;

	private HIPS hips;
	private CompletableFuture<HIPSServerRunningEvent> futureHipsRunning;

	@BeforeEach
	void setUp() throws Exception {
		hips = new HIPS(new Context());
		hips.log().setLevel(LogLevel.INFO);
		hips.launchHeadless();
	}

	@AfterEach
	void tearDown() throws Exception {
		hips.dispose();
	}

	@Test
	void installCondaCreateEnvironment() throws IOException, InterruptedException, ExecutionException {
		File path = new File(folder.toFile(), "miniconda");
		path.mkdirs();

		// test installing conda
		assertFalse(hips.conda().checkIfCondaInstalled(path));
		hips.conda().installConda(path);
		assertTrue(hips.conda().checkIfCondaInstalled(path));

		// create environment
//		File envFile = folder.newFile("test.yml");
//		FileUtils.writeStringToFile(envFile,
//				"name: test\n" +
//				"channels:\n" +
//				"  - defaults\n" +
//				"dependencies:\n" +
//				"  - python=3.6\n", Charset.defaultCharset());
		assertFalse(hips.conda().checkIfEnvironmentExists(path, "hips"));
//		hips.conda().createEnvironment(path, envFile);
		hips.conda().createEnvironment(path, hips.server().getEnvironmentFile());
		assertTrue(hips.conda().checkIfEnvironmentExists(path, "hips"));

		hips.conda().setDefaultCondaPath(path);

		LocalHIPSInstallation installation = hips.server().loadLocalInstallation();
		assertNotNull(installation);

		assertEquals(path, installation.getCondaPath());

		futureHipsRunning = new CompletableFuture<>();
		hips.event().subscribe(this);
		hips.server().runAsynchronously(installation);
		futureHipsRunning.get();

		assertTrue(hips.server().checkIfRunning(installation));

		CompletableFuture<HIPSCollection> futureCollectionReturned = new CompletableFuture<>();
		hips.server().updateIndex(e -> futureCollectionReturned.complete(e.getCollection()));
		HIPSCollection collection = futureCollectionReturned.get();
		assertNotNull(collection);
		assertTrue(collection.size() > 0);
		HIPSCatalog catalog = collection.get(0);
		assertNotNull(catalog);
		assertTrue(catalog.size() > 0);

		HIPSolution solution = catalog.get(0);
		assertNotNull(solution);

		//TODO add HIPS test solution to default catalog
//		hips.server().launchSolution(solution);
	}

	@EventHandler
	void hipsStarted(HIPSServerRunningEvent e) {
		futureHipsRunning.complete(e);
	}
}
