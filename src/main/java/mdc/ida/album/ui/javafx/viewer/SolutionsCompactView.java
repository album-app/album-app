package mdc.ida.album.ui.javafx.viewer;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import mdc.ida.album.DefaultValues;
import mdc.ida.album.UITextValues;
import mdc.ida.album.model.AlbumInstallation;
import mdc.ida.album.model.Solution;
import mdc.ida.album.control.AlbumServerService;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;

import java.io.IOException;

import static mdc.ida.album.ui.javafx.viewer.JavaFXUtils.addButtonColumn;

public class SolutionsCompactView extends TableView<Solution> {

	@Parameter
	private UIService uiService;

	@Parameter
	private AlbumServerService albumService;

	public SolutionsCompactView(Context context, AlbumInstallation installation) {
		context.inject(this);
		getStyleClass().add("noheader");
		TableColumn<Solution, Void> infoCol = addButtonColumn(this, 0,
				this::solutionIdentifier, solution -> uiService.show(solution.getName(), solution), Pos.CENTER_LEFT);
		TableColumn<Solution, Void> runCol = addButtonColumn(this, 1, s -> UITextValues.SOLUTION_LIST_RUN_BTN, s -> {
			try {
				albumService.launchSolution(
						installation, s, DefaultValues.DEFAULT_RECENT_SOLUTIONS_ACTION);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		double width = runCol.widthProperty().get() + 20;
		infoCol.prefWidthProperty().bind(widthProperty().subtract(width));

		setPlaceholder(new Label(UITextValues.RECENT_SOLUTIONS_LIST_PLACEHOLDER));
		setBorder(Border.EMPTY);
		setBackground(Background.EMPTY);
	}

	private String solutionIdentifier(Solution solution) {
		return solution.getGroup() + ": " + solution.getName() + " (" + solution.getVersion() + ")";
	}
}
