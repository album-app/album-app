package mdc.ida.hips.ui.javafx.viewer;

import mdc.ida.hips.model.HIPSolution;
import org.scijava.display.AbstractDisplay;
import org.scijava.display.Display;
import org.scijava.plugin.Plugin;

@Plugin(type = Display.class)
public class HIPSolutionDisplay extends AbstractDisplay<HIPSolution> {
	public HIPSolutionDisplay() {
		super(HIPSolution.class);
	}
}
