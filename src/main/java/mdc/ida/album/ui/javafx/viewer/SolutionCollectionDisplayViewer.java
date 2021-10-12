package mdc.ida.album.ui.javafx.viewer;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import mdc.ida.album.model.SolutionBundle;
import mdc.ida.album.model.SolutionCollection;
import mdc.ida.album.scijava.ui.javafx.viewer.EasyJavaFXDisplayViewer;
import org.scijava.Context;
import org.scijava.command.CommandService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.viewer.DisplayViewer;

/**
 * This class displays a {@link SolutionCollection}.
 */
@Plugin(type = DisplayViewer.class)
public class SolutionCollectionDisplayViewer extends EasyJavaFXDisplayViewer<SolutionCollection> {

	@Parameter
	private Context context;
	@Parameter
	private CommandService commandService;
	@Parameter
	private LogService logService;

	public SolutionCollectionDisplayViewer() {
		super(SolutionCollection.class);
	}

	@Override
	protected boolean canView(SolutionCollection collection) {
		return true;
	}

	@Override
	protected Node createDisplayPanel(SolutionCollection collection) {
		TableView<SolutionBundle> solutionsView = new SolutionBundlesView(context, collection.getInstallation());
//		VBox solutionsBox = new VBox(createFilter(), solutionsView);
		VBox solutionsBox = new VBox(solutionsView);
		CatalogListView catalogView = new CatalogListView(context, collection);
		catalogView.getCatalogView().getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null) {
				solutionsView.getItems().clear();
				newSelection.getSolutionBundles().forEach((id, bundle) -> {
					solutionsView.getItems().add(bundle);
				});
			}
		});
		SplitPane split = new SplitPane(catalogView, solutionsBox);
		split.setDividerPosition(0, 0.3);
		SplitPane.setResizableWithParent(catalogView, Boolean.FALSE);
		split.setPadding(new Insets(5));
		return split;
	}

	private Node createFilter() {
		final Pane spacer = new Pane();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		spacer.setMinSize(10, 1);
		HBox box = new HBox(createTypeButtons(), spacer);
		box.setPadding(new Insets(5));
		return box;
	}

	private HBox createTypeButtons() {
		ToggleGroup group = new ToggleGroup();
		ToggleButton filterAll = createTextButton("all");
		ToggleButton filterSolution = createTextButton("solution");
		ToggleButton filterApp = createTextButton("app");
		ToggleButton filterSample = createTextButton("sample");
		group.getToggles().add(filterAll);
		group.getToggles().add(filterSolution);
		group.getToggles().add(filterApp);
		group.getToggles().add(filterSample);
		filterAll.setSelected(true);
		HBox box = new HBox(filterAll, filterSolution, filterApp, filterSample);
		box.setPadding(new Insets(0, 10, 0, 0));
		return box;
	}

	private ToggleButton createTextButton(String name) {
		ToggleButton btn = new ToggleButton(name);
		btn.setPrefHeight(42);
		return btn;
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
