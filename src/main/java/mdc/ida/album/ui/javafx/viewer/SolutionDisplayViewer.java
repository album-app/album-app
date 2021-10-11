package mdc.ida.album.ui.javafx.viewer;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import mdc.ida.album.DefaultValues;
import mdc.ida.album.UITextValues;
import mdc.ida.album.model.Solution;
import mdc.ida.album.model.event.SolutionLaunchRequestEvent;
import mdc.ida.album.scijava.ui.javafx.viewer.EasyJavaFXDisplayViewer;
import org.scijava.Context;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.viewer.DisplayViewer;

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

	public SolutionDisplayViewer() {
		super(Solution.class);
	}

	@Override
	protected boolean canView(Solution solution) {
		return true;
	}

	@Override
	protected Node createDisplayPanel(Solution solution) {
		Text title = new Text(solution.getTitle() != null? solution.getTitle() : solution.getName());
		title.setFont(DefaultValues.FONT_TITLE);
		Text idText = new Text(solution.getGroup() + UITextValues.SOLUTION_VIEW_ID_SEPARATOR
				+ solution.getName() + UITextValues.SOLUTION_VIEW_ID_SEPARATOR + UITextValues.SOLUTION_VIEW_ID_PRE_VERSION + solution.getVersion());
		idText.setStyle("-fx-font-family: 'monospaced';");
		Text description = getText(solution.getDescription(), UITextValues.SOLUTION_VIEW_DESCRIPTION_LABEL, UITextValues.SOLUTION_VIEW_DESCRIPTION_PLACEHOLDER);
		Node citation = getText(solution.getCite(), UITextValues.SOLUTION_VIEW_CITE_LABEL, UITextValues.SOLUTION_VIEW_CITE_PLACEHOLDER);
		Node authors = getText(solution.getAuthors(), UITextValues.SOLUTION_VIEW_AUTHORS_LABEL, UITextValues.SOLUTION_VIEW_AUTHORS_PLACEHOLDER);
		Node documentation = getText(solution.getDocumentation(), UITextValues.SOLUTION_VIEW_DOCUMENTATION_LABEL, UITextValues.SOLUTION_VIEW_DOCUMENTATION_PLACEHOLDER);
		Node license = getText(solution.getLicense(), UITextValues.SOLUTION_VIEW_LICENSE_LABEL, UITextValues.SOLUTION_VIEW_LICENSE_PLACEHOLDER);
		Text catalogInfo = new Text(UITextValues.SOLUTION_VIEW_CATALOG_LABEL + solution.getCatalogName());
		VBox content = new VBox(title, idText, description, citation, authors, license, documentation);
		content.setSpacing(UI_SPACING);
		content.setPadding(new Insets(UI_SPACING));
		VBox.setVgrow(content, Priority.ALWAYS);
		HBox.setHgrow(content, Priority.ALWAYS);
		VBox actions = createActions(solution, catalogInfo);
		HBox res = new HBox(content, actions);
		res.setSpacing(UI_SPACING);
		res.setPadding(new Insets(UI_SPACING));
		return res;
	}

	private VBox createActions(Solution solution, Text catalogInfo) {
		Button installBtn = new Button(UITextValues.SOLUTION_VIEW_INSTALL_BTN);
		installBtn.setOnAction(e -> eventService.publish(new SolutionLaunchRequestEvent(solution.getInstallation(), solution, "install")));
		Button runBtn = new Button(UITextValues.SOLUTION_VIEW_RUN_BTN);
		runBtn.setOnAction(e -> eventService.publish(new SolutionLaunchRequestEvent(solution.getInstallation(), solution, "run")));
		runBtn.setMaxWidth(Double.MAX_VALUE);
		installBtn.setMaxWidth(Double.MAX_VALUE);
		VBox actions = new VBox(catalogInfo, installBtn, runBtn);
		actions.getStyleClass().add("recently-launched");
		actions.setSpacing(UI_SPACING);
		actions.setPadding(new Insets(UI_SPACING));
		VBox.setVgrow(actions, Priority.ALWAYS);
		return actions;
	}

	private Text getText(String content, String label, String placeholder) {
		return new Text((!label.isEmpty()? label + ": " : "") + (content != null && !content.isEmpty() ? content : placeholder));
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
