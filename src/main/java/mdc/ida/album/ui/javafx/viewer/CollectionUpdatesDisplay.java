package mdc.ida.album.ui.javafx.viewer;

import mdc.ida.album.model.CollectionUpdates;
import org.scijava.display.AbstractDisplay;
import org.scijava.display.Display;
import org.scijava.plugin.Plugin;

@Plugin(type = Display.class)
public class CollectionUpdatesDisplay extends AbstractDisplay<CollectionUpdates> {
	public CollectionUpdatesDisplay() {
		super(CollectionUpdates.class);
	}
}
