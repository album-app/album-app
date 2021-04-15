package mdc.ida.hips.howto;

import mdc.ida.hips.AbstractHowto;
import mdc.ida.hips.DummyServer;
import mdc.ida.hips.HIPS;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;

public class E00_LaunchHIPS extends AbstractHowto {

	@Test
	public void run() throws IOException {
		// use dummy server to test launcher, otherwise connect to external HIPS server launched from Python
		int port = 1234;
		server = DummyServer.launch(port);

		// launch HIPS launcher
		hips = new HIPS();
		hips.launch("--port", String.valueOf(port));
	}

	public static void main(String... args) throws IOException {
		new E00_LaunchHIPS().run();
	}
}
