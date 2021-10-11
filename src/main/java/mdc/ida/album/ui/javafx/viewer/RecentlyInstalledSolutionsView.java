package mdc.ida.album.ui.javafx.viewer;

import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import mdc.ida.album.model.AlbumInstallation;
import mdc.ida.album.model.event.RecentlyInstalledUpdatedEvent;
import mdc.ida.album.model.Solution;
import org.scijava.Context;
import org.scijava.event.EventHandler;

public class RecentlyInstalledSolutionsView extends VBox {

	private final TableView<Solution> solutionList;

	public RecentlyInstalledSolutionsView(Context context, AlbumInstallation installation) {
		context.inject(this);
		solutionList = new SolutionsCompactView(context, installation);
		getChildren().add(solutionList);
	}

	@EventHandler
	private void recentSolutionsListUpdated(RecentlyInstalledUpdatedEvent e) {
		solutionList.getItems().clear();
		ObservableList<Solution> solutions = solutionList.getItems();
		for (Solution solution : e.getSolutions()) {
			if(solution.isInstalled()) solutions.add(solution);
		}
	}
}
