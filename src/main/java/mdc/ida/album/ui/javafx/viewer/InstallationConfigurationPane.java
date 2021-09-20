package mdc.ida.album.ui.javafx.viewer;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import mdc.ida.album.DefaultValues;
import mdc.ida.album.UITextValues;
import mdc.ida.album.model.ServerProperties;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import static mdc.ida.album.ui.javafx.viewer.JavaFXUtils.createExpandable;
import static mdc.ida.album.ui.javafx.viewer.JavaFXUtils.createStatefulExpandable;
import static mdc.ida.album.ui.javafx.viewer.JavaFXUtils.createStatusBinding;
import static mdc.ida.album.ui.javafx.viewer.JavaFXUtils.makePretty;
import static mdc.ida.album.ui.javafx.viewer.LocalAlbumInstallationDisplayViewer.createFileChooserAction;

public class InstallationConfigurationPane extends VBox {

	private final InstallationState installationState;
	private VBox serverPropertiesBox;

	public InstallationConfigurationPane(InstallationState installationState) {
		this.installationState = installationState;
		getChildren().addAll(
				createCondaStatus(),
				createAlbumEnvironmentStatus(),
				createAlbumServerStatus(),
				new Separator(),
				createLauncherProperties(),
				updateServerProperties());

		setSpacing(DefaultValues.UI_SPACING);
		setBackground(Background.EMPTY);
		VBox.setVgrow(this, Priority.ALWAYS);
		HBox.setHgrow(this, Priority.ALWAYS);

		installationState.albumRunningProperty().addListener((observable, oldValue, newValue) -> {
			updateServerProperties();
		});
}

	private Node createLauncherProperties() {
		VBox res = new VBox(
				addResettableLine(UITextValues.INSTALLATION_CONDA_PATH_LABEL, installationState.getCondaPath(), this::resetCondaPath, null),
				addResettableLine(UITextValues.INSTALLATION_ALBUM_ENVIRONMENT_PATH_LABEL, installationState.getAlbumEnvironmentPath(), null, this::deleteEnvironment)
		);
		makePretty(res);
		return createExpandable(UITextValues.INSTALLATION_LAUNCHER_PROPERTIES_TITLE, res);
	}

