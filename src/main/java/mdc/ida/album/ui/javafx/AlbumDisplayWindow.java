package mdc.ida.album.ui.javafx;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Tab;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import mdc.ida.album.ui.javafx.viewer.LocalAlbumInstallationDisplay;
import org.scijava.ui.viewer.DisplayPanel;
import org.scijava.ui.viewer.DisplayWindow;

/**
 * JavaFX class implementation of the {@link DisplayWindow} interface.
 *
 */
public class AlbumDisplayWindow extends Tab implements DisplayWindow {

	private VBox root;

	public AlbumDisplayWindow() {
		root = new VBox();
		VBox.setVgrow(root, Priority.ALWAYS);
		this.setContent(root);
	}

	@Override
	public void setTitle(String s) {
		setText(s);
	}

	@Override
	public void setContent(final DisplayPanel panel) {
		VBox p = (VBox) panel;
		root = new VBox(p);
		VBox.setVgrow(root, Priority.ALWAYS);
		this.setContent(root);

		//TODO this is bad design
		if(LocalAlbumInstallationDisplay.class.isAssignableFrom(panel.getDisplay().getClass())) {
			setClosable(false);
		}
	}

	@Override
	public void pack() {
	}

	@Override
	public void showDisplay(final boolean visible) {
		if (visible) pack();
	}

	@Override
	public void requestFocus() {
		this.getTabPane().getSelectionModel().select(this);
	}

	@Override
	public void close() {
		EventHandler<Event> handler = getOnClosed();
		if (null != handler) {
			handler.handle(null);
		} else {
			Platform.runLater(() -> {
				getTabPane().getTabs().remove(this);
			});
		}
	}

	@Override
	public int findDisplayContentScreenX() {
		// TODO
		return 0;
	}

	@Override
	public int findDisplayContentScreenY() {
		// TODO
		return 0;
	}
}
