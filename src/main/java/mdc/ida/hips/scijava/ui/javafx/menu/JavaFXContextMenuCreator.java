
package mdc.ida.hips.scijava.ui.javafx.menu;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import org.scijava.menu.ShadowMenu;

public class JavaFXContextMenuCreator extends
		AbstractJavaFXMenuCreator<ContextMenu>
{

	@Override
	protected void addLeafToTop(final ShadowMenu shadow, final ContextMenu target)
	{
		final MenuItem menuItem = createLeaf(shadow);
		target.getItems().add(menuItem);
	}

	@Override
	protected Menu addNonLeafToTop(final ShadowMenu shadow,
	                               final ContextMenu target)
	{
		final Menu menu = createNonLeaf(shadow);
		target.getItems().add(menu);
		return menu;
	}

	@Override
	protected void addSeparatorToTop(final ContextMenu target) {
		target.getItems().add(new SeparatorMenuItem());
	}
}
