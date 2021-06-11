package mdc.ida.hips.scijava.ui.javafx.viewer;

import javafx.scene.layout.VBox;
import org.scijava.ui.viewer.DisplayPanel;

/**
 * An JavaFX panel for displaying data
 *
 * @param <W> the type of {@link VBox} housing the panel
 */
public interface JavaFXDisplayPanel<W extends VBox> extends DisplayPanel {

    default void redraw() {
        // TODO
    }

}
