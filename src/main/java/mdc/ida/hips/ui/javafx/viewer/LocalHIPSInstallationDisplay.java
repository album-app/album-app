package mdc.ida.hips.ui.javafx.viewer;

import mdc.ida.hips.model.LocalHIPSInstallation;
import org.scijava.display.AbstractDisplay;
import org.scijava.display.Display;
import org.scijava.plugin.Plugin;

@Plugin(type = Display.class)
public class LocalHIPSInstallationDisplay extends AbstractDisplay<LocalHIPSInstallation> {
	public LocalHIPSInstallationDisplay() {
		super(LocalHIPSInstallation.class);
	}
}
