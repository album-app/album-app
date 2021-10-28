package mdc.ida.album.ui.javafx;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import mdc.ida.album.scijava.ui.javafx.JavaFXService;
import mdc.ida.album.scijava.ui.javafx.JavaFXStatusBar;
import org.scijava.Context;
import org.scijava.app.AppService;
import org.scijava.command.CommandService;
import org.scijava.menu.MenuService;
import org.scijava.plugin.Parameter;
import org.scijava.ui.ApplicationFrame;
import org.scijava.ui.UIService;

/**
 * JavaFX implementation of {@link ApplicationFrame}.
 *
 */
public class AlbumApplicationFrame implements ApplicationFrame {

	@Parameter
	private MenuService menuService;

	@Parameter
	private AppService appService;

	@Parameter
	private UIService uiService;

	@Parameter
	private JavaFXService javaFXService;

	@Parameter
	private CommandService commandService;

	private final TabPane tabPane;
	private final Stage window;

	public AlbumApplicationFrame(Context context, String title, JavaFXStatusBar statusBar) {
		context.inject(this);
		window = new Stage();
		tabPane = new TabPane();
		VBox.setVgrow(tabPane, Priority.ALWAYS);
		Node header = createHeader();
		VBox box = new VBox(header, tabPane, statusBar);
		box.getStyleClass().add("main");
		Scene scene = new Scene(box);

		scene.getStylesheets().add(getClass().getResource("style.css").toString());

		window.setScene(scene);
		window.setTitle(title);
		window.setWidth(830);
		window.setHeight(500);
		window.show();
		window.setOnCloseRequest(event -> {
			javaFXService.setClosing(true);
			appService.getApp().quit();
		});
	}

	private Node createHeader() {
		Label header1 = new Label("album ");
		header1.getStyleClass().add("header-short");
		Label header2 = new Label("sharing solutions across tools and domains");
		header2.getStyleClass().add("header-long");
		final Pane spacer = new Pane();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		spacer.setMinSize(10, 1);
//		HBox box = new HBox(header1, header2, spacer, createMenu());
		HBox box = new HBox(header1, header2, spacer);
		box.setBackground(Background.EMPTY);
		box.setPadding(new Insets(5));
		box.getStyleClass().add("header");
		return box;
	}

//	private Node createMenu() {
//		ShadowMenu menu = menuService.getMenu();
//		MenuButton menuBar = new MenuButton();
//		new JavaFXMenuButtonCreator().createMenus(menu, menuBar);
//		return menuBar;
//	}

	@Override
	public void setLocation(int x, int y) {
		if(window != null) {
			window.setX(x);
			window.setY(y);
		}
	}

	@Override
	public int getLocationX() {
		return window == null? 0 : (int) window.getX();
	}

	@Override
	public int getLocationY() {
		return window == null? 0 : (int) window.getY();
	}

	@Override
	public void activate() {
		// TODO
	}

	@Override
	public void setVisible(boolean visible) {
		if(window == null) return;
		if(visible) window.show();
		else window.hide();
	}

	public TabPane getTabPane() {
		return tabPane;
	}

}
