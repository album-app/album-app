package mdc.ida.hips.ui.javafx;

import javafx.application.Platform;
import org.scijava.Context;
import org.scijava.Priority;
import org.scijava.app.AppService;
import org.scijava.display.Display;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.menu.MenuService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.thread.ThreadService;
import org.scijava.ui.AbstractUserInterface;
import org.scijava.ui.DialogPrompt;
import org.scijava.ui.SystemClipboard;
import org.scijava.ui.ToolBar;
import org.scijava.ui.UIService;
import org.scijava.ui.UserInterface;
import org.scijava.ui.javafx.JavaFXClipboard;
import org.scijava.ui.javafx.console.JavaFXConsolePane;
import org.scijava.ui.javafx.JavaFXStatusBar;
import org.scijava.ui.javafx.JavaFXUI;
import org.scijava.ui.viewer.DisplayWindow;

/**
 * Implementation for JavaFX-based user interfaces.
 *
 */
@Plugin(type = UserInterface.class, name = JavaFXUI.NAME, priority = Priority.HIGH)
public class HIPSUI extends AbstractUserInterface implements
	JavaFXUI {

	@Parameter
	private AppService appService;

	@Parameter
	private EventService eventService;

	@Parameter
	private MenuService menuService;

	@Parameter
	private UIService uiService;

	@Parameter
	private ThreadService threadService;

	@Parameter
	private LogService log;

	@Parameter
	private Context context;

	private HIPSApplicationFrame appFrame;
	private JavaFXStatusBar statusBar;
	private JavaFXConsolePane consolePane;
	private JavaFXClipboard systemClipboard;

	@Override
	protected void createUI() {
		Platform.startup(() -> {
			statusBar = new JavaFXStatusBar(context);
			consolePane = new JavaFXConsolePane(context);
			systemClipboard = new JavaFXClipboard();
			appFrame = new HIPSApplicationFrame(context, appService.getApp().getTitle(), statusBar);
			consolePane.setTabPane(appFrame.getTabPane());
			super.createUI();
		});
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
		getApplicationFrame().getTabPane().getTabs().add(window);
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

}
