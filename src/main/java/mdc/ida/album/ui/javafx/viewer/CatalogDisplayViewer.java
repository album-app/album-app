package mdc.ida.album.ui.javafx.viewer;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import mdc.ida.album.UITextValues;
import mdc.ida.album.model.Catalog;
import mdc.ida.album.model.Solution;
import mdc.ida.album.model.SolutionLaunchRequestEvent;
import mdc.ida.album.scijava.ui.javafx.viewer.EasyJavaFXDisplayViewer;
import org.scijava.Context;
import org.scijava.command.CommandService;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;
import org.scijava.ui.viewer.DisplayViewer;

import static mdc.ida.album.ui.javafx.viewer.JavaFXUtils.addButtonColumn;

/**
 * This class displays a {@link Catalog}.
 */
@Plugin(type = DisplayViewer.class)
public class CatalogDisplayViewer extends EasyJavaFXDisplayViewer<Catalog> {

	@Parameter
	private Context context;
	@Parameter
	private CommandService commandService;
	@Parameter
	private LogService logService;
	@Parameter
	private UIService uiService;
	@Parameter
	private EventService eventService;

	private Catalog collection;

	public CatalogDisplayViewer() {
		super(Catalog.class);
	}

	@Override
	protected boolean canView(Catalog catalog) {
		return true;
	}

	@Override
	protected VBox createDisplayPanel(Catalog catalog) {
		this.collection = catalog;
		TableView<Solution> tableView = new TableView<>();
		tableView.getColumns().add(makeColumn(UITextValues.SOLUTION_LIST_HEADER_TITLE, "title"));
		tableView.getColumns().add(makeColumn(UITextValues.SOLUTION_LIST_HEADER_VERSION, "version"));
		tableView.getColumns().add(makeColumn(UITextValues.SOLUTION_LIST_HEADER_DESCRIPTION, "description"));
		addButtonColumn(tableView, 3, s-> UITextValues.SOLUTION_LIST_HEADER_ABOUT, solution -> uiService.show(solution.getName(), solution));
		addButtonColumn(tableView, 4, s-> UITextValues.SOLUTION_LIST_HEADER_INSTALL,
				solution -> eventService.publish(new SolutionLaunchRequestEvent(catalog.getParent(), solution, "install")));
		addButtonColumn(tableView, 5, s-> UITextValues.SOLUTION_LIST_HEADER_RUN, solution -> eventService.publish(new SolutionLaunchRequestEvent(catalog.getParent(), solution, "run")));
		catalog.forEach(solution -> tableView.getItems().add(solution));
		tableView.setBorder(Border.EMPTY);
		tableView.setBackground(Background.EMPTY);
		VBox vBox = new VBox(createFilter(), tableView);
		vBox.setPadding(new Insets(5));
		return vBox;
	}

	private TableColumn<Solution, String> makeColumn(String text, String property) {
		TableColumn<Solution, String> column = new TableColumn<>(text);
		column.setCellValueFactory(new PropertyValueFactory<>(property));
		return column;
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
