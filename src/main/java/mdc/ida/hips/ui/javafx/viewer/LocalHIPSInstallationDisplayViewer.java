package mdc.ida.hips.ui.javafx.viewer;

import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import mdc.ida.hips.model.HIPSCatalog;
import mdc.ida.hips.model.HIPSCollection;
import mdc.ida.hips.model.HIPSCollectionUpdatedEvent;
import mdc.ida.hips.model.HIPSServerRunningEvent;
import mdc.ida.hips.model.LocalHIPSInstallation;
import mdc.ida.hips.scijava.ui.javafx.viewer.EasyJavaFXDisplayViewer;
import mdc.ida.hips.service.HIPSServerService;
import mdc.ida.hips.service.conda.CondaEnvironmentDetectedEvent;
import mdc.ida.hips.service.conda.CondaEnvironmentMissingEvent;
import mdc.ida.hips.service.conda.CondaService;
import mdc.ida.hips.service.conda.HasCondaInstalledEvent;
import org.scijava.Context;
import org.scijava.command.CommandService;
import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;
import org.scijava.ui.viewer.DisplayViewer;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * This class displays a {@link LocalHIPSInstallation}.
 */
@Plugin(type = DisplayViewer.class)
public class LocalHIPSInstallationDisplayViewer extends EasyJavaFXDisplayViewer<LocalHIPSInstallation> {

	private final int spacing = 15;
	@Parameter
	private Context context;
	@Parameter
	private CommandService commandService;
	@Parameter
	private LogService logService;
	@Parameter
	private UIService uiService;
	@Parameter
	private EventService eventService;
	@Parameter
	private HIPSServerService hipsService;
	@Parameter
	private CondaService condaService;

	private LocalHIPSInstallation installation;
	private final BooleanProperty hipsRunning = new SimpleBooleanProperty(false);
	private final BooleanProperty condaInstalled = new SimpleBooleanProperty(false);
	private final BooleanProperty condaMissing = new SimpleBooleanProperty(false);
	private final BooleanProperty initialSetupRunning = new SimpleBooleanProperty(false);
	private final BooleanProperty hasHipsEnvironment = new SimpleBooleanProperty(false);
	private final StringProperty hipsCatalog = new SimpleStringProperty();
	private int LABEL_WITH_MIN = 200;

	public LocalHIPSInstallationDisplayViewer() {
		super(LocalHIPSInstallation.class);
		condaInstalled.addListener((observable, oldValue, newValue) -> {
			System.out.println("conda install changed: " + newValue);
		});
	}

	@Override
	protected boolean canView(LocalHIPSInstallation installation) {
		return true;
	}

	@Override
	protected Node createDisplayPanel(LocalHIPSInstallation installation) {
		this.installation = installation;
		hipsCatalog.set(installation.getDefaultCatalog());
		ImageView image = createScaleImage();
		double imgWidth = image.getFitWidth();
		Node welcomeBox = createWelcomeBox(imgWidth);
		VBox content = new VBox(image, welcomeBox);
		content.setAlignment(Pos.CENTER);
		content.setPadding(new Insets(5));
		return content;
	}

	@EventHandler
	private void hipsServerStarted(HIPSServerRunningEvent e) {
		hipsRunning.set(true);
	}

	@EventHandler
	private void hasCondaInstalled(HasCondaInstalledEvent e) {
		condaInstalled.set(true);
		condaMissing.set(false);
	}

	@EventHandler
	private void condaEnvironmentMissing(CondaEnvironmentMissingEvent e) {
		condaMissing.set(true);
	}

	@EventHandler
	private void hasHIPSEnvironment(CondaEnvironmentDetectedEvent e) {
		hasHipsEnvironment.set(true);
	}

	private ImageView createScaleImage() {
		Image img = new Image(getClass().getResourceAsStream("hips-scales.png"));
		ImageView image = new ImageView(img);
		image.setOpacity(0.5);
		return image;
	}

