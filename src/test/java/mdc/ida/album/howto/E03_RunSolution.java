package mdc.ida.album.howto;

import mdc.ida.album.AbstractHowto;
import mdc.ida.album.DummyServer;
import mdc.ida.album.Album;
import mdc.ida.album.model.Catalog;
import mdc.ida.album.model.LocalAlbumInstallation;
import mdc.ida.album.model.Solution;

import java.io.IOException;
import java.util.Optional;

public class E03_RunSolution extends AbstractHowto {

//	@Test
	public void run() throws IOException {
		// use dummy server to test launcher, otherwise connect to external album server launched from Python
		int port = 1237;
		server = DummyServer.launch(port);

		// launch album
		album = new Album();
		album.launch();
		LocalAlbumInstallation installation = album.loadLocalInstallation("--port", String.valueOf(port));

		// ask for updated collection index
		album.server().updateIndex(installation, event -> {
			// once the collection is updated, find and run one solution
			Catalog catalog = event.getCollection().get(0);
			Optional<Solution> imageJDisplay = catalog.stream().filter(solution -> solution.getName().equals("imagej-display")).findFirst();
			imageJDisplay.ifPresent(solution -> album.server().launchSolution(installation, solution, "run"));
		});
	}

	public static void main(String... args) throws IOException {
		new E03_RunSolution().run();
	}
}
