package mdc.ida.album.ui.javafx.viewer;

import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import mdc.ida.album.model.AlbumInstallation;
import mdc.ida.album.model.event.RecentlyLaunchedUpdatedEvent;
import mdc.ida.album.model.Solution;
import org.scijava.Context;
import org.scijava.event.EventHandler;

public class RecentlyLaunchedSolutionsView extends VBox {

	private final TableView<Solution> solutionList;

	public RecentlyLaunchedSolutionsView(Context context, AlbumInstallation installation) {
		context.inject(this);
		solutionList = new SolutionsCompactView(context, installation);
		getChildren().add(solutionList);
	}

	@EventHandler
	private void recentSolutionsListUpdated(RecentlyLaunchedUpdatedEvent e) {
		solutionList.getItems().clear();
		e.getSolutions().forEach(solutionList.getItems()::add);
	}
}
