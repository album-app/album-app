package mdc.ida.album.ui.javafx.viewer;

import mdc.ida.album.model.SolutionCollection;
import org.scijava.display.AbstractDisplay;
import org.scijava.display.Display;
import org.scijava.plugin.Plugin;

@Plugin(type = Display.class)
public class SolutionCollectionDisplay extends AbstractDisplay<SolutionCollection> {
	public SolutionCollectionDisplay() {
		super(SolutionCollection.class);
	}
}
