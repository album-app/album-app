package mdc.ida.album.utils;

import org.apache.commons.io.IOUtils;
import org.scijava.log.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamGobbler extends Thread {
	private final Logger logger;
	protected boolean streamClosed = false;
	InputStream is;
	private boolean failed = false;

	// reads everything from is until empty.
	public StreamGobbler(InputStream is, Logger logger) {
		this.logger = logger;
		this.is = is;
	}

	public void run() {
		try(InputStreamReader isr = new InputStreamReader(is);
		    BufferedReader br = new BufferedReader(isr);) {
			String line;
			while ( !streamClosed && (line = br.readLine()) != null) {
				handleLog(line);
			}

		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				IOUtils.close(is);
			} catch (IOException ignored) {
			}
		}
	}

	public void handleLog(String line) throws RuntimeException {
		if(line.contains("ERROR") || line.contains("Error")) {
			failed = true;
			throw new RuntimeException(line);
		} else {
			if(line.contains("WARNING")) {
				logger.warn(line);
			} else {
				logger.info(line);
			}
		}
	}

	public boolean failed() {
		return failed;
	}
}
