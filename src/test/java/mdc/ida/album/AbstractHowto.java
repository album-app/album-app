package mdc.ida.album;

import org.junit.jupiter.api.AfterEach;

import java.io.IOException;

public class AbstractHowto {

	protected Album album;
	protected DummyServer server;

	@AfterEach
	public void tearDown() throws IOException {
		album.context().dispose();
		server.dispose();
	}
}
