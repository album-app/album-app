package mdc.ida.album.ui.javafx.viewer;

import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
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
import javafx.stage.DirectoryChooser;
import mdc.ida.album.DefaultValues;
import mdc.ida.album.UITextValues;
import mdc.ida.album.model.CatalogUpdate;
import mdc.ida.album.model.CollectionUpdates;
import mdc.ida.album.model.event.CollectionIndexEvent;
import mdc.ida.album.model.LocalAlbumInstallation;
import mdc.ida.album.model.SolutionCollection;
import mdc.ida.album.model.event.CollectionUpgradeEvent;
import mdc.ida.album.scijava.ui.javafx.viewer.EasyJavaFXDisplayViewer;
import org.scijava.Context;
import org.scijava.event.EventHandler;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;
import org.scijava.ui.viewer.DisplayViewer;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import static mdc.ida.album.DefaultValues.UI_SPACING;
import static mdc.ida.album.ui.javafx.viewer.JavaFXUtils.createLocalInstallationTab;
import static mdc.ida.album.ui.javafx.viewer.JavaFXUtils.makePretty;
import static mdc.ida.album.ui.javafx.viewer.JavaFXUtils.scrollable;

/**
 * This class displays a {@link LocalAlbumInstallation}.
 */
@Plugin(type = DisplayViewer.class)
public class LocalAlbumInstallationDisplayViewer extends EasyJavaFXDisplayViewer<LocalAlbumInstallation> {

	@Parameter
	private Context context;
	@Parameter
	private UIService uiService;

	private InstallationState installationState;
	private Label updateStatusLabel;

	public LocalAlbumInstallationDisplayViewer() {
		super(LocalAlbumInstallation.class);
	}

	@Override
	protected boolean canView(LocalAlbumInstallation installation) {
		return true;
	}

	@Override
	protected Node createDisplayPanel(LocalAlbumInstallation installation) {
		installationState = new InstallationState(installation);
		context.inject(installationState);
		Tab collectionTab = createCollectionTab();
		TabPane content = createTabPane(
				createSystemTab(),
				collectionTab,
				createAuthorTab(installationState)
		);
		content.getSelectionModel().select(collectionTab);
		content.setPadding(new Insets(5));
		return content;
	}

	@EventHandler
	private void updatesApplied(CollectionUpgradeEvent e) {
		updateStatusLabel.setText(UITextValues.COLLECTION_UP_TO_DATE_LABEL);
	}

	private Tab createAuthorTab(InstallationState installationState) {
		InformationForAuthorsView content = new InformationForAuthorsView(installationState);
		return createLocalInstallationTab(scrollable(content), UITextValues.INSTALLATION_AUTHOR_TAB);
	}

	private Tab createCollectionTab() {
		VBox contentLeft = new VBox(
				createLoadCollectionButton(),
				createManageCatalogsSection()
		);
		makePretty(contentLeft);
		contentLeft.setPadding(Insets.EMPTY);
		VBox contentRight = createRecentSolutionsSection();
		makePretty(contentRight);
		HBox.setHgrow(contentRight, Priority.ALWAYS);
		HBox content = makePretty(new HBox(contentLeft, contentRight));
		content.disableProperty().bind(Bindings.createBooleanBinding(
				() -> !installationState.isAlbumRunning(), installationState.albumRunningProperty()));
		VBox tabContent = new VBox(createInstallationStatusbar(), content);
		return createLocalInstallationTab(tabContent, UITextValues.INSTALLATION_COLLECTION_TAB);
	}

	private Node createInstallationStatusbar() {
		final Pane spacer = new Pane();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		spacer.setMinSize(10, 1);
		HBox res = new HBox(
				createInstallationStatusLabel(),
				spacer,
				createUpdateBtn()
		);
		res.setPadding(new Insets(UI_SPACING/2.));
		res.setBackground(new Background(
				new BackgroundFill(new Color(0.9, 0.9, 0.9, 1.0), new CornerRadii(0), Insets.EMPTY)));
		return res;
	}

