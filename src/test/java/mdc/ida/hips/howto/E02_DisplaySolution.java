package mdc.ida.hips.howto;

import mdc.ida.hips.AbstractHowto;
import mdc.ida.hips.DummyServer;
import mdc.ida.hips.HIPS;
import mdc.ida.hips.model.HIPSCollection;
import mdc.ida.hips.model.HIPSCollectionUpdatedEvent;
import mdc.ida.hips.model.HIPSolution;

import java.io.IOException;

public class E02_DisplaySolution extends AbstractHowto {

//	@Test
	public void run() throws IOException {
		// use dummy server to test launcher, otherwise connect to external HIPS server launched from Python
		int port = 1236;
		server = DummyServer.launch(port);

		// launch HIPS launcher
		hips = new HIPS();
		hips.launch("--port", String.valueOf(port));

		// ask for updated collection index
		hips.server().updateIndex(this::collectionUpdated);
	}

	public void collectionUpdated(HIPSCollectionUpdatedEvent event) {
		// once the collection is updated, display only the first solution of the first catalog
		HIPSCollection collection = event.getCollection();
		HIPSolution firstSolution = collection.get(0).get(0);
		hips.ui().show(firstSolution.getName(), firstSolution);
	}

	public static void main(String... args) throws IOException {
		new E02_DisplaySolution().run();
	}
}
