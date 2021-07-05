package mdc.ida.album.scijava.ui.javafx.menu;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import org.scijava.menu.ShadowMenu;

public class JavaFXMenuBarCreator extends AbstractJavaFXMenuCreator<MenuBar> {

	@Override
	protected void addLeafToTop(final ShadowMenu shadow, final MenuBar target) {
		// does not work?!
	}

	@Override
	protected Menu
		addNonLeafToTop(final ShadowMenu shadow, final MenuBar target)
	{
		final Menu menu = createNonLeaf(shadow);
		target.getMenus().add(menu);
		return menu;
	}

	@Override
	protected void addSeparatorToTop(final MenuBar target) {
		// NB: Ignore top-level separator.
	}

}