	private Node createUpdateBtn() {
		updateStatusLabel = new Label();
		Button res = new Button(UITextValues.COLLECTION_UPDATE_BTN);
		res.setDisable(true);
		res.setPrefHeight(42);
		res.setPadding(new Insets(5));
		installationState.albumRunningProperty().addListener((observable, oldValue, newValue) -> {
			res.setDisable(!newValue);
		});
		res.setOnAction(event -> {
			try {
				installationState.startUpdate(upgradePreviewEvent -> {
					CollectionUpdates updates = upgradePreviewEvent.getUdpates();
					boolean updatesFound = false;
					for (List<CatalogUpdate> catalogUpdates : updates.values()) {
						if (catalogUpdates.size() > 0) {
							updatesFound = true;
							break;
						}
					}
					if(!updatesFound) {
						updateStatusLabel.setText(UITextValues.COLLECTION_UP_TO_DATE_LABEL);
					} else {
						updateStatusLabel.setText("");
						uiService.show(UITextValues.UPDATES_TAB_NAME, updates);
					}
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		HBox box = new HBox(updateStatusLabel, res);
		box.setFillHeight(true);
		box.setAlignment(Pos.CENTER_RIGHT);
		return box;
	}

	private Node createInstallationStatusLabel() {
		Label textLabel = new Label();
		Label iconLabel = new Label();
		if(installationState.isAlbumRunning()) {
			textLabel.setText(UITextValues.STATUS_ALBUM_RUNNING);
			iconLabel.getStyleClass().add("icon-ok");
			iconLabel.setText(UITextValues.STATUS_SUCCESSFUL_ICON);
		} else {
			if(installationState.isCondaMissing()) {
				textLabel.setText(UITextValues.STATUS_CONDA_MISSING);
				iconLabel.setText(UITextValues.STATUS_WARNING_ICON);
				iconLabel.getStyleClass().add("icon-warning");
			} else {
				if(!installationState.isHasAlbumEnvironment()) {
					textLabel.setText(UITextValues.STATUS_ALBUM_ENVIRONMENT_MISSING);
					iconLabel.setText(UITextValues.STATUS_WARNING_ICON);
					iconLabel.getStyleClass().add("icon-warning");
				}
			}
		}
		installationState.albumRunningProperty().addListener((observable, oldValue, newValue) -> {
			if(newValue) {
				textLabel.setText(UITextValues.STATUS_ALBUM_RUNNING);
				iconLabel.setText(UITextValues.STATUS_SUCCESSFUL_ICON);
				iconLabel.getStyleClass().add("icon-ok");
			}
		});
		installationState.condaMissingProperty().addListener((observable, oldValue, newValue) -> {
			if(newValue) {
				textLabel.setText(UITextValues.STATUS_CONDA_MISSING);
				iconLabel.setText(UITextValues.STATUS_WARNING_ICON);
				iconLabel.getStyleClass().add("icon-warning");
			}
		});
		installationState.hasAlbumEnvironmentProperty().addListener((observable, oldValue, newValue) -> {
			if(newValue) {
				textLabel.setText(UITextValues.STATUS_ALBUM_ENVIRONMENT_FOUND);
				iconLabel.setText(UITextValues.STATUS_SUCCESSFUL_ICON);
				iconLabel.getStyleClass().add("icon-ok");
			}
		});
		HBox box = new HBox(iconLabel, textLabel);
		box.setFillHeight(true);
		box.setAlignment(Pos.CENTER_RIGHT);
		return box;
	}

	private Node createManageCatalogsSection() {
		Label label = new Label(UITextValues.COLLECTION_CATALOGS_TITLE);
		VBox box = new VBox(label, new CatalogListView(context, installationState.getInstallation()));
		box.getStyleClass().add("my-catalogs");
		VBox.setVgrow(box, Priority.ALWAYS);
		return makePretty(box);
	}

	private VBox createRecentSolutionsSection() {
		VBox installedBox = new VBox(new Label(UITextValues.INSTALLATION_RECENTLY_INSTALLED_TITLE),
				new RecentlyInstalledSolutionsView(context, installationState.getInstallation()));
		installedBox.getStyleClass().add("recently-launched");
		VBox launchedBox = new VBox(new Label(UITextValues.INSTALLATION_RECENTLY_LAUNCHED_TITLE),
				new RecentlyLaunchedSolutionsView(context, installationState.getInstallation()));
		installedBox.getStyleClass().add("recently-launched");
		launchedBox.getStyleClass().add("recently-launched");
		makePretty(installedBox);
		makePretty(launchedBox);
//		return new VBox(installedBox, launchedBox);
		return installedBox;
	}

	private Tab createSystemTab() {
		VBox systemContentBox = createSystemContentBox();
		makePretty(systemContentBox);
		systemContentBox.setBackground(Background.EMPTY);
		return createLocalInstallationTab(scrollable(systemContentBox), UITextValues.INSTALLATION_SYSTEM_TAB);
	}

	private TabPane createTabPane(Tab... content) {
		TabPane tabPane = new TabPane(content);
		tabPane.getStyleClass().add("installation-tabpane");
		tabPane.setSide(Side.LEFT);
		tabPane.setRotateGraphic(true);
		return tabPane;
	}

	private VBox createSystemContentBox() {
		Node appInitializingBox = createWelcomeSubBox(
				new Label(UITextValues.INSTALLATION_INITIALIZING_LABEL)
		);
		Node initialSetupRunningBox = createWelcomeSubBox(
				new Label(UITextValues.INSTALLATION_SETUP_RUNNING_LABEL)
		);
		Node defaultWelcomeBox = new InstallationConfigurationPane(installationState);
		Node initialSetupBox = createWelcomeSubBoxVertial(
				createSetupAlbumLabel(),
				createSetupAlbumBox()
		);

		IntegerProperty state = new SimpleIntegerProperty();
		state.bind(Bindings.createIntegerBinding(() -> {
				if(installationState.isInitialSetupRunning()) return 2;
				if(!installationState.isCondaMissing() && !installationState.isCondaInstalled()) return 0;
				if(installationState.isCondaMissing() && !installationState.isCondaShouldExist()) return 1;
				return 3;
			}, installationState.condaInstalledProperty(),
				installationState.condaMissingProperty(),
				installationState.initialSetupRunningProperty()));

		bindToState(state, 0, appInitializingBox);
		bindToState(state, 1, initialSetupBox);
		bindToState(state, 2, initialSetupRunningBox);
		bindToState(state, 3, defaultWelcomeBox);

		return new VBox(
				appInitializingBox,
				initialSetupBox,
				defaultWelcomeBox,
				initialSetupRunningBox);
	}

	private void bindToState(IntegerProperty state, int i, Node node) {
		node.visibleProperty().bind(Bindings.createBooleanBinding(() -> state.get() == i, state));
		node.managedProperty().bind(node.visibleProperty());
	}

	private Label createSetupAlbumLabel() {
		Label label = new Label(UITextValues.INSTALLATION_STARTING_SETUP_INTRO_LABEL);
		label.getStyleClass().add("bold");
		return label;
	}

	private Node createSetupAlbumBox() {
		RadioButton downloadConda = new RadioButton(UITextValues.INSTALLATION_DOWNLOAD_CONDA_BTN);
		RadioButton useExistingConda = new RadioButton(UITextValues.INSTALLATION_EXISTING_CONDA_BTN);
		downloadConda.setSelected(true);
		ToggleGroup group = new ToggleGroup();
		group.getToggles().add(downloadConda);
		group.getToggles().add(useExistingConda);
		StringProperty downloadTarget = new SimpleStringProperty();
		String condaPath = installationState.getCondaPath();
		VBox res = new VBox(
				createFileChooserAction(
						downloadConda,
						installationState.getDefaultCondaDownloadTarget().getAbsolutePath(),
						file -> {
							try {
								installationState.validateCondaTarget(downloadTarget, file);
							} catch (IOException e) {
								e.printStackTrace();
							}
						},
						null, null
				),
				createFileChooserAction(
						useExistingConda,
						condaPath,
						file1 -> {
							installationState.setCondaPath(file1);
						}, null, null
				),
				createDefaultCatalogField(),
				createStartAlbumButton(downloadConda, downloadTarget));
		res.setSpacing(DefaultValues.UI_SPACING);
		res.setAlignment(Pos.BASELINE_CENTER);
		return res;
	}

	private Button createStartAlbumButton(RadioButton downloadConda, StringProperty downloadTarget) {
		Button startAlbumButton = new Button(UITextValues.INSTALLATION_LAUNCH_ALBUM_BTN);
		startAlbumButton.setOnAction(event -> {
			new Thread(() -> {
				try {
					installationState.initAlbumInstallation(downloadConda.isSelected(), downloadTarget);
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}).start();
		});
		startAlbumButton.getStyleClass().add("bold");
		return startAlbumButton;
	}

	private Node createDefaultCatalogField() {
		TextField textField = new TextField(installationState.getCatalog());
		installationState.catalogProperty().bind(textField.textProperty());
		Label label = new Label(UITextValues.INSTALLATION_DEFAULT_CATALOG_LABEL);
		label.setPadding(new Insets(0, DefaultValues.UI_SPACING, 0, 0));
		HBox.setHgrow(textField, Priority.ALWAYS);
		return new HBox(label, textField);
	}

	private HBox createWelcomeSubBox(Node... nodes) {
		HBox res = new HBox(nodes);
		res.setAlignment(Pos.CENTER);
		res.setSpacing(DefaultValues.UI_SPACING);
		res.setPadding(new Insets(DefaultValues.UI_SPACING));
		res.setBackground(new Background(new BackgroundFill(new Color(1.0, 1.0, 1.0, 0.5), new CornerRadii(10), Insets.EMPTY)));
		return res;
	}

	private VBox createWelcomeSubBoxVertial(Node... nodes) {
		VBox res = new VBox(nodes);
		res.setAlignment(Pos.CENTER);
		res.setSpacing(DefaultValues.UI_SPACING);
		res.setPadding(new Insets(DefaultValues.UI_SPACING));
		res.setBackground(new Background(new BackgroundFill(new Color(1.0, 1.0, 1.0, 0.5), new CornerRadii(10), Insets.EMPTY)));
		return res;
	}

	static HBox createFileChooserAction(RadioButton initialButton, String initialValue, Consumer<File> resultHandler, Action confirmAction, Binding<Boolean> confirmBinding) {
		TextField path = new TextField(initialValue);
		HBox.setHgrow(path, Priority.ALWAYS);
		resultHandler.accept(new File(initialValue));
		Button browse = new Button(UITextValues.FILE_BROWSE_BTN);
		browse.setOnAction(e -> {
			File file = new File(path.getText());
			if (!file.isDirectory()) {
				file = file.getParentFile();
			}
			DirectoryChooser chooser = new DirectoryChooser();
			if (file != null) chooser.setInitialDirectory(file);
			file = chooser.showDialog(null);
			if(file != null) path.setText(file.getAbsolutePath());
		});
		path.textProperty().addListener((observable, oldValue, newValue) -> {
			File file = new File(newValue);
			if(file.exists() && file.isDirectory()) {
				resultHandler.accept(file);
			}
		});
		initialButton.setMinWidth(DefaultValues.UI_BUTTON_MIN_WIDTH);
		HBox res;
		if(confirmAction != null) {
			Button confirmButton = new Button(confirmAction.name);
			confirmButton.setMinWidth(100);
			confirmButton.setOnAction(event -> confirmAction.runnable.run());
			path.disableProperty().bind(Bindings.createBooleanBinding(() -> !initialButton.isSelected(), initialButton.selectedProperty()));
			browse.disableProperty().bind(Bindings.createBooleanBinding(() -> !initialButton.isSelected(), initialButton.selectedProperty()));
			confirmButton.disableProperty().bind(confirmBinding);
			res = new HBox(initialButton, path, browse, confirmButton);
		} else {
			res = new HBox(initialButton, path, browse);
		}
		res.setSpacing(DefaultValues.UI_SPACING);
		return res;
	}

	private Node createLoadCollectionButton() {
		Image img = new Image(getClass().getResourceAsStream("/album-m.png"));
		ImageView imgView = new ImageView(img);
		Button loadCollectionButton = new Button(UITextValues.INSTALLATION_LOAD_COLLECTION_BTN, imgView);
		loadCollectionButton.setPadding(new Insets(10));
		loadCollectionButton.setOnAction(this::updateAndDisplayCollection);
		loadCollectionButton.disableProperty().bind(Bindings.createBooleanBinding(
				() -> !installationState.isAlbumRunning(), installationState.albumRunningProperty()));
		loadCollectionButton.setMaxWidth(Double.MAX_VALUE);
		return loadCollectionButton;
	}

	@Override
	public void redraw()
	{
		getWindow().pack();
	}

	@Override
	public void redoLayout()
	{
		// ignored
	}

	@Override
	public void setLabel(final String s)
	{
		// ignored
	}

	private void updateAndDisplayCollection(ActionEvent event) {
		try {
			installationState.updateIndex(this::collectionUpdated);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void collectionUpdated(CollectionIndexEvent event) {
		SolutionCollection collection = event.getCollection();
		uiService.show(UITextValues.COLLECTION_DISPLAY_NAME, collection);
	}

	static class Action {
		String name;
		Runnable runnable;

		public Action(String name, Runnable runnable) {
			this.name = name;
			this.runnable = runnable;
		}
	}
}
