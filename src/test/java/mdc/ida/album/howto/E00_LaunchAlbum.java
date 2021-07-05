package mdc.ida.album.howto;

import mdc.ida.album.AbstractHowto;
import mdc.ida.album.DummyServer;
import mdc.ida.album.Album;

import java.io.IOException;

public class E00_LaunchAlbum extends AbstractHowto {

//	@Test
	public void run() throws IOException {
		// use dummy server to test launcher, otherwise connect to external album server launched from Python
		int port = 1234;
		server = DummyServer.launch(port);

		// launch album
		album = new Album();
		album.launch("--port", String.valueOf(port));
	}

	public static void main(String... args) throws IOException {
		new E00_LaunchAlbum().run();
	}
}
