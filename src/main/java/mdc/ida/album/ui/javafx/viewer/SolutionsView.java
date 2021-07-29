package mdc.ida.album.ui.javafx.viewer;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import mdc.ida.album.UITextValues;
import mdc.ida.album.model.AlbumInstallation;
import mdc.ida.album.model.Solution;
import mdc.ida.album.model.SolutionLaunchRequestEvent;
import org.scijava.Context;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;

import static mdc.ida.album.ui.javafx.viewer.JavaFXUtils.addButtonColumn;
import static mdc.ida.album.ui.javafx.viewer.JavaFXUtils.makeColumn;

public class SolutionsView extends TableView<Solution> {

	@Parameter
	private UIService uiService;

	@Parameter
	private EventService eventService;

	public SolutionsView(Context context, AlbumInstallation installation) {
		context.inject(this);
		getStyleClass().add("noheader");
		setPlaceholder(new Label(UITextValues.COLLECTION_SOLUTIONS_LIST_PLACEHOLDER));
		TableColumn<Solution, Void> titleCol = addButtonColumn(this, 0, Solution::getTitle, solution -> uiService.show(solution.getName(), solution), Pos.CENTER_LEFT);
		TableColumn<Solution, Void> installCol = addButtonColumn(this, 1, s -> UITextValues.SOLUTION_LIST_INSTALL_BTN,
				solution -> eventService.publish(new SolutionLaunchRequestEvent(installation, solution, "install")));
		TableColumn<Solution, Void> runCol = addButtonColumn(this, 2, s -> UITextValues.SOLUTION_LIST_RUN_BTN, solution -> eventService.publish(new SolutionLaunchRequestEvent(installation, solution, "run")));
		TableColumn<Solution, String> versionCol = makeColumn("", "version");
		double width = installCol.widthProperty().get() + runCol.widthProperty().get() + versionCol.widthProperty().get() + 20;
		titleCol.prefWidthProperty().bind(widthProperty().subtract(width));
		getColumns().add(versionCol);
		setBorder(Border.EMPTY);
		setBackground(Background.EMPTY);
	}
}
