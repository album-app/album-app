package mdc.ida.album.model.event;

import org.scijava.event.SciJavaEvent;

import java.io.File;

public class CondaEnvironmentDetectedEvent extends SciJavaEvent {
	private final String environmentName;
	private final File condaPath;

	public CondaEnvironmentDetectedEvent(File condaPath, String environmentName) {
		this.condaPath = condaPath;
		this.environmentName = environmentName;
	}

	public String getEnvironmentName() {
		return environmentName;
	}

	public File getCondaPath() {
		return condaPath;
	}
}
