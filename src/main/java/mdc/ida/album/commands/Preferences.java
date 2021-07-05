package mdc.ida.album.commands;

import org.scijava.command.Command;
import org.scijava.menu.MenuConstants;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.prefs.PrefService;

@Plugin(type = Command.class, label = "album preferences", menu = {
		@Menu(label = MenuConstants.FILE_LABEL, weight = MenuConstants.FILE_WEIGHT,
				mnemonic = MenuConstants.FILE_MNEMONIC),
		@Menu(label = "Preferences") }, headless = true)
public class Preferences implements Command {

	@Parameter
	private PrefService prefService;

	@Parameter(label = "Clear all preferences")
	private boolean clearAll = false;

	@Override
	public void run() {
		if (clearAll) prefService.clearAll();
	}

	public void setClearAll(boolean val) {
		clearAll = val;
	}

	public boolean isClearAll() {
		return clearAll;
	}
}