	private void deleteEnvironment() {
		installationState.closeServer();
		try {
			installationState.removeEnvironment();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	private Node updateServerProperties() {
		if(serverPropertiesBox != null) {
			serverPropertiesBox.getChildren().clear();
		} else {
			serverPropertiesBox = new VBox();
			makePretty(serverPropertiesBox);
		}
		try {
			ServerProperties serverProperties = installationState.getServerProperties();
			if(serverProperties != null) {
				serverProperties.forEach((name, value) -> {
//					if(!new File(value).exists()) return;
					serverPropertiesBox.getChildren().add(addResettableLine(name, value, null, () -> {
						try {
							FileUtils.deleteDirectory(new File(value));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}));
				});
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return createExpandable(UITextValues.INSTALLATION_SERVER_PROPERTIES_TITLE, serverPropertiesBox);
	}

	private Node createTitle(String title) {
		Label label = new Label(title);
		label.getStyleClass().add("bold");
		return label;
	}

	private Node addResettableLine(String name, String value, Runnable onReset, Runnable onDelete) {
		Label nameLabel = new Label(name);
		nameLabel.setMinWidth(200);
		TextField valueLabel = new TextField(value);
		valueLabel.setEditable(false);
		HBox.setHgrow(valueLabel, Priority.ALWAYS);
		HBox box = new HBox(
				nameLabel,
				valueLabel
		);
		box.setSpacing(DefaultValues.UI_SPACING);
		HBox.setHgrow(box, Priority.ALWAYS);
		if(onReset != null) {
			Button btn = new Button(UITextValues.INSTALLATION_RESET_BTN);
			btn.setOnAction(e -> onReset.run());
			box.getChildren().add(btn);
		}
		if(onDelete != null) {
			Button btn = new Button(UITextValues.INSTALLATION_DELETE_BTN);
			btn.setOnAction(e -> onDelete.run());
			box.getChildren().add(btn);
		}
		return box;
	}

	private void resetCondaPath() {
		installationState.resetCondaPath();
	}

	private Node createAlbumEnvironmentStatus() {
		Button btn = new Button(UITextValues.INSTALLATION_CREATE_ENVIRONMENT_BTN);
		btn.setOnAction(event -> {
			new Thread(() -> {
				try {
					installationState.createEnvironment();
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}).start();
		});
		btn.disableProperty().bind(Bindings.createBooleanBinding(
				() -> !installationState.isCondaInstalled() || installationState.isHasAlbumEnvironment(),
				installationState.condaInstalledProperty(), installationState.hasAlbumEnvironmentProperty()));
		StringBinding titleBinding = createStatusBinding(installationState.hasAlbumEnvironmentProperty(), "album environment exists", "album environment does not exist");
		VBox expandable = makePretty(new VBox(btn));
		return createStatefulExpandable(titleBinding, installationState.hasAlbumEnvironmentProperty(), expandable);
	}

	private Node createAlbumServerStatus() {
		Button startAlbumButton = new Button(UITextValues.INSTALLATION_START_ALBUM_SERVER_BTN);
		startAlbumButton.disableProperty().bind(Bindings.createBooleanBinding(
				() -> !installationState.condaInstalledProperty().get()
						|| !installationState.hasAlbumEnvironmentProperty().get()
						|| installationState.isAlbumRunning(),
				installationState.condaInstalledProperty(),
				installationState.albumRunningProperty(),
				installationState.hasAlbumEnvironmentProperty()));
		startAlbumButton.setOnAction(event -> installationState.runServer());
		VBox expandable = makePretty(new VBox(startAlbumButton));
		StringBinding binding = createStatusBinding(installationState.albumRunningProperty(), "album server running", "album server not running");
		return createStatefulExpandable(binding, installationState.albumRunningProperty(), expandable);
	}

	private Node createCondaStatus() {
		RadioButton downloadConda = new RadioButton(UITextValues.INSTALLATION_DOWNLOAD_CONDA_BTN);
		RadioButton useExistingConda = new RadioButton(UITextValues.INSTALLATION_EXISTING_CONDA_BTN);
		downloadConda.setSelected(installationState.getInstallation().getCondaPath() == null);
		useExistingConda.setSelected(installationState.getInstallation().getCondaPath() != null);
		ToggleGroup group = new ToggleGroup();
		group.getToggles().add(downloadConda);
		group.getToggles().add(useExistingConda);
		VBox expandable = makePretty(
				new VBox(downloadCondaBox(downloadConda), existingCondaBox(useExistingConda))
		);
		StringBinding binding = createStatusBinding(
				installationState.condaInstalledProperty(),
				UITextValues.CONFIGURATION_CONDA_ACCESSIBLE,
				UITextValues.CONFIGURATION_CONDA_NOT_ACCESSIBLE);
		return createStatefulExpandable(binding, installationState.condaInstalledProperty(), expandable);
	}

	private HBox existingCondaBox(RadioButton useExistingConda) {
		String condaPath = installationState.getCondaPath();
		return createFileChooserAction(
				useExistingConda,
				condaPath,
				installationState::setCondaPath, new LocalAlbumInstallationDisplayViewer.Action("Confirm", installationState::checkCondaLocation),
				Bindings.createBooleanBinding(() -> !useExistingConda.isSelected(), useExistingConda.selectedProperty())
		);
	}

	private HBox downloadCondaBox(RadioButton downloadConda) {
		StringProperty downloadTarget = new SimpleStringProperty();
		return createFileChooserAction(
				downloadConda,
				new File(System.getProperty("user.home"), "miniconda").getAbsolutePath(),
				file -> {
					try {
						installationState.validateCondaTarget(downloadTarget, file);
					} catch (IOException e) {
						e.printStackTrace();
					}
				},
				new LocalAlbumInstallationDisplayViewer.Action("Download", () -> {
					try {
						installationState.downloadAndInstallConda(downloadTarget);
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
}
