package mdc.ida.album.ui.javafx.viewer;

import mdc.ida.album.model.Catalog;
import org.scijava.display.AbstractDisplay;
import org.scijava.display.Display;
import org.scijava.plugin.Plugin;

@Plugin(type = Display.class)
public class CatalogDisplay extends AbstractDisplay<Catalog> {
	public CatalogDisplay() {
		super(Catalog.class);
	}
}
