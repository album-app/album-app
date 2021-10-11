package mdc.ida.album.howto;

import mdc.ida.album.AbstractHowto;
import mdc.ida.album.Album;
import mdc.ida.album.model.event.CollectionIndexEvent;
import mdc.ida.album.model.event.LocalInstallationLoadedEvent;
import mdc.ida.album.model.Solution;
import mdc.ida.album.model.SolutionCollection;
import org.scijava.event.EventHandler;

import java.io.IOException;

public class E02_DisplaySolution extends AbstractHowto {

//	@Test
	public void run() {

		// launch album
		album = new Album();
		album.launch();
		album.loadLocalInstallation(this::installationLoaded);

	}

	private void installationLoaded(LocalInstallationLoadedEvent event) {
		// ask for updated collection index
		try {
			album.server().index(event.getInstallation(), this::collectionUpdated);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void collectionUpdated(CollectionIndexEvent event) {
		// once the collection is updated, display only the first solution of the first catalog
		SolutionCollection collection = event.getCollection();
		Solution firstSolution = collection.get(0).get(0);
		album.ui().show(firstSolution.getName(), firstSolution);
	}

	public static void main(String... args) {
		new E02_DisplaySolution().run();
	}
}
