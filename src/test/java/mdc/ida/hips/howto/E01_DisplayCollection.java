package mdc.ida.hips.howto;

import mdc.ida.hips.AbstractHowto;
import mdc.ida.hips.DummyServer;
import mdc.ida.hips.HIPS;
import mdc.ida.hips.model.HIPSCollectionUpdatedEvent;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;

public class E01_DisplayCollection extends AbstractHowto {

	@Test
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
		// once the collection is updated, display it
		hips.ui().show("HIPS Collection", event.getCollection());
	}

	public static void main(String... args) throws IOException {
		new E01_DisplayCollection().run();
	}
}
