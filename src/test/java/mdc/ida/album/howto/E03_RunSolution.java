package mdc.ida.album.howto;

import mdc.ida.album.AbstractHowto;
import mdc.ida.album.Album;
import mdc.ida.album.model.Catalog;
import mdc.ida.album.model.LocalInstallationLoadedEvent;
import mdc.ida.album.model.Solution;

import java.io.IOException;
import java.util.Optional;

public class E03_RunSolution extends AbstractHowto {

//	@Test
	public void run() {
		// launch album
		album = new Album();
		album.launch();
		album.loadLocalInstallation(this::installationLoaded);
	}

	private void installationLoaded(LocalInstallationLoadedEvent e) {
		// ask for updated collection index
		try {
			album.server().updateIndex(e.getInstallation(), event -> {
				// once the collection is updated, find and run one solution
				Catalog catalog = event.getCollection().get(0);
				Optional<Solution> imageJDisplay = catalog.stream().filter(solution -> solution.getName().equals("imagej-display")).findFirst();
				imageJDisplay.ifPresent(solution -> {
					try {
						album.server().launchSolution(e.getInstallation(), solution, "run");
					} catch (IOException ioException) {
						ioException.printStackTrace();
					}
				});
			});
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	public static void main(String... args) {
		new E03_RunSolution().run();
	}
}
