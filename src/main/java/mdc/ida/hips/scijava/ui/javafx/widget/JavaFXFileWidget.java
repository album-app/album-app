
package mdc.ida.hips.scijava.ui.javafx.widget;

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
public class JavaFXFileWidget extends JavaFXInputWidget<File> implements FileWidget<HBox>
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
		browse.setOnAction(e -> chooseFile());
		path.textProperty().addListener((observable, oldValue, newValue) -> {
			updateModel();
		});
		getComponent().getChildren().add(browse);
		refreshWidget();
	}

	private void chooseFile() {
		File file = new File(path.getText());
		if (!file.isDirectory()) {
			file = file.getParentFile();
		}

		// display file chooser in appropriate mode
		final WidgetModel model = get();
		final String style;
		if (model.isStyle(FileWidget.DIRECTORY_STYLE)) {
			style = FileWidget.DIRECTORY_STYLE;
		}
		else if (model.isStyle(FileWidget.SAVE_STYLE)) {
			style = FileWidget.SAVE_STYLE;
		}
		else {
			style = FileWidget.OPEN_STYLE;
		}
		file = uiService.chooseFile(file, style);
		if (file == null) return;

		path.setText(file.getAbsolutePath());
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