	private Node createWelcomeBox(double imgWidth) {
		HBox appInitializingBox = createWelcomeSubBox(
				new Label("Checking HIPS configuration..")
		);
		HBox initialSetupRunningBox = createWelcomeSubBox(
				new Label("Initial setup running..")
		);
		HBox defaultWelcomeBox = createWelcomeSubBox(
				createInstallationStatus(),
				createLoadCollectionButton()
		);
		VBox initialSetupBox = createWelcomeSubBoxVertial(
				createSetupHipsLabel(),
				createSetupHIPSBox()
		);

		IntegerProperty state = new SimpleIntegerProperty();
		state.bind(Bindings.createIntegerBinding(() -> {
			if(initialSetupRunning.get()) return 2;
			if(!condaMissing.get() && !condaInstalled.get()) return 0;
			if(condaMissing.get()) return 1;
			return 3;
		}, condaInstalled, condaMissing, initialSetupRunning));

//		appInitializingBox.setVisible(!condaInstalled.get() && !condaMissing.get());
		appInitializingBox.visibleProperty().bind(Bindings.createBooleanBinding(() -> state.get() == 0, state));
		appInitializingBox.managedProperty().bind(appInitializingBox.visibleProperty());

//		defaultWelcomeBox.setVisible(condaInstalled.get());
		defaultWelcomeBox.visibleProperty().bind(Bindings.createBooleanBinding(() -> state.get() == 3, state));
		defaultWelcomeBox.managedProperty().bind(defaultWelcomeBox.visibleProperty());

//		initialSetupRunningBox.setVisible(initialSetupRunning.get());
		initialSetupRunningBox.visibleProperty().bind(Bindings.createBooleanBinding(() -> state.get() == 2, state));
		initialSetupRunningBox.managedProperty().bind(initialSetupRunningBox.visibleProperty());

//		initialSetupBox.setVisible(condaMissing.get());
		initialSetupBox.visibleProperty().bind(Bindings.createBooleanBinding(() -> state.get() == 1, state));
		initialSetupBox.managedProperty().bind(initialSetupBox.visibleProperty());

		VBox res = new VBox(appInitializingBox, initialSetupBox, defaultWelcomeBox, initialSetupRunningBox);
		res.setPadding(new Insets(spacing));
		return res;
	}

	private Label createSetupHipsLabel() {
		Label label = new Label("Ready to set up HIPS?");
		label.getStyleClass().add("bold");
		return label;
	}

