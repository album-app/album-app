package mdc.ida.hips.ui.javafx;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;
import org.scijava.ui.viewer.DisplayPanel;
import org.scijava.ui.viewer.DisplayWindow;

import java.awt.HeadlessException;

/**
 * JavaFX class implementation of the {@link DisplayWindow} interface.
 *
 */
public class HIPSDisplayWindow extends Tab implements DisplayWindow {

	private VBox root;

	public HIPSDisplayWindow() throws HeadlessException {
		root = new VBox();
		this.setContent(root);
	}

	@Override
	public void setTitle(String s) {
		setText(s);
	}

	@Override
	public void setContent(final DisplayPanel panel) {
		root.getChildren().clear();
		root.getChildren().add((Node) panel);
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
