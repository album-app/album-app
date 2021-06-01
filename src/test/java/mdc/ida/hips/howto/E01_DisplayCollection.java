package mdc.ida.hips.howto;

import mdc.ida.hips.AbstractHowto;
import mdc.ida.hips.DummyServer;
import mdc.ida.hips.HIPS;
import mdc.ida.hips.model.HIPSCatalog;
import mdc.ida.hips.model.HIPSCollection;
import mdc.ida.hips.model.HIPSCollectionUpdatedEvent;

import java.io.IOException;

public class E01_DisplayCollection extends AbstractHowto {

//	@Test
	public void run() throws IOException {
		// use dummy server to test launcher, otherwise connect to external HIPS server launched from Python
		int port = 1235;
		server = DummyServer.launch(port);

		// launch HIPS launcher
		hips = new HIPS();
		hips.launch("--port", String.valueOf(port));

		// ask for updated collection index
		hips.server().updateIndex(this::collectionUpdated);
	}

	public void collectionUpdated(HIPSCollectionUpdatedEvent event) {
		// once the collection is updated, display all catalogs
		HIPSCollection collection = event.getCollection();
		for (HIPSCatalog catalog : collection) {
			hips.ui().show(catalog.getName(), catalog);
		}
	}

	public static void main(String... args) throws IOException {
		new E01_DisplayCollection().run();
	}
}
