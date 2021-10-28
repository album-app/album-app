package mdc.ida.album.scijava.ui.javafx.widget;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.scijava.widget.AbstractInputPanel;
import org.scijava.widget.InputWidget;
import org.scijava.widget.WidgetModel;

public class JavaFXInputPanel extends AbstractInputPanel<VBox, HBox> {

	private VBox uiComponent;

	// -- InputPanel methods --

	@Override
	public void addWidget(final InputWidget<?, HBox> widget) {
		super.addWidget(widget);
		final HBox widgetPane = widget.getComponent();
		final WidgetModel model = widget.get();

		// add widget to panel
		if (widget.isLabeled()) {
			// widget is prefixed by a label
			final Label l = new Label(model.getWidgetLabel());
			final String desc = model.getItem().getDescription();
//			if (desc != null && !desc.isEmpty()) l.setToolTipText(desc);
			getComponent().getChildren().add(l);
			getComponent().getChildren().add(widgetPane);
		}
		else {
			// widget occupies entire row
			getComponent().getChildren().add(widgetPane);
		}
	}

	@Override
	public Class<HBox> getWidgetComponentType() {
		return HBox.class;
	}

	// -- UIComponent methods --

	@Override
	public VBox getComponent() {
		if (uiComponent == null) {
			uiComponent = new VBox();
		}
		return uiComponent;
	}

	@Override
	public Class<VBox> getComponentType() {
		return VBox.class;
	}

}
