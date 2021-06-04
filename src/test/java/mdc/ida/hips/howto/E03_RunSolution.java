package mdc.ida.hips.howto;

import mdc.ida.hips.AbstractHowto;
import mdc.ida.hips.DummyServer;
import mdc.ida.hips.HIPS;
import mdc.ida.hips.model.HIPSCatalog;
import mdc.ida.hips.model.HIPSolution;
import mdc.ida.hips.model.LocalHIPSInstallation;

import java.io.IOException;
import java.util.Optional;

public class E03_RunSolution extends AbstractHowto {

//	@Test
	public void run() throws IOException {
		// use dummy server to test launcher, otherwise connect to external HIPS server launched from Python
		int port = 1237;
		server = DummyServer.launch(port);

		// launch HIPS launcher
		hips = new HIPS();
		hips.launch();
		LocalHIPSInstallation installation = hips.loadLocalInstallation("--port", String.valueOf(port));

		// ask for updated collection index
		hips.server().updateIndex(installation, event -> {
			// once the collection is updated, find and run one solution
			HIPSCatalog catalog = event.getCollection().get(0);
			Optional<HIPSolution> imageJDisplay = catalog.stream().filter(solution -> solution.getName().equals("imagej-display")).findFirst();
			imageJDisplay.ifPresent(solution -> hips.server().launchSolution(installation, solution));
		});
	}

	public static void main(String... args) throws IOException {
		new E03_RunSolution().run();
	}
}
