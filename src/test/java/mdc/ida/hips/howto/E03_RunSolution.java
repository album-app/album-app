package mdc.ida.hips.howto;

import mdc.ida.hips.AbstractHowto;
import mdc.ida.hips.DummyServer;
import mdc.ida.hips.HIPS;
import mdc.ida.hips.model.HIPSCollection;
import mdc.ida.hips.model.HIPSCollectionUpdatedEvent;
import mdc.ida.hips.model.HIPSolution;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.util.Optional;

public class E03_RunSolution extends AbstractHowto {

	@Test
	public void run() throws IOException {
		// use dummy server to test launcher, otherwise connect to external HIPS server launched from Python
		int port = 1237;
		server = DummyServer.launch(port);

		// launch HIPS launcher
		hips = new HIPS();
		hips.launch("--port", String.valueOf(port));

		// ask for updated collection index
		hips.server().updateIndex(this::collectionUpdated);
	}

	public void collectionUpdated(HIPSCollectionUpdatedEvent event) {
		// once the collection is updated, find and run one solution
		HIPSCollection collection = event.getCollection();
		Optional<HIPSolution> imageJDisplay = collection.stream().filter(solution -> solution.getName().equals("imagej-display")).findFirst();
		imageJDisplay.ifPresent(solution -> hips.server().launchSolution(solution));
	}

	public static void main(String... args) throws IOException {
		new E03_RunSolution().run();
	}
}
