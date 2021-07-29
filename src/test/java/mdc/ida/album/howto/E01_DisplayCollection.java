package mdc.ida.album.howto;

import mdc.ida.album.AbstractHowto;
import mdc.ida.album.DummyServer;
import mdc.ida.album.Album;
import mdc.ida.album.model.Catalog;
import mdc.ida.album.model.LocalInstallationLoadedEvent;
import mdc.ida.album.model.SolutionCollection;
import mdc.ida.album.model.CollectionUpdatedEvent;
import mdc.ida.album.model.LocalAlbumInstallation;

import java.io.IOException;

public class E01_DisplayCollection extends AbstractHowto {

//	@Test
	public void run() throws IOException {
		// use dummy server to test launcher, otherwise connect to external album server launched from Python
		int port = 1235;
		server = DummyServer.launch(port);

		// launch album
		album = new Album();
		album.launch("--port", String.valueOf(port));
		album.loadLocalInstallation();
		album.loadLocalInstallation(this::installationLoaded);

	}

	public void installationLoaded(LocalInstallationLoadedEvent e) {
		// once the installation is loaded, update the collection
		try {
			album.server().updateIndex(e.getInstallation(), this::collectionUpdated);
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	public void collectionUpdated(CollectionUpdatedEvent event) {
		// once the collection is updated, display it
		SolutionCollection collection = event.getCollection();
		album.ui().show("my collection", collection);
	}

	public static void main(String... args) throws IOException {
		new E01_DisplayCollection().run();
	}
}
