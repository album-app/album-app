package mdc.ida.album.ui.javafx.viewer;

import mdc.ida.album.model.Solution;
import org.scijava.display.AbstractDisplay;
import org.scijava.display.Display;
import org.scijava.plugin.Plugin;

@Plugin(type = Display.class)
public class SolutionDisplay extends AbstractDisplay<Solution> {
	public SolutionDisplay() {
		super(Solution.class);
	}
}
