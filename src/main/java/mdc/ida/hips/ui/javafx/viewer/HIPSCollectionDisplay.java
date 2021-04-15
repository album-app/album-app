package mdc.ida.hips.ui.javafx.viewer;

import mdc.ida.hips.model.HIPSCollection;
import org.scijava.display.AbstractDisplay;
import org.scijava.display.Display;
import org.scijava.plugin.Plugin;

@Plugin(type = Display.class)
public class HIPSCollectionDisplay extends AbstractDisplay<HIPSCollection> {
	public HIPSCollectionDisplay() {
		super(HIPSCollection.class);
	}
}