	private Node createSetupHIPSBox() {
		RadioButton downloadConda = new RadioButton("Download Miniconda to..");
		RadioButton useExistingConda = new RadioButton("Use existing conda:");
		downloadConda.setSelected(true);
		ToggleGroup group = new ToggleGroup();
		group.getToggles().add(downloadConda);
		group.getToggles().add(useExistingConda);
		StringProperty downloadTarget = new SimpleStringProperty();
		String condaPath = "";
		VBox res = new VBox(
				createFileChooserAction(
						downloadConda,
						new File(System.getProperty("user.home"), "miniconda").getAbsolutePath(),
						file -> {
							try {
								validateCondaTarget(downloadTarget, file);
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
							installation.setCondaPath(file1);
							condaService.setDefaultCondaPath(file1);
						}, null, null
				),
				createDefaultCatalogField(),
				createStartHIPSButton(downloadConda, downloadTarget));
		res.setSpacing(spacing);
		res.setAlignment(Pos.BASELINE_CENTER);
		return res;
	}

	private Button createStartHIPSButton(RadioButton downloadConda, StringProperty downloadTarget) {
		Button startHIPSButton = new Button("Let's go!");
		startHIPSButton.setOnAction(event -> {
			new Thread(() -> {
				try {
					initHIPSInstallation(downloadConda, downloadTarget);
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}).start();
		});
		startHIPSButton.getStyleClass().add("bold");
		return startHIPSButton;
	}

	private Node createDefaultCatalogField() {
		TextField textField = new TextField(hipsCatalog.get());
		hipsCatalog.bind(textField.textProperty());
		Label label = new Label("Default catalog:");
		label.setPadding(new Insets(0, spacing, 0, 0));
		HBox.setHgrow(textField, Priority.ALWAYS);
		return new HBox(label, textField);
	}

	private void validateCondaTarget(StringProperty downloadTarget, File file) throws IOException {
		if (file == null) return;
		if (file.exists() && !isDirEmpty(file.toPath())) {
			file = new File(file, "miniconda");
		}
		downloadTarget.set(file.getAbsolutePath());
	}

	private static boolean isDirEmpty(final Path directory) throws IOException {
		try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
			return !dirStream.iterator().hasNext();
		}
	}


	private void initHIPSInstallation(RadioButton downloadConda, StringProperty downloadTarget) throws IOException, InterruptedException {
		initialSetupRunning.set(true);
		if(downloadConda.isSelected()) {
			downloadAndInstallConda(downloadTarget);
			installation.setCondaPath(new File(downloadTarget.get()));
		} else {
			checkCondaLocation();
		}
		if(!condaService.checkIfCondaInstalled(installation.getCondaPath())) {
			logService.error("Cannot find conda in " + installation.getCondaPath());
			return;
		}
		if(!hasHipsEnvironment.get()) {
			condaService.createEnvironment(installation.getCondaPath(), hipsService.getEnvironmentFile());
		}
		if(hipsService.checkIfHIPSEnvironmentExists(installation)) {
			installation.setDefaultCatalog(hipsCatalog.get());
			hipsService.runAsynchronously(installation);
		} else {
			logService.error("Could not install hips environment using conda " + installation.getCondaPath());
		}
		initialSetupRunning.set(false);
	}

	private HBox createWelcomeSubBox(Node... nodes) {
		HBox res = new HBox(nodes);
		res.setAlignment(Pos.CENTER);
		res.setSpacing(spacing);
		res.setPadding(new Insets(spacing));
		res.setBackground(new Background(new BackgroundFill(new Color(1.0, 1.0, 1.0, 0.5), new CornerRadii(10), Insets.EMPTY)));
		return res;
	}

	private VBox createWelcomeSubBoxVertial(Node... nodes) {
		VBox res = new VBox(nodes);
		res.setAlignment(Pos.CENTER);
		res.setSpacing(spacing);
		res.setPadding(new Insets(spacing));
		res.setBackground(new Background(new BackgroundFill(new Color(1.0, 1.0, 1.0, 0.5), new CornerRadii(10), Insets.EMPTY)));
		return res;
	}

	private Node createInstallationStatus() {
		Action action = new Action("Configure HIPS", this::configureHips);
		Text statusText = getStatusText(hipsRunning, "HIPS service running", "HIPS service not running");
		Button btn = new Button(action.name);
		btn.setOnAction(event -> action.runnable.run());
		HBox res = new HBox(statusText, btn);
		res.setSpacing(spacing);
		res.setPadding(new Insets(spacing));
		return res;
	}

	private void configureHips() {
		VBox content = new VBox(
				createCondaStatus(true),
				createHIPSEnvironmentStatus());
		ButtonType startHIPSButtonType = new ButtonType("Start HIPS service", ButtonBar.ButtonData.OK_DONE);
		ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
		Dialog<ButtonType> dialog = new Dialog<>();
		DialogPane pane = new DialogPane();
		pane.setContent(content);
		pane.getButtonTypes().add(cancelButtonType);
		pane.getButtonTypes().add(startHIPSButtonType);
		pane.lookupButton(startHIPSButtonType).disableProperty().bind(Bindings.createBooleanBinding(() -> !condaInstalled.get() || !hasHipsEnvironment.get(), condaInstalled, hasHipsEnvironment));
		dialog.setDialogPane(pane);
		Optional<ButtonType> res = dialog.showAndWait();
		if(!res.isPresent() || res.get().equals(cancelButtonType)) return;
		new Thread(() -> {
			hipsService.runAsynchronously(installation);
		}).start();
	}

	private Text getStatusText(BooleanProperty isOK, String titleOK, String titleNotOK) {
		Text text = new Text();
		text.textProperty().bind(Bindings.createStringBinding(() -> isOK.get()?  "✓ " + titleOK : "❌ " + titleNotOK, isOK));
		return text;
	}

	private Node createHIPSEnvironmentStatus() {
		Text statusText = getStatusText(hasHipsEnvironment, "HIPS environment exists", "HIPS environment does not exist");
		Button btn = new Button("Create Environment");
		btn.setOnAction(event -> {
			try {
				condaService.createEnvironment(installation.getCondaPath(), hipsService.getEnvironmentFile());
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		});
		btn.disableProperty().bind(Bindings.createBooleanBinding(
				() -> !condaInstalled.get() || hasHipsEnvironment.get(),
				condaInstalled, hasHipsEnvironment));
		Region spacer = new Region();
		spacer.setPrefWidth(10);
		spacer.setPrefHeight(10);
		HBox.setHgrow(spacer, Priority.ALWAYS);
		HBox res = new HBox(statusText, spacer, btn);
		HBox.setHgrow(res, Priority.ALWAYS);
		res.setSpacing(spacing);
		res.setPadding(new Insets(spacing));
		return res;
	}

	private Node createCondaStatus(boolean withActions) {
		Text title = withActions?
				getStatusText(condaInstalled, "Conda is accessible", "Conda is not accessible")
				: new Text("Conda");
		RadioButton downloadConda = new RadioButton("Download Miniconda to..");
		RadioButton useExistingConda = new RadioButton("Use existing conda:");
		downloadConda.setSelected(installation.getCondaPath() == null);
		useExistingConda.setSelected(installation.getCondaPath() != null);
		ToggleGroup group = new ToggleGroup();
		group.getToggles().add(downloadConda);
		group.getToggles().add(useExistingConda);
		VBox res = new VBox(title, downloadCondaBox(downloadConda), existingCondaBox(useExistingConda));
		res.setSpacing(spacing);
		res.setPadding(new Insets(spacing));
		res.setBackground(new Background(new BackgroundFill(new Color(1.0, 1.0, 1.0, 0.5), new CornerRadii(10), Insets.EMPTY)));
		return res;
	}

	private HBox downloadCondaBox(RadioButton downloadConda) {
		StringProperty downloadTarget = new SimpleStringProperty();
		return createFileChooserAction(
			downloadConda,
			new File(System.getProperty("user.home"), "miniconda").getAbsolutePath(),
			file -> {
				try {
					validateCondaTarget(downloadTarget, file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			},
			new Action("Download", () -> {
				try {
					downloadAndInstallConda(downloadTarget);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}),
			Bindings.createBooleanBinding(() -> {
				if(downloadTarget.isEmpty().get()) return false;
				File target = new File(downloadTarget.get());
				return !downloadConda.isSelected() && target.exists() && target.isDirectory();
			}, downloadConda.selectedProperty())
		);
	}

	private void downloadAndInstallConda(StringProperty downloadTarget) throws IOException {
		if(downloadTarget.isEmpty().get()) return;
		File condaPath = new File(downloadTarget.get());
		installation.setCondaPath(condaPath);
		condaService.setDefaultCondaPath(condaPath);
		condaService.installConda(condaPath);
		condaInstalled.set(condaService.checkIfCondaInstalled(condaPath));
	}

	private HBox existingCondaBox(RadioButton useExistingConda) {
		String condaPath = "";
		if(installation.getCondaPath() != null && installation.getCondaPath().exists()) {
			condaPath = installation.getCondaPath().getAbsolutePath();
		}
		return createFileChooserAction(
			useExistingConda,
				condaPath,
			file -> {
				installation.setCondaPath(file);
				condaService.setDefaultCondaPath(file);
			}, new Action("Confirm", () -> {
					checkCondaLocation();
				}),
			Bindings.createBooleanBinding(() -> !useExistingConda.isSelected(), useExistingConda.selectedProperty())
		);
	}

	private void checkCondaLocation() {
		condaInstalled.set(condaService.checkIfCondaInstalled(installation.getCondaPath()));
		if(condaInstalled.get()) {
			hasHipsEnvironment.set(hipsService.checkIfHIPSEnvironmentExists(installation));
		}
	}

	private HBox createFileChooserAction(RadioButton initialButton, String initialValue, Consumer<File> resultHandler, Action confirmAction, Binding<Boolean> confirmBinding) {
		TextField path = new TextField(initialValue);
		HBox.setHgrow(path, Priority.ALWAYS);
		resultHandler.accept(new File(initialValue));
		Button browse = new Button("Browse");
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
		initialButton.setMinWidth(LABEL_WITH_MIN);
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
		res.setSpacing(spacing);
		return res;
	}

	private Node createLoadCollectionButton() {
		Button loadCollectionButton = new Button("Load collection");
		loadCollectionButton.setOnAction(this::updateAndDisplayCollection);
		loadCollectionButton.disableProperty().bind(Bindings.createBooleanBinding(() -> !hipsRunning.get(), hipsRunning));
		VBox res = new VBox(loadCollectionButton);
		res.setPadding(new Insets(spacing));
		return res;
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
			hipsService.updateIndex(this::collectionUpdated);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void collectionUpdated(HIPSCollectionUpdatedEvent event) {
		HIPSCollection collection = event.getCollection();
		for (HIPSCatalog catalog : collection) {
			uiService.show(catalog.getName(), catalog);
		}
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
