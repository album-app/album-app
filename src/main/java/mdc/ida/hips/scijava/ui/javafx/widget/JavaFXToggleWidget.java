package mdc.ida.hips.scijava.ui.javafx.widget;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import org.scijava.plugin.Plugin;
import org.scijava.widget.InputWidget;
import org.scijava.widget.ToggleWidget;
import org.scijava.widget.WidgetModel;

@Plugin(type = InputWidget.class)
public class JavaFXToggleWidget extends JavaFXInputWidget<Boolean> implements
		ChangeListener<Boolean>, ToggleWidget<HBox>
{

	private CheckBox checkBox;

	// -- InputWidget methods --

	@Override
	public Boolean getValue() {
		return checkBox.isSelected();
	}

	// -- WrapperPlugin methods --

	@Override
	public void set(final WidgetModel model) {
		super.set(model);

		checkBox = new CheckBox("");
//		setToolTip(checkBox);
		getComponent().getChildren().add(checkBox);
		checkBox.selectedProperty().addListener(this);

		refreshWidget();
	}

	// -- Typed methods --

	@Override
	public boolean supports(final WidgetModel model) {
		return super.supports(model) && model.isBoolean();
	}

	// -- AbstractUIInputWidget methods ---

	@Override
	public void doRefresh() {
		final Boolean value = (Boolean) get().getValue();
		if (value != getValue()) checkBox.setSelected(value != null && value);
	}

	@Override
	public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
		updateModel();
	}
}
