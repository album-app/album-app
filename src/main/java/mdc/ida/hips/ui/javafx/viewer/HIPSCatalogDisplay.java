package mdc.ida.hips.ui.javafx.viewer;

import mdc.ida.hips.model.HIPSCatalog;
import org.scijava.display.AbstractDisplay;
import org.scijava.display.Display;
import org.scijava.plugin.Plugin;

@Plugin(type = Display.class)
public class HIPSCatalogDisplay extends AbstractDisplay<HIPSCatalog> {
	public HIPSCatalogDisplay() {
		super(HIPSCatalog.class);
	}
}
