package mdc.ida.album.commands;

import org.scijava.app.StatusService;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.menu.MenuConstants;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;
@Plugin(type = Command.class, label = "Quit", menu = {
		@Menu(label = MenuConstants.FILE_LABEL, weight = MenuConstants.FILE_WEIGHT,
			mnemonic = MenuConstants.FILE_MNEMONIC),
		@Menu(label = "Quit", weight = Double.MAX_VALUE, mnemonic = 'q',
			accelerator = "^Q") })
public class QuitProgram extends ContextCommand {

	@Parameter(required = false)
	private StatusService statusService;

	@Parameter(required = false)
	private UIService uiService;

	@Override
	public void run() {
		if (statusService != null) {
			statusService.showStatus("Quitting album...");
		}
		getContext().dispose();
		System.exit(0);
	}

}
