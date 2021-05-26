package mdc.ida.hips.ui.javafx;

import javafx.animation.AnimationTimer;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
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
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import mdc.ida.hips.model.HIPSCatalog;
import mdc.ida.hips.model.HIPSCollection;
import mdc.ida.hips.model.HIPSCollectionUpdatedEvent;
import mdc.ida.hips.service.HIPSServerService;
import org.scijava.Context;
import org.scijava.app.AppService;
import org.scijava.menu.MenuService;
import org.scijava.menu.ShadowMenu;
import org.scijava.plugin.Parameter;
import org.scijava.ui.ApplicationFrame;
import org.scijava.ui.UIService;
import mdc.ida.hips.scijava.ui.javafx.JavaFXService;
import mdc.ida.hips.scijava.ui.javafx.JavaFXStatusBar;
import mdc.ida.hips.scijava.ui.javafx.menu.JavaFXMenuButtonCreator;

import java.io.IOException;

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
	AppService appService;

	@Parameter
	UIService uiService;

	@Parameter
	JavaFXService javaFXService;

	private final TabPane tabPane;
	private final Stage window;

	public HIPSApplicationFrame(Context context, String title, JavaFXStatusBar statusBar) {
		context.inject(this);
		window = new Stage();
		tabPane = new TabPane();
		VBox.setVgrow(tabPane, Priority.ALWAYS);
		Node placeHolder = createPlaceHolder();
		BooleanBinding bb = Bindings.isEmpty( tabPane.getTabs() );
		placeHolder.visibleProperty().bind(bb);
		placeHolder.managedProperty().bind(bb);
		Node header = createHeader();
		VBox box = new VBox(header, placeHolder, tabPane, statusBar);
		box.getStyleClass().add("main");
		Scene scene = new Scene(box);
		scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
		window.setScene(scene);
		window.setTitle(title);
		window.setWidth(800);
		window.setHeight(500);
		window.show();
		window.setOnCloseRequest(event -> {
			javaFXService.setClosing(true);
			appService.getApp().quit();
		});
	}

	private Node createPlaceHolder() {
		Image img = new Image(getClass().getResourceAsStream("hips-scales.png"));
		ImageView image = new ImageView(img);
		image.setOpacity(0.5);
		Button button = new Button("Load collection");
		button.setOnAction(this::updateAndDisplayCollection);
		Button transformButton = new Button("TRANSFORM");
		transformButton.setOnAction(this::transform);
		Text welcomeText = new Text("Welcome to HIPS!");
		HBox welcomeBox = new HBox(welcomeText, button, transformButton);
		welcomeBox.setAlignment(Pos.CENTER);
		welcomeBox.setSpacing(15);
		welcomeBox.setPrefWidth(img.getWidth());
		welcomeBox.setMaxWidth(img.getWidth());
		welcomeBox.setPadding(new Insets(15));
		welcomeBox.setBackground(new Background(new BackgroundFill(new Color(1.0, 1.0, 1.0, 0.5), new CornerRadii(10), Insets.EMPTY)));
		VBox box = new VBox(image, welcomeBox);
		box.setAlignment(Pos.CENTER);
		VBox.setVgrow(box, Priority.ALWAYS);
		return box;
	}

	private void transform(ActionEvent event) {
//		VBox box = new VBox();
//		Scene scene = new Scene(box);
//		window.setScene(scene);
		Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
		double factor = 3;
		AnimationTimer t = new AnimationTimer() {
			@Override
			public void handle(long now) {
				double newWidth = Math.max(80, window.getWidth() - factor);
				double newHeight = Math.max(80, window.getHeight() - factor/2);
				window.setWidth(newWidth);
				window.setHeight(newHeight);
				window.setY(window.getY()+factor);
				// Here put your condition for "destination and desired size"
				// in this case everything will move/grow by 600 px
				if( window.getY()+newHeight >= bounds.getMaxY()){
					this.stop();
					window.setResizable(false);
					addWheels(window);
				}
			}
		};
		t.start();
	}

	private void addWheels(Stage window) {
		Image wheelImage = new Image(getClass().getResourceAsStream("wheel.png"));
		ImageView imageView1 = new ImageView(wheelImage);
		ImageView imageView2 = new ImageView(wheelImage);
		Stage wheel1 = addWheel(imageView1);
		Stage wheel2 = addWheel(imageView2);
		Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

		wheel1.show();
		wheel2.show();

		Window windowWheel1 = wheel1.getScene().getWindow();
		Window windowWheel2 = wheel2.getScene().getWindow();

		windowWheel1.setWidth(0);
		windowWheel1.setHeight(0);
		windowWheel2.setWidth(0);
		windowWheel2.setHeight(0);
		double goalSize = 40;
		double factor = 2;

		AnimationTimer t = new AnimationTimer() {
			@Override
			public void handle(long now) {

				double newSize = windowWheel1.getWidth() + factor;
				double wheelY = bounds.getMaxY() - newSize;
				double windowY = bounds.getMaxY() - newSize / 2 - window.getHeight();
				windowWheel1.setWidth(newSize);
				windowWheel1.setHeight(newSize);
				windowWheel2.setWidth(newSize);
				windowWheel2.setHeight(newSize);
				windowWheel1.setY(wheelY);
				windowWheel2.setY(wheelY);
				windowWheel1.setX(window.getX()+goalSize-newSize/2);
				windowWheel2.setX(window.getX()+window.getWidth()-goalSize-newSize/2);
				window.setY(windowY);
				if( newSize >= goalSize){
					this.stop();
					window.setResizable(false);
					moveAway(window, wheel1, wheel2, imageView1, imageView2);
				}
			}
		};
		t.start();
	}

	private void moveAway(Stage window, Stage wheel1, Stage wheel2, ImageView wheel1Image, ImageView wheel2Image) {
		Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
		float factor = 3;
		window.setOpacity(1.0);
		Image wheelImage = new Image(getClass().getResourceAsStream("wheel.gif"));
		wheel1Image.setImage(wheelImage);
		wheel2Image.setImage(wheelImage);
		AnimationTimer t = new AnimationTimer() {
			@Override
			public void handle(long now) {
				window.setX(window.getX()+factor);
				wheel1.setX(window.getX()+wheel1.getWidth()*0.5);
				wheel2.setX(window.getX()+window.getWidth()-wheel1.getWidth()*1.5);
				window.setOpacity(Math.max(0.0, Math.min(1.0, (bounds.getMaxX() - window.getX()-50)/50.0)));
				wheel1.setOpacity(Math.max(0.0, Math.min(1.0, (bounds.getMaxX() - wheel1.getX()-50)/50.0)));
				wheel2.setOpacity(Math.max(0.0, Math.min(1.0, (bounds.getMaxX() - wheel2.getX()-50)/50.0)));
				if( window.getX() > bounds.getMaxX()){
					this.stop();
				}
			}
		};
		t.start();
	}

	private Stage addWheel(ImageView imageView) {
		imageView.setPreserveRatio(true);
		VBox wheel = new VBox(imageView);
		wheel.setStyle("-fx-background: transparent; -fx-background-color: transparent; ");
		Stage dialog = new Stage();
		dialog.initStyle(StageStyle.TRANSPARENT);
		Scene dialogPane = new Scene(wheel);
		dialogPane.setFill(null);
		wheel.setPadding(Insets.EMPTY);
		wheel.setBackground(Background.EMPTY);
		dialog.setScene(dialogPane);
		imageView.fitWidthProperty().bind(dialog.widthProperty());
		return dialog;
	}

	public void collectionUpdated(HIPSCollectionUpdatedEvent event) {
		HIPSCollection collection = event.getCollection();
		for (HIPSCatalog catalog : collection) {
			uiService.show(catalog.getName(), catalog);
		}
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
