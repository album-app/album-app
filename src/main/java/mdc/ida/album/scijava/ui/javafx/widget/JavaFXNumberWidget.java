package mdc.ida.album.scijava.ui.javafx.widget;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.util.converter.NumberStringConverter;
import org.scijava.plugin.Plugin;
import org.scijava.widget.InputWidget;
import org.scijava.widget.NumberWidget;
import org.scijava.widget.WidgetModel;

@Plugin(type = InputWidget.class)
public class JavaFXNumberWidget extends JavaFXInputWidget<Number> implements NumberWidget<HBox>
{

	private TextField numberField;


	// -- InputWidget methods --

	@Override
	public Number getValue() {
		return Float.valueOf(numberField.getText());
	}

	// -- WrapperPlugin methods --

	@Override
	public void set(final WidgetModel model) {
		super.set(model);

		final Number min = model.getMin();
		final Number max = model.getMax();
		final Number softMin = model.getSoftMin();
		final Number softMax = model.getSoftMax();
		final Number stepSize = model.getStepSize();

		final Class<?> type = model.getItem().getType();
		final Number value = (Number) model.getValue();

		TextField numberField = new TextField();
		numberField.setTextFormatter(new TextFormatter<>(new NumberStringConverter()));
		numberField.textProperty().addListener((observable, oldValue, newValue) -> {
			updateModel();
		});

		refreshWidget();
	}

	@Override
	public boolean supports(final WidgetModel model) {
		return super.supports(model) && model.isNumber();
	}

	@Override
	public void doRefresh() {
		final Object value = get().getValue();
		if (Float.valueOf(numberField.getText()).equals(value)) return; // no change
		numberField.setText(value.toString());
	}
}
