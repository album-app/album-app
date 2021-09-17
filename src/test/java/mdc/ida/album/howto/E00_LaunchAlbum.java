package mdc.ida.album.howto;

import mdc.ida.album.AbstractHowto;
import mdc.ida.album.Album;

public class E00_LaunchAlbum extends AbstractHowto {

//	@Test
	public void run() {
		// launch album
		album = new Album();
		album.launch();
	}

	public static void main(String... args) {
		new E00_LaunchAlbum().run();
	}
}
