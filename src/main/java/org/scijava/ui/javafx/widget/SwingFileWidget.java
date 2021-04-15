
package org.scijava.ui.javafx.widget;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;
import org.scijava.widget.FileWidget;
import org.scijava.widget.InputWidget;
import org.scijava.widget.WidgetModel;

import java.io.File;

@Plugin(type = InputWidget.class)
public class SwingFileWidget extends JavaFXInputWidget<File> implements FileWidget<HBox>
{

	@Parameter
	private UIService uiService;

	private TextField path;
	private Button browse;

	// -- InputWidget methods --

	@Override
	public File getValue() {
		final String text = path.getText();
		return text.isEmpty() ? null : new File(text);
	}

	// -- WrapperPlugin methods --

	@Override
	public void set(final WidgetModel model) {
		super.set(model);

		path = new TextField();
//		path.setDragEnabled(true);
		final String style = model.getItem().getWidgetStyle();
		getComponent().getChildren().add(path);
		FileChooser fileChooser = new FileChooser();
		browse = new Button("Browse");
		browse.setOnAction(e -> {
			File file = fileChooser.showOpenDialog(getComponent().getScene().getWindow());
			if(file != null) path.setText(file.getAbsolutePath());
		});
		getComponent().getChildren().add(browse);
		refreshWidget();
	}

	// -- Typed methods --

	@Override
	public boolean supports(final WidgetModel model) {
		return super.supports(model) && model.isType(File.class);
	}

	@Override
	public void doRefresh() {
		final String text = get().getText();
		if (text.equals(path.getText())) return; // no change
		path.setText(text);
	}
}
