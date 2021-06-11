package mdc.ida.hips;

import org.junit.jupiter.api.AfterEach;

import java.io.IOException;

public class AbstractHowto {

	protected HIPS hips;
	protected DummyServer server;

	@AfterEach
	public void tearDown() throws IOException {
		hips.context().dispose();
		server.dispose();
	}
}
