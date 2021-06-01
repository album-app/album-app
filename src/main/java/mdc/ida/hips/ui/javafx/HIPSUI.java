package mdc.ida.hips.ui.javafx;

import javafx.application.Platform;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.scijava.Priority;
import org.scijava.display.Display;
import org.scijava.plugin.Plugin;
import org.scijava.ui.AbstractUserInterface;
import org.scijava.ui.DialogPrompt;
import org.scijava.ui.SystemClipboard;
import org.scijava.ui.ToolBar;
import org.scijava.ui.UserInterface;
import mdc.ida.hips.scijava.ui.javafx.JavaFXClipboard;
import mdc.ida.hips.scijava.ui.javafx.JavaFXStatusBar;
import mdc.ida.hips.scijava.ui.javafx.JavaFXUI;
import mdc.ida.hips.scijava.ui.javafx.console.JavaFXConsolePane;
import org.scijava.ui.viewer.DisplayWindow;
import org.scijava.widget.FileWidget;

import java.io.File;

/**
 * Implementation for JavaFX-based user interfaces.
 *
 */
@Plugin(type = UserInterface.class, name = JavaFXUI.NAME, priority = Priority.HIGH)
public class HIPSUI extends AbstractUserInterface implements JavaFXUI {

	private HIPSApplicationFrame appFrame;
	private JavaFXStatusBar statusBar;
	private JavaFXConsolePane consolePane;
	private JavaFXClipboard systemClipboard;

	@Override
	protected void createUI() {
		Platform.startup(() -> {
			statusBar = new JavaFXStatusBar(context());
			consolePane = new JavaFXConsolePane(context());
			systemClipboard = new JavaFXClipboard();
			appFrame = new HIPSApplicationFrame(context(), "HIPS", statusBar);
			consolePane.setTabPane(appFrame.getTabPane());
			super.createUI();
		});
		Platform.setImplicitExit(true);
	}

	@Override
	public HIPSApplicationFrame getApplicationFrame() {
		return appFrame;
	}

	@Override
	public ToolBar getToolBar() {
		return null;
	}

	@Override
	public JavaFXStatusBar getStatusBar() {
		return statusBar;
	}

	@Override
	public JavaFXConsolePane getConsolePane() {
		return consolePane;
	}

	@Override
	public SystemClipboard getSystemClipboard() {
		return systemClipboard;
	}

	@Override
	public DisplayWindow createDisplayWindow(Display<?> display) {
		HIPSDisplayWindow window = new HIPSDisplayWindow();
		Platform.runLater(() -> {
			getApplicationFrame().getTabPane().getTabs().add(window);
		});
		return window;
	}

	@Override
	public DialogPrompt dialogPrompt(String message, String title, DialogPrompt.MessageType messageType, DialogPrompt.OptionType optionType) {
		// TODO
		return null;
	}

	@Override
	public void showContextMenu(String menuRoot, Display<?> display, int x, int y) {
		// TODO
	}

	@Override
	public boolean requiresEDT() {
		return true;
	}

	@Override
	public File chooseFile(File file, String style) {
		return chooseFile("Choose file", file, style);
	}

	@Override
	public File chooseFile(String title, File file, String style) {
		Window window = getApplicationFrame().getTabPane().getScene().getWindow();
		if(style.equals(FileWidget.DIRECTORY_STYLE)) {
			DirectoryChooser chooser = new DirectoryChooser();
			if(file != null) chooser.setInitialDirectory(file);
			return chooser.showDialog(window);
		} else {
			FileChooser chooser = new FileChooser();
			chooser.setTitle(title);
			if(file != null && file.exists()) {
				chooser.setInitialDirectory(file.getParentFile());
				chooser.setInitialFileName(file.getName());
			}
			if(style.equals(FileWidget.SAVE_STYLE)) {
				return chooser.showSaveDialog(window);
			}
			return chooser.showOpenDialog(window);
		}
	}
}
