package mdc.ida.album.commands;

import org.scijava.ItemIO;
import org.scijava.app.App;
import org.scijava.app.AppService;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.display.Display;
import org.scijava.display.DisplayService;
import org.scijava.menu.MenuConstants;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, label = "About album...",
	menu = {
		@Menu(label = MenuConstants.HELP_LABEL, weight = MenuConstants.HELP_WEIGHT,
			mnemonic = MenuConstants.HELP_MNEMONIC),
		@Menu(label = "About album...") }, headless = true)
public class AboutAlbum extends ContextCommand {

	@Parameter
	private AppService appService;

	@Parameter
	private DisplayService dispSrv;

	@Parameter(type = ItemIO.OUTPUT)
	private Display<?> display;

	@Override
	public void run() {
		final String title = getApp().getTitle();
		String aboutText = "About album >>>> TODO";
		display = dispSrv.createDisplay("About " + title, aboutText);
	}

	private App getApp() {
		return appService.getApp();
	}

}
