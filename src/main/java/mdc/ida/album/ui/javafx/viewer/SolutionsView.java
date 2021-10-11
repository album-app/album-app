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
import mdc.ida.album.model.event.SolutionLaunchFinishedEvent;
import mdc.ida.album.model.event.SolutionLaunchRequestEvent;
import org.scijava.Context;
import org.scijava.display.DisplayService;
import org.scijava.event.EventHandler;
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

	@Parameter
	private DisplayService displayService;

	public SolutionsView(Context context, AlbumInstallation installation) {
		context.inject(this);
		getStyleClass().add("noheader");
		setPlaceholder(new Label(UITextValues.COLLECTION_SOLUTIONS_LIST_PLACEHOLDER));
		TableColumn<Solution, Void> titleCol = addButtonColumn(this, 0, Solution::getTitle, solution -> {
			uiService.show(solution.getName(), solution);
		}, Pos.CENTER_LEFT);
		TableColumn<Solution, Void> installCol = addButtonColumn(this, 1,
				s -> s.isBlocked() ? s.getBlockedMessage() : s.isInstalled()? UITextValues.SOLUTION_LIST_UNINSTALL_BTN : UITextValues.SOLUTION_LIST_INSTALL_BTN,
				solution -> eventService.publish(
						new SolutionLaunchRequestEvent(installation, solution, solution.isInstalled()? "uninstall" : "install")
				),
				s -> !s.isBlocked()
		);
		TableColumn<Solution, Void> runCol = addButtonColumn(this, 2,
				s -> UITextValues.SOLUTION_LIST_RUN_BTN,
				solution -> eventService.publish(new SolutionLaunchRequestEvent(installation, solution, "run")),
				s -> s.isInstalled() && !s.isBlocked());
		installCol.setPrefWidth(100);
		runCol.setPrefWidth(70);
		TableColumn<Solution, String> versionCol = makeColumn("", "version");
		double width = installCol.widthProperty().get() + runCol.widthProperty().get() + versionCol.widthProperty().get() + 20;
		titleCol.prefWidthProperty().bind(widthProperty().subtract(width));
		getColumns().add(versionCol);
		setBorder(Border.EMPTY);
		setBackground(Background.EMPTY);
	}

	@EventHandler
	private void solutionTaskStarted(SolutionLaunchRequestEvent e) {
		if(e.getAction().equals("install")) {
			e.getSolution().block("installing...");
			refresh();
		}
		if(e.getAction().equals("uninstall")) {
			e.getSolution().block("uninstalling...");
			refresh();
		}
	}

	@EventHandler
	private void solutionTaskFinished(SolutionLaunchFinishedEvent e) {
		if(e.getAction().equals("install")) {
			e.getSolution().setInstalled(true);
			e.getSolution().unblock();
			refresh();
		}
		if(e.getAction().equals("uninstall")) {
			e.getSolution().setInstalled(false);
			e.getSolution().unblock();
			refresh();
		}
	}
}
