package mdc.ida.hips.ui.javafx.viewer;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import mdc.ida.hips.model.ServerProperties;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import static mdc.ida.hips.ui.javafx.viewer.LocalHIPSInstallationDisplayViewer.createFileChooserAction;
import static mdc.ida.hips.ui.javafx.viewer.LocalHIPSInstallationDisplayViewer.getStatusText;

public class InstallationConfigurationWindow extends Stage {

	private final InstallationState installationState;
	private final int spacing = 15;
	private VBox serverPropertiesBox;

	public InstallationConfigurationWindow(InstallationState installationState) {
		this.installationState = installationState;
		VBox content = new VBox(
				createCondaStatus(),
				createHIPSEnvironmentStatus(),
				createHIPSServerStatus(),
				createLauncherProperties(),
				updateServerProperties());

		content.setSpacing(spacing);
		content.setPadding(new Insets(spacing));
		HBox.setHgrow(content,Priority.ALWAYS);

		installationState.hipsRunningProperty().addListener((observable, oldValue, newValue) -> {
			updateServerProperties();
		});

		ScrollPane root = new ScrollPane(content);
		root.setFitToWidth(true);
		Scene secondScene = new Scene(root, 700, 500);

		setTitle("HIPS Configuration");
		setScene(secondScene);
		show();
	}

	private Node createLauncherProperties() {
		VBox res = new VBox(
				createTitle("Launcher properties"),
				addResettableLine("Conda location", installationState.getCondaPath(), this::resetCondaPath, null),
				addResettableLine("HIPS environment location", installationState.getHipsEnvironmentPath(), null, this::deleteEnvironment)
		);
		makePretty(res);
		return res;
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
			serverPropertiesBox.getChildren().remove(1, serverPropertiesBox.getChildren().size());
		} else {
			serverPropertiesBox = new VBox(
				createTitle("Server properties")
			);
			makePretty(serverPropertiesBox);

		}
		try {
			ServerProperties serverProperties = installationState.getServerProperties();
			if(serverProperties != null) {
				serverProperties.forEach((name, value) -> {
					if(!new File(value).exists()) return;
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
		return serverPropertiesBox;
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
		box.setSpacing(spacing);
		HBox.setHgrow(box, Priority.ALWAYS);
		if(onReset != null) {
			Button btn = new Button("reset");
			btn.setOnAction(e -> onReset.run());
			box.getChildren().add(btn);
		}
		if(onDelete != null) {
			Button btn = new Button("delete");
			btn.setOnAction(e -> onDelete.run());
			box.getChildren().add(btn);
		}
		return box;
	}

	private void resetCondaPath() {
		installationState.resetCondaPath();
	}

	private Node createHIPSEnvironmentStatus() {
		Text statusText = getStatusText(installationState.hasHipsEnvironmentProperty(), "HIPS environment exists", "HIPS environment does not exist");
		Button btn = new Button("Create Environment");
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
				() -> !installationState.isCondaInstalled() || installationState.isHasHipsEnvironment(),
				installationState.condaInstalledProperty(), installationState.hasHipsEnvironmentProperty()));
		return createStatusActionBox(statusText, btn);
	}

	private HBox createStatusActionBox(Text statusText, Button btn) {
		HBox res = new HBox(statusText, createHorizontalSpacer(), btn);
		HBox.setHgrow(res, Priority.ALWAYS);
		makePretty(res);
		return res;
	}

	private Region createHorizontalSpacer() {
		Region spacer = new Region();
		spacer.setPrefWidth(10);
		spacer.setPrefHeight(10);
		HBox.setHgrow(spacer, Priority.ALWAYS);
		return spacer;
	}

	private Node createHIPSServerStatus() {
		Text statusText = getStatusText(installationState.hipsRunningProperty(), "HIPS server running", "HIPS server not running");
		Button startHIPSButton = new Button("Start HIPS service");
		startHIPSButton.disableProperty().bind(Bindings.createBooleanBinding(
				() -> !installationState.condaInstalledProperty().get()
						|| !installationState.hasHipsEnvironmentProperty().get()
						|| installationState.isHipsRunning(),
				installationState.condaInstalledProperty(),
				installationState.hipsRunningProperty(),
				installationState.hasHipsEnvironmentProperty()));
		startHIPSButton.setOnAction(event -> installationState.runServer());
		return createStatusActionBox(statusText, startHIPSButton);
	}

	private Node createCondaStatus() {
		Text title = getStatusText(installationState.condaInstalledProperty(), "Conda is accessible", "Conda is not accessible");
		RadioButton downloadConda = new RadioButton("Download Miniconda to..");
		RadioButton useExistingConda = new RadioButton("Use existing conda:");
		downloadConda.setSelected(installationState.getInstallation().getCondaPath() == null);
		useExistingConda.setSelected(installationState.getInstallation().getCondaPath() != null);
		ToggleGroup group = new ToggleGroup();
		group.getToggles().add(downloadConda);
		group.getToggles().add(useExistingConda);
		VBox res = new VBox(title, downloadCondaBox(downloadConda), existingCondaBox(useExistingConda));
		makePretty(res);
		return res;
	}

	private void makePretty(VBox res) {
		res.setSpacing(spacing);
		res.setPadding(new Insets(spacing));
		res.setBackground(new Background(new BackgroundFill(new Color(1.0, 1.0, 1.0, 0.5), new CornerRadii(10), Insets.EMPTY)));
		VBox.setVgrow(res, Priority.ALWAYS);
	}

	private void makePretty(HBox res) {
		res.setSpacing(spacing);
		res.setPadding(new Insets(spacing));
		res.setBackground(new Background(new BackgroundFill(new Color(1.0, 1.0, 1.0, 0.5), new CornerRadii(10), Insets.EMPTY)));
		VBox.setVgrow(res, Priority.ALWAYS);
	}

	private HBox existingCondaBox(RadioButton useExistingConda) {
		String condaPath = installationState.getCondaPath();
		return createFileChooserAction(
				useExistingConda,
				condaPath,
				installationState::setCondaPath, new LocalHIPSInstallationDisplayViewer.Action("Confirm", installationState::checkCondaLocation),
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
				new LocalHIPSInstallationDisplayViewer.Action("Download", () -> {
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
