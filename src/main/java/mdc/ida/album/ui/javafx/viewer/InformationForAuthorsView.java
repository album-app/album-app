package mdc.ida.album.ui.javafx.viewer;

import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import mdc.ida.album.UITextValues;

import java.io.File;

import static mdc.ida.album.ui.javafx.viewer.JavaFXUtils.asConsoleTextFlow;
import static mdc.ida.album.ui.javafx.viewer.JavaFXUtils.asTextFlow;
import static mdc.ida.album.ui.javafx.viewer.JavaFXUtils.createExpandable;
import static mdc.ida.album.ui.javafx.viewer.JavaFXUtils.makePretty;

public class InformationForAuthorsView extends VBox {

	private final InstallationState installationState;

	public InformationForAuthorsView(InstallationState installationState) {
		this.installationState = installationState;
		makePretty(this);
		Node createCatalog = createExpandable(UITextValues.INSTALLATION_AUTHORS_CREATE_CATALOG_TITLE, createSetupCatalogBox());
		Node createSolution = createExpandable(UITextValues.INSTALLATION_AUTHORS_CREATE_SOLUTION_TITLE, createSetupSolutionBox());
		Node shareCatalog = createExpandable(UITextValues.INSTALLATION_AUTHORS_SHARE_CATALOG_TITLE, createShareSolutionBox());
		getChildren().addAll(createCatalog, createSolution, shareCatalog);
	}

	private Node createShareSolutionBox() {
		return makePretty(new VBox(
				asTextFlow(UITextValues.AUTHORS_SHARE_CATALOG_TEXT)
		));
	}

	private Node createSetupSolutionBox() {
		return makePretty(new VBox(
				asTextFlow(UITextValues.AUTHORS_ACTIVATE_ENVIRONMENT_TEXT),
				getActivateEnvironmentBox(),
				asTextFlow(UITextValues.AUTHORS_SETUP_SOLUTIONS_TEXT),
				asConsoleTextFlow(UITextValues.AUTHORS_SETUPS_SOLUTIONS_CODE),
				asTextFlow(UITextValues.AUTHORS_TEST_SOLUTIONS_TEXT),
				asConsoleTextFlow(UITextValues.AUTHORS_TEST_SOLUTIONS_CODE),
				asTextFlow(UITextValues.AUTHORS_DEPLOY_SOLUTIONS_TEXT),
				asConsoleTextFlow(UITextValues.AUTHORS_DEPLOY_SOLUTIONS_CODE)
		));
	}

	private Node createSetupCatalogBox() {
		return makePretty(new VBox(
				asTextFlow(UITextValues.AUTHORS_ACTIVATE_ENVIRONMENT_TEXT),
				getActivateEnvironmentBox(),
				asTextFlow(UITextValues.AUTHORS_SETUP_CATALOG_TEXT),
				asConsoleTextFlow(UITextValues.AUTHORS_SETUP_CATALOG_CODE)
		));
	}

	private VBox getActivateEnvironmentBox() {
		String condaIncludePath = new File(installationState.getCondaPath()).getParent();
		String condaEnvironmentPath = installationState.getAlbumEnvironmentPath();
		return asConsoleTextFlow(UITextValues.getActivateEnvironmentCode(condaIncludePath, condaEnvironmentPath));
	}

}
