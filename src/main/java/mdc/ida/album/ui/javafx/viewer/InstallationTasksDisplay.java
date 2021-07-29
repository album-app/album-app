package mdc.ida.album.ui.javafx.viewer;

import mdc.ida.album.model.InstallationTasks;
import org.scijava.display.AbstractDisplay;
import org.scijava.display.Display;
import org.scijava.plugin.Plugin;

@Plugin(type = Display.class)
public class InstallationTasksDisplay extends AbstractDisplay<InstallationTasks> {
	public InstallationTasksDisplay() {
		super(InstallationTasks.class);
	}
}
