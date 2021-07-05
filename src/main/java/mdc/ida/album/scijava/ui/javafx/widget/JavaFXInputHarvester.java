package mdc.ida.album.scijava.ui.javafx.widget;

import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import org.scijava.module.Module;
import org.scijava.module.process.PreprocessorPlugin;
import org.scijava.plugin.Plugin;
import org.scijava.ui.AbstractInputHarvesterPlugin;
import mdc.ida.album.scijava.ui.javafx.JavaFXUI;
import org.scijava.widget.InputHarvester;
import org.scijava.widget.InputPanel;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

@Plugin(type = PreprocessorPlugin.class, priority = InputHarvester.PRIORITY)
public class JavaFXInputHarvester extends
	AbstractInputHarvesterPlugin<VBox, HBox>
{

	// -- InputHarvester methods --

	@Override
	public JavaFXInputPanel createInputPanel() {
		return new JavaFXInputPanel();
	}

	@Override
	public boolean harvestInputs(final InputPanel<VBox, HBox> inputPanel,
		final Module module) {
		FutureTask<Optional<ButtonType>> futureTask = new FutureTask<>(
				() -> {
					final VBox pane = inputPanel.getComponent();

					// display input panel in a dialog
					final String title = module.getInfo().getTitle();
					final boolean modal = !module.getInfo().isInteractive();
					final boolean allowCancel = module.getInfo().canCancel();
					final int messageType;
					final boolean doScrollBars = true;
					Dialog<ButtonType> dialog = new Dialog<>();

					DialogPane dialogPane = new DialogPane();
					if (doScrollBars) {
						dialogPane.setContent(new ScrollPane(pane));
					} else {
						dialogPane.setContent(pane);
					}
					dialog.setDialogPane(dialogPane);

					dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
					if(allowCancel) {
						dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
					}
					dialog.setTitle(title);
					if (modal) {
						dialog.initModality(Modality.APPLICATION_MODAL);
					} else {
						dialog.initModality(Modality.NONE);
					}
					return dialog.showAndWait();
				}
		);
		Platform.runLater(futureTask);
		Optional<ButtonType> result = Optional.empty();
		try {
			result = futureTask.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return result.isPresent() && result.get() == ButtonType.OK;
	}

	// -- Internal methods --

	@Override
	protected String getUI() {
		return JavaFXUI.NAME;
	}
}

