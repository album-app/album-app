package mdc.ida.hips.ui.javafx.viewer;

import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
import mdc.ida.hips.model.HIPSServerRunningEvent;
import mdc.ida.hips.model.HIPSCatalog;
import mdc.ida.hips.model.HIPSCollection;
import mdc.ida.hips.model.HIPSCollectionUpdatedEvent;
import mdc.ida.hips.service.conda.CondaService;
import mdc.ida.hips.service.conda.HasCondaInstalledEvent;
import mdc.ida.hips.service.conda.CondaEnvironmentDetectedEvent;
import mdc.ida.hips.model.LocalHIPSInstallation;
import mdc.ida.hips.scijava.ui.javafx.viewer.EasyJavaFXDisplayViewer;
import mdc.ida.hips.service.HIPSServerService;
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
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * This class displays a {@link LocalHIPSInstallation}.
 */
@Plugin(type = DisplayViewer.class)
public class LocalHIPSInstallationDisplayViewer extends EasyJavaFXDisplayViewer<LocalHIPSInstallation> {

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
	private final BooleanProperty hasHipsEnvironment = new SimpleBooleanProperty(false);

	public LocalHIPSInstallationDisplayViewer() {
		super(LocalHIPSInstallation.class);
	}

	@Override
	protected boolean canView(LocalHIPSInstallation installation) {
		return true;
	}

	@Override
	protected Node createDisplayPanel(LocalHIPSInstallation installation) {
		this.installation = installation;
		ImageView image = createScaleImage();
		double imgWidth = image.getFitWidth();
		HBox welcomeBox = createWelcomeBox(imgWidth);
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

	private HBox createWelcomeBox(double imgWidth) {
		HBox welcomeBox = new HBox(
				createInstallationStatus(),
				createLoadCollectionButton()
		);
		welcomeBox.setAlignment(Pos.CENTER);
		welcomeBox.setSpacing(15);
//		welcomeBox.setPrefWidth(imgWidth);
//		welcomeBox.setMaxWidth(imgWidth);
		welcomeBox.setPadding(new Insets(15));
//		welcomeBox.setBackground(new Background(new BackgroundFill(new Color(1.0, 1.0, 1.0, 0.5), new CornerRadii(10), Insets.EMPTY)));
		return welcomeBox;
	}

	private Node createInstallationStatus() {
		Action action = new Action("Configure HIPS", this::configureHips);
		Text statusText = getStatusText(hipsRunning, "HIPS service running", "HIPS service not running");
		Button btn = new Button(action.name);
		btn.setOnAction(event -> action.runnable.run());
		HBox res = new HBox(statusText, btn);
		res.setSpacing(15);
		res.setPadding(new Insets(15));
		return res;
	}

	private void configureHips() {
		VBox content = new VBox(
				createCondaStatus(),
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
			try {
				hipsService.runAsynchronously(installation);
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
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
		res.setSpacing(15);
		res.setPadding(new Insets(15));
		return res;
	}

	private Node createCondaStatus() {
		Text title = getStatusText(condaInstalled, "Conda is accessible", "Conda is not accessible");
		RadioButton downloadConda = new RadioButton("Download Miniconda to..");
		RadioButton useExistingConda = new RadioButton("Use existing conda:");
		downloadConda.setSelected(installation.getCondaPath() == null);
		useExistingConda.setSelected(installation.getCondaPath() != null);
		ToggleGroup group = new ToggleGroup();
		group.getToggles().add(downloadConda);
		group.getToggles().add(useExistingConda);
		VBox res = new VBox(title, downloadCondaBox(downloadConda), existingCondaBox(useExistingConda));
		res.setSpacing(15);
		res.setPadding(new Insets(15));
		res.setBackground(new Background(new BackgroundFill(new Color(1.0, 1.0, 1.0, 0.5), new CornerRadii(10), Insets.EMPTY)));
		return res;
	}

	private HBox downloadCondaBox(RadioButton downloadConda) {
		StringProperty downloadTarget = new SimpleStringProperty();
		return createFileChooserAction(
			downloadConda,
				new File(System.getProperty("user.home"), "miniconda").getAbsolutePath(),
			file -> {
				if(file == null) return;
				if(Objects.requireNonNull(file.listFiles()).length > 0) {
					file = new File(file, "miniconda");
				}
				downloadTarget.set(file.getAbsolutePath());
			}, new Action("Download", () -> {
				try {
					if(downloadTarget.isEmpty().get()) return;
					File condaPath = new File(downloadTarget.get());
					installation.setCondaPath(condaPath);
					condaService.setDefaultCondaPath(condaPath);
					condaService.installConda(condaPath);
					condaInstalled.set(condaService.checkIfCondaInstalled(condaPath));
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
				condaInstalled.set(condaService.checkIfCondaInstalled(installation.getCondaPath()));
				if(condaInstalled.get()) {
					hasHipsEnvironment.set(hipsService.checkIfHIPSEnvironmentExists(installation));
				}
			}),
			Bindings.createBooleanBinding(() -> !useExistingConda.isSelected(), useExistingConda.selectedProperty())
		);
	}

	private HBox createFileChooserAction(RadioButton initialButton, String initialValue, Consumer<File> resultHandler, Action confirmAction, Binding<Boolean> confirmBinding) {
		TextField path = new TextField(initialValue);
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
		Button confirmButton = new Button(confirmAction.name);
		confirmButton.setOnAction(event -> confirmAction.runnable.run());
		initialButton.setMinWidth(200);
		confirmButton.setMinWidth(100);
		path.disableProperty().bind(Bindings.createBooleanBinding(() -> !initialButton.isSelected(), initialButton.selectedProperty()));
		browse.disableProperty().bind(Bindings.createBooleanBinding(() -> !initialButton.isSelected(), initialButton.selectedProperty()));
		confirmButton.disableProperty().bind(confirmBinding);
		HBox res = new HBox(initialButton, path, browse, confirmButton);
		res.setSpacing(15);
		return res;
	}

	private Node createLoadCollectionButton() {
		Button loadCollectionButton = new Button("Load collection");
		loadCollectionButton.setOnAction(this::updateAndDisplayCollection);
		loadCollectionButton.disableProperty().bind(Bindings.createBooleanBinding(() -> !hipsRunning.get(), hipsRunning));
		VBox res = new VBox(loadCollectionButton);
		res.setPadding(new Insets(15));
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
