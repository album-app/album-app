package org.scijava.ui.javafx.menu;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import org.scijava.input.Accelerator;
import org.scijava.input.KeyCode;
import org.scijava.menu.AbstractMenuCreator;
import org.scijava.menu.ShadowMenu;
import org.scijava.module.ModuleInfo;

import javax.swing.ButtonGroup;
import java.net.URL;
import java.util.HashMap;

public abstract class AbstractJavaFXMenuCreator<T> extends
	AbstractMenuCreator<T, Menu>
{

	/** Table of button groups for radio button menu items. */
	private HashMap<String, ButtonGroup> buttonGroups = new HashMap<>();

	// -- MenuCreator methods --

	@Override
	public void createMenus(final ShadowMenu root, final T target) {
		buttonGroups = new HashMap<>();
		super.createMenus(root, target);
	}

	// -- Internal methods --

	@Override
	protected void addLeafToMenu(final ShadowMenu shadow, final Menu target) {
		final MenuItem menuItem = createLeaf(shadow);
		target.getItems().add(menuItem);
	}

	@Override
	protected Menu addNonLeafToMenu(final ShadowMenu shadow, final Menu target)
	{
		final Menu menu = createNonLeaf(shadow);
		target.getItems().add(menu);
		return menu;
	}

	@Override
	protected void addSeparatorToMenu(final Menu target) {
		target.getItems().add(new SeparatorMenuItem());
	}

	protected MenuItem createLeaf(final ShadowMenu shadow) {
		final String name = shadow.getMenuEntry().getName();
		final MenuItem menuItem = new MenuItem(name);
		assignProperties(menuItem, shadow);
		linkAction(shadow, menuItem);
		return menuItem;
	}

	protected Menu createNonLeaf(final ShadowMenu shadow) {
		final Menu menu = new Menu(shadow.getMenuEntry().getName());
		assignProperties(menu, shadow);
		return menu;
	}

	// -- Helper methods --

	private boolean isSelected(final ShadowMenu shadow) {
		return shadow.getModuleInfo().isSelected();
	}

	private ButtonGroup getButtonGroup(final ShadowMenu shadow) {
		final String selectionGroup = shadow.getModuleInfo().getSelectionGroup();
		ButtonGroup buttonGroup = buttonGroups.get(selectionGroup);
		if (buttonGroup == null) {
			buttonGroup = new ButtonGroup();
			buttonGroups.put(selectionGroup, buttonGroup);
		}
		return buttonGroup;
	}

	private KeyCombination getKeyStroke(final ShadowMenu shadow) {
		final Accelerator accelerator = shadow.getMenuEntry().getAccelerator();
		if (accelerator == null || accelerator.getKeyCode() == KeyCode.UNDEFINED) return null;
		return KeyCombination.valueOf(accelerator.toString());
	}

	private Image loadIcon(final ShadowMenu shadow) {
		final URL iconURL = shadow.getIconURL();
		return iconURL == null ? null : new Image(iconURL.toString());
	}

	private void assignProperties(final MenuItem menuItem,
		final ShadowMenu shadow)
	{
		final char mnemonic = shadow.getMenuEntry().getMnemonic();
//		if (mnemonic != '\0') menuItem.setMnemonic(mnemonic);

		final KeyCombination keyStroke = getKeyStroke(shadow);
		if (keyStroke != null) menuItem.setAccelerator(keyStroke);

		final Image icon = loadIcon(shadow);
		if (icon != null) menuItem.setGraphic(new ImageView(icon));

		final ModuleInfo info = shadow.getModuleInfo();
		if (info != null) menuItem.setDisable(!info.isEnabled());
	}

	private void linkAction(final ShadowMenu shadow, final MenuItem menuItem) {
		menuItem.setOnAction(event -> shadow.run());
	}

}
