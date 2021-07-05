package mdc.ida.album.ui.javafx.viewer;

import mdc.ida.album.model.LocalAlbumInstallation;
import org.scijava.display.AbstractDisplay;
import org.scijava.display.Display;
import org.scijava.plugin.Plugin;

@Plugin(type = Display.class)
public class LocalAlbumInstallationDisplay extends AbstractDisplay<LocalAlbumInstallation> {
	public LocalAlbumInstallationDisplay() {
		super(LocalAlbumInstallation.class);
	}
}
