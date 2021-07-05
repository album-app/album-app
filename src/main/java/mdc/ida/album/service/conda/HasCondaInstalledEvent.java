package mdc.ida.album.service.conda;

import org.scijava.event.SciJavaEvent;

import java.io.File;

public class HasCondaInstalledEvent extends SciJavaEvent {
	private final File condaPath;

	public HasCondaInstalledEvent(File condaPath) {
		this.condaPath = condaPath;
	}

	public File getCondaPath() {
		return condaPath;
	}
}
