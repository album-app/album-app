package mdc.ida.hips.scijava.ui.javafx.widget;

import javafx.scene.layout.HBox;
import org.scijava.ui.AbstractUIInputWidget;
import org.scijava.ui.UserInterface;
import mdc.ida.hips.scijava.ui.javafx.JavaFXUI;
import org.scijava.widget.WidgetModel;

public abstract class JavaFXInputWidget<T> extends
	AbstractUIInputWidget<T, HBox>
{

	private HBox uiComponent;

	// -- WrapperPlugin methods --

	@Override
	public void set(final WidgetModel model) {
		super.set(model);
		uiComponent = new HBox();
	}

	// -- UIComponent methods --

	@Override
	public HBox getComponent() {
		return uiComponent;
	}

	@Override
	public Class<HBox> getComponentType() {
		return HBox.class;
	}

	@Override
	protected UserInterface ui() {
		return ui(JavaFXUI.NAME);
	}

}
