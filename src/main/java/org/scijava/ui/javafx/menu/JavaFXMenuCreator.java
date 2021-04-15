package org.scijava.ui.javafx.menu;

import javafx.scene.control.Menu;
import org.scijava.menu.ShadowMenu;

public class JavaFXMenuCreator extends AbstractJavaFXMenuCreator<Menu> {

	@Override
	protected void addLeafToTop(final ShadowMenu shadow, final Menu target) {
		addLeafToMenu(shadow, target);
	}

	@Override
	protected Menu addNonLeafToTop(final ShadowMenu shadow, final Menu target) {
		return addNonLeafToMenu(shadow, target);
	}

	@Override
	protected void addSeparatorToTop(final Menu target) {
		addSeparatorToMenu(target);
	}

}
