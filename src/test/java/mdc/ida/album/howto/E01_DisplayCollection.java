package mdc.ida.album.howto;

import mdc.ida.album.AbstractHowto;
import mdc.ida.album.Album;

public class E01_DisplayCollection extends AbstractHowto {

//	@Test
	public void run() {
		// launch album
		album = new Album();
		album.launch();
		album.loadLocalInstallation();
	}

	public static void main(String... args) {
		new E01_DisplayCollection().run();
	}
}
