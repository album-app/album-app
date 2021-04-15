package org.scijava.ui.javafx.menu;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import org.scijava.menu.ShadowMenu;

public class JavaFXMenuButtonCreator extends AbstractJavaFXMenuCreator<MenuButton> {

	@Override
	protected void addLeafToTop(final ShadowMenu shadow, final MenuButton target) {
		final MenuItem menuItem = createLeaf(shadow);
		target.getItems().add(menuItem);
	}

	@Override
	protected Menu
		addNonLeafToTop(final ShadowMenu shadow, final MenuButton target)
	{
		final Menu menu = createNonLeaf(shadow);
		target.getItems().add(menu);
		return menu;
	}

	@Override
	protected void addSeparatorToTop(final MenuButton target) {
		target.getItems().add(new SeparatorMenuItem());
	}

}
