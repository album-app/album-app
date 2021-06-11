package mdc.ida.hips.ui.javafx.viewer;

import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import mdc.ida.hips.model.HIPSCatalog;
import mdc.ida.hips.model.HIPSCollection;
import mdc.ida.hips.model.HIPSCollectionUpdatedEvent;
import mdc.ida.hips.model.LocalHIPSInstallation;
import mdc.ida.hips.scijava.ui.javafx.viewer.EasyJavaFXDisplayViewer;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;
import org.scijava.ui.viewer.DisplayViewer;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * This class displays a {@link LocalHIPSInstallation}.
 */
@Plugin(type = DisplayViewer.class)
public class LocalHIPSInstallationDisplayViewer extends EasyJavaFXDisplayViewer<LocalHIPSInstallation> {

	private static final int spacing = 15;
	@Parameter
	private Context context;
	@Parameter
	private UIService uiService;

	private InstallationState installationState;
	private static int LABEL_WITH_MIN = 200;

	public LocalHIPSInstallationDisplayViewer() {
		super(LocalHIPSInstallation.class);
	}

	@Override
	protected boolean canView(LocalHIPSInstallation installation) {
		return true;
	}

	@Override
	protected Node createDisplayPanel(LocalHIPSInstallation installation) {
		installationState = new InstallationState(installation);
		context.inject(installationState);
		ImageView image = createScaleImage();
		double imgWidth = image.getFitWidth();
		Node welcomeBox = createWelcomeBox(imgWidth);
		VBox content = new VBox(image, welcomeBox);
		content.setAlignment(Pos.CENTER);
		content.setPadding(new Insets(5));
		return content;
	}

	private ImageView createScaleImage() {
		Image img = new Image(getClass().getResourceAsStream("hips-scales.png"));
		ImageView image = new ImageView(img);
		image.setOpacity(0.5);
		return image;
	}

	private Node createWelcomeBox(double imgWidth) {
		Node appInitializingBox = createWelcomeSubBox(
				new Label("Checking HIPS configuration..")
		);
		Node initialSetupRunningBox = createWelcomeSubBox(
				new Label("Initial setup running..")
		);
		Node defaultWelcomeBox = createWelcomeSubBox(
				createInstallationStatus(),
				createLoadCollectionButton()
		);
		Node initialSetupBox = createWelcomeSubBoxVertial(
				createSetupHipsLabel(),
				createSetupHIPSBox()
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

		VBox res = new VBox(appInitializingBox, initialSetupBox, defaultWelcomeBox, initialSetupRunningBox);
		res.setPadding(new Insets(spacing));
		return res;
	}

	private void bindToState(IntegerProperty state, int i, Node node) {
		node.visibleProperty().bind(Bindings.createBooleanBinding(() -> state.get() == i, state));
		node.managedProperty().bind(node.visibleProperty());
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
					installationState.initHIPSInstallation(downloadConda.isSelected(), downloadTarget);
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}).start();
		});
		startHIPSButton.getStyleClass().add("bold");
		return startHIPSButton;
	}

	private Node createDefaultCatalogField() {
		TextField textField = new TextField(installationState.getHipsCatalog());
		installationState.hipsCatalogProperty().bind(textField.textProperty());
		Label label = new Label("Default catalog:");
		label.setPadding(new Insets(0, spacing, 0, 0));
		HBox.setHgrow(textField, Priority.ALWAYS);
		return new HBox(label, textField);
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
		Text statusText = getStatusText(installationState.hipsRunningProperty(), "HIPS service running", "HIPS service not running");
		Button btn = new Button(action.name);
		btn.setOnAction(event -> action.runnable.run());
		HBox res = new HBox(statusText, btn);
		res.setSpacing(spacing);
		res.setPadding(new Insets(spacing));
		return res;
	}

	private void configureHips() {
		new InstallationConfigurationWindow(installationState).show();
	}

	static Text getStatusText(BooleanProperty isOK, String titleOK, String titleNotOK) {
		Text text = new Text();
		text.textProperty().bind(Bindings.createStringBinding(() -> isOK.get()?  "✓ " + titleOK : "❌ " + titleNotOK, isOK));
		return text;
	}

	static HBox createFileChooserAction(RadioButton initialButton, String initialValue, Consumer<File> resultHandler, Action confirmAction, Binding<Boolean> confirmBinding) {
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
		loadCollectionButton.disableProperty().bind(Bindings.createBooleanBinding(
				() -> !installationState.isHipsRunning(), installationState.hipsRunningProperty()));
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
			installationState.updateIndex(this::collectionUpdated);
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
