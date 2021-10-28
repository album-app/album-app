package mdc.ida.album.ui.javafx.viewer;

import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import mdc.ida.album.DefaultValues;
import mdc.ida.album.UITextValues;
import mdc.ida.album.model.LocalAlbumInstallation;
import mdc.ida.album.model.Solution;
import mdc.ida.album.model.event.SolutionLaunchFinishedEvent;
import mdc.ida.album.model.event.SolutionLaunchRequestEvent;
import mdc.ida.album.scijava.ui.javafx.viewer.EasyJavaFXDisplayViewer;
import mdc.ida.album.control.AlbumServerService;
import org.scijava.Context;
import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.viewer.DisplayViewer;

import java.io.File;
import java.net.MalformedURLException;

import static mdc.ida.album.DefaultValues.UI_SPACING;
import static mdc.ida.album.ui.javafx.viewer.JavaFXUtils.makePretty;

/**
 * This class displays a {@link Solution}.
 */
@Plugin(type = DisplayViewer.class)
public class SolutionDisplayViewer extends EasyJavaFXDisplayViewer<Solution> {

	@Parameter
	private Context context;

	@Parameter
	private EventService eventService;

	@Parameter
	private AlbumServerService albumServerService;

	public SolutionDisplayViewer() {
		super(Solution.class);
	}

	@Override
	protected boolean canView(Solution solution) {
		return true;
	}

	@Override
	protected Node createDisplayPanel(Solution solution) {
		Node actions = createActions(solution);
		HBox header = createContentHeader(solution);
		Node content = createContent(solution);
		VBox.setVgrow(content, Priority.ALWAYS);
		VBox res = new VBox(header, content);
		VBox.setVgrow(res, Priority.ALWAYS);
		ScrollPane scrollPane = new ScrollPane(res);
		VBox.setVgrow(scrollPane, Priority.ALWAYS);
		HBox.setHgrow(scrollPane, Priority.ALWAYS);
		scrollPane.setBackground(null);
		scrollPane.setBorder(null);
		scrollPane.setFitToWidth(true);
//		scrollPane.getStyleClass().add("transparent");
		res.setBackground(new Background(new BackgroundFill(Paint.valueOf("#f4f4f4"), CornerRadii.EMPTY, Insets.EMPTY)));
		scrollPane.setBackground(new Background(new BackgroundFill(Paint.valueOf("#f4f4f4"), CornerRadii.EMPTY, Insets.EMPTY)));
		VBox.setVgrow(actions, Priority.ALWAYS);
		HBox panel = new HBox(scrollPane, actions);
		VBox.setVgrow(panel, Priority.ALWAYS);
		return panel;
	}

	private Node createContent(Solution solution) {
		Node citation = getCitationTexts(solution.getCite(), UITextValues.SOLUTION_VIEW_CITE_LABEL, UITextValues.SOLUTION_VIEW_CITE_PLACEHOLDER);
		Node authors = getTexts(solution.getAuthors(), UITextValues.SOLUTION_VIEW_AUTHORS_LABEL, UITextValues.SOLUTION_VIEW_AUTHORS_PLACEHOLDER);
		Node documentation = getText(solution.getDocumentation(), UITextValues.SOLUTION_VIEW_DOCUMENTATION_LABEL, UITextValues.SOLUTION_VIEW_DOCUMENTATION_PLACEHOLDER);
		Node license = getText(solution.getLicense(), UITextValues.SOLUTION_VIEW_LICENSE_LABEL, UITextValues.SOLUTION_VIEW_LICENSE_PLACEHOLDER);
		VBox content = new VBox(citation, authors, license, documentation);
		content.setSpacing(UI_SPACING);
		content.setPadding(new Insets(UI_SPACING));
		return content;
	}

	private HBox createContentHeader(Solution solution) {
		Image cover = getCover(solution);
		Label title = new Label(solution.getTitle() != null? solution.getTitle() : solution.getName());
		title.setFont(DefaultValues.FONT_TITLE);
		title.setWrapText(true);
		Label idText = new Label(solution.getGroup() + UITextValues.SOLUTION_VIEW_ID_SEPARATOR
				+ solution.getName() + UITextValues.SOLUTION_VIEW_ID_SEPARATOR + UITextValues.SOLUTION_VIEW_ID_PRE_VERSION + solution.getVersion());
		idText.setWrapText(true);
		HBox idBox = new HBox(idText);
		idBox.setPadding(new Insets(10, 0, 10, 0));
		idBox.getStyleClass().add("solution-header-id");
		Label description = getText(solution.getDescription(), UITextValues.SOLUTION_VIEW_DESCRIPTION_LABEL, UITextValues.SOLUTION_VIEW_DESCRIPTION_PLACEHOLDER);
		VBox headerText = new VBox(title, idBox, description);
		HBox header = new HBox(headerText);
		header.getStyleClass().add("solution-header");
		if(cover != null) {
			headerText.setBackground(new Background(new BackgroundFill(Paint.valueOf("#f4f4f4"), new CornerRadii(5), new Insets(-UI_SPACING))));
			BackgroundImage bgImage = new BackgroundImage(
					cover,
					BackgroundRepeat.NO_REPEAT,
					BackgroundRepeat.NO_REPEAT,
					BackgroundPosition.DEFAULT,
					new BackgroundSize(1.0, 1.0, true, true, false, true)
			);
			header.setBackground(new Background(bgImage));
			headerText.getStyleClass().add("solution-header-text");
		} else {
			headerText.setBackground(new Background(new BackgroundFill(Paint.valueOf("#ffffff"), new CornerRadii(5), new Insets(-UI_SPACING))));
			header.setBackground(new Background(new BackgroundFill(Paint.valueOf("#ffffff"), CornerRadii.EMPTY, Insets.EMPTY)));
		}
		return header;
	}

