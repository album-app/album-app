package mdc.ida.album.howto;

import mdc.ida.album.AbstractHowto;
import mdc.ida.album.DummyServer;
import mdc.ida.album.Album;
import mdc.ida.album.model.SolutionCollection;
import mdc.ida.album.model.CollectionUpdatedEvent;
import mdc.ida.album.model.LocalAlbumInstallation;
import mdc.ida.album.model.Solution;

import java.io.IOException;

public class E02_DisplaySolution extends AbstractHowto {

//	@Test
	public void run() throws IOException {
		// use dummy server to test launcher, otherwise connect to external album server launched from Python
		int port = 1236;
		server = DummyServer.launch(port);

		// launch album
		album = new Album();
		album.launch("--port", String.valueOf(port));
		LocalAlbumInstallation installation = album.loadLocalInstallation();

		// ask for updated collection index
		album.server().updateIndex(installation, this::collectionUpdated);
	}

	public void collectionUpdated(CollectionUpdatedEvent event) {
		// once the collection is updated, display only the first solution of the first catalog
		SolutionCollection collection = event.getCollection();
		Solution firstSolution = collection.get(0).get(0);
		album.ui().show(firstSolution.getName(), firstSolution);
	}

	public static void main(String... args) throws IOException {
		new E02_DisplaySolution().run();
	}
}
