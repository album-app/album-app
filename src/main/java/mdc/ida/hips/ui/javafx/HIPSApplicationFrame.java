package mdc.ida.hips.ui.javafx;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import mdc.ida.hips.model.HIPSCollectionUpdatedEvent;
import mdc.ida.hips.service.HIPSServerService;
import org.scijava.Context;
import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.event.EventSubscriber;
import org.scijava.menu.MenuService;
import org.scijava.menu.ShadowMenu;
import org.scijava.plugin.Parameter;
import org.scijava.ui.ApplicationFrame;
import org.scijava.ui.UIService;
import org.scijava.ui.javafx.JavaFXStatusBar;
import org.scijava.ui.javafx.menu.JavaFXMenuButtonCreator;

import java.io.IOException;
import java.util.List;

/**
 * JavaFX implementation of {@link ApplicationFrame}.
 *
 */
public class HIPSApplicationFrame implements ApplicationFrame {

	@Parameter
	MenuService menuService;

	@Parameter
	HIPSServerService serverService;

	@Parameter
	UIService uiService;

	private final TabPane tabPane;
	private final Stage window;

	public HIPSApplicationFrame(Context context, String title, JavaFXStatusBar statusBar) {
		context.inject(this);
		window = new Stage();
		tabPane = new TabPane();
		VBox.setVgrow(tabPane, Priority.ALWAYS);
		Node placeHolder = createPlaceHolder();
		VBox.setVgrow(placeHolder, Priority.ALWAYS);
		BooleanBinding bb = Bindings.isEmpty( tabPane.getTabs() );
		placeHolder.visibleProperty().bind(bb);
		placeHolder.managedProperty().bind(bb);
		Node header = createHeader();
		VBox box = new VBox(header, placeHolder, tabPane, statusBar);
		box.getStyleClass().add("main");
		Scene scene = new Scene(box);
		scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
		window.setScene(scene);
		window.setTitle(title);
		window.setWidth(800);
		window.setHeight(500);
		window.show();
	}

	private Node createPlaceHolder() {
		Image img = new Image(getClass().getResourceAsStream("/hips-scales.png"));
		ImageView image = new ImageView(img);
		image.setOpacity(0.5);
		Button button = new Button("Load collection");
		button.setOnAction(this::updateAndDisplayCollection);
		Text welcomeText = new Text("Welcome to HIPS!");
		HBox welcomeBox = new HBox(welcomeText, button);
		welcomeBox.setAlignment(Pos.CENTER);
		welcomeBox.setSpacing(15);
		welcomeBox.setPrefWidth(img.getWidth());
		welcomeBox.setMaxWidth(img.getWidth());
		welcomeBox.setPadding(new Insets(15));
		welcomeBox.setBackground(new Background(new BackgroundFill(new Color(1.0, 1.0, 1.0, 0.5), new CornerRadii(10), Insets.EMPTY)));
		VBox box = new VBox(image, welcomeBox);
		box.setAlignment(Pos.CENTER);
		return box;
	}

	public void collectionUpdated(HIPSCollectionUpdatedEvent event) {
		uiService.show("HIPS Collection", event.getCollection());
	}

	private Node createHeader() {
		Text header1 = new Text("HIPS ");
		header1.getStyleClass().add("header-short");
		Text header2 = new Text("Helmholtz Imaging Platform Solutions");
		header2.getStyleClass().add("header-long");
		final Pane spacer = new Pane();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		spacer.setMinSize(10, 1);
		HBox box = new HBox(header1, header2, spacer, createMenu());
		box.setBackground(Background.EMPTY);
		box.setPadding(new Insets(5));
		box.getStyleClass().add("header");
		return box;
	}

	private Node createMenu() {
		ShadowMenu menu = menuService.getMenu();
		MenuButton menuBar = new MenuButton();
		new JavaFXMenuButtonCreator().createMenus(menu, menuBar);
		return menuBar;
	}

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

	private void updateAndDisplayCollection(ActionEvent event) {
		try {
			serverService.updateIndex(this::collectionUpdated);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