	private Image getCover(Solution solution) {
		Image cover = null;
		if(solution.isInstalled() && LocalAlbumInstallation.class.isAssignableFrom(solution.getInstallation().getClass())) {
			String coverPath = albumServerService.getCoverPath(solution, (LocalAlbumInstallation) solution.getInstallation());
			if(coverPath != null && new File(coverPath).exists()) {
				try {
					cover = new Image(new File(coverPath).toURI().toURL().toString());
				} catch (MalformedURLException ignored) {
				}
			}
		}
		return cover;
	}

	private Node getTexts(ObservableList<String> list, String label, String placeholder) {
		Label titleLabel = new Label(label);
		titleLabel.setFont(DefaultValues.FONT_SECOND_TITLE);
		VBox res = new VBox(titleLabel);
		if(list.size() > 0) {
			for (String item : list) {
				res.getChildren().add(new Label(item));
			}
		} else {
			res.getChildren().add(new Label(placeholder));
		}
		return res;
	}

	private Node getCitationTexts(ObservableList<Solution.Citation> list, String label, String placeholder) {
		Label titleLabel = new Label(label);
		titleLabel.setFont(DefaultValues.FONT_SECOND_TITLE);
		VBox res = new VBox(titleLabel);
		if(list.size() > 0) {
			for (Solution.Citation item : list) {
				res.getChildren().add(new Label(item.getDOI() + " | " + item.getText()));
			}
		} else {
			res.getChildren().add(new Label(placeholder));
		}
		return res;
	}

	private Node createActions(Solution solution) {
		Text catalogInfo = new Text(UITextValues.SOLUTION_VIEW_CATALOG_LABEL);
		Text catalogBtn = new Text(solution.getCatalogName());
		Text actionInfo = new Text(UITextValues.SOLUTION_VIEW_ACTION_LABEL);
		Button installBtn = new Button(UITextValues.SOLUTION_VIEW_INSTALL_BTN);
		installBtn.textProperty().bind(Bindings.createStringBinding(
				() -> solution.isBlocked() ? solution.getBlockedMessage() : solution.isInstalled() ? UITextValues.SOLUTION_LIST_UNINSTALL_BTN : UITextValues.SOLUTION_LIST_INSTALL_BTN,
				solution.installedProperty(), solution.blockedProperty()));
		installBtn.onActionProperty().bind(Bindings.createObjectBinding(
				() -> solution.isInstalled() ?
						e ->eventService.publish(new SolutionLaunchRequestEvent(solution.getInstallation(), solution, "install")) :
						e ->eventService.publish(new SolutionLaunchRequestEvent(solution.getInstallation(), solution, "uninstall")),
				solution.installedProperty()
		));
		installBtn.disableProperty().bind(Bindings.createBooleanBinding(
				solution::isBlocked, solution.blockedProperty()
		));
		Button runBtn = new Button(UITextValues.SOLUTION_VIEW_RUN_BTN);
		runBtn.setOnAction(e -> eventService.publish(new SolutionLaunchRequestEvent(solution.getInstallation(), solution, "run")));
		runBtn.disableProperty().bind(Bindings.createBooleanBinding(
				() -> solution.isBlocked() || !solution.isInstalled(),
				solution.blockedProperty(), solution.installedProperty()
		));
		runBtn.setMaxWidth(Double.MAX_VALUE);
		installBtn.setMaxWidth(Double.MAX_VALUE);
		VBox actions = new VBox(catalogInfo, catalogBtn, actionInfo, installBtn, runBtn);
		ScrollPane scrollPane = new ScrollPane(actions);
		actions.setSpacing(UI_SPACING);
		actions.setPadding(new Insets(UI_SPACING));
		VBox.setVgrow(scrollPane, Priority.ALWAYS);
		scrollPane.setBorder(null);
		scrollPane.setBackground(null);
		scrollPane.setMinWidth(200);
		scrollPane.setFitToWidth(true);
		scrollPane.getStyleClass().add("transparent");
		return scrollPane;
	}

	private Label getText(String content, String label, String placeholder) {
		Label res = new Label((!label.isEmpty() ? label + ": " : "") + (content != null && !content.isEmpty() ? content : placeholder));
		res.setWrapText(true);
		return res;
	}

	private void refresh() {

	}

	@EventHandler
	private void solutionTaskStarted(SolutionLaunchRequestEvent e) {
		if(e.getAction().equals("install") || e.getAction().equals("uninstall")) {
			e.getSolution().block("uninstalling...");
			refresh();
		}
	}

	@EventHandler
	private void solutionTaskFinished(SolutionLaunchFinishedEvent e) {
		if(e.getAction().equals("install") || e.getAction().equals("uninstall")) {
			refresh();
		}
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
}
