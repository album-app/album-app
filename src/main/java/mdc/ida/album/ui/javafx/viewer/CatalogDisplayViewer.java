package mdc.ida.album.ui.javafx.viewer;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import mdc.ida.album.model.Catalog;
import mdc.ida.album.model.Solution;
import mdc.ida.album.model.SolutionLaunchRequestEvent;
import org.scijava.Context;
import org.scijava.command.CommandService;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;
import mdc.ida.album.scijava.ui.javafx.viewer.EasyJavaFXDisplayViewer;
import org.scijava.ui.viewer.DisplayViewer;

import java.util.function.Consumer;

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
	protected boolean canView(Catalog collection) {
		return true;
	}

	@Override
	protected VBox createDisplayPanel(Catalog collection) {
		this.collection = collection;
		TableView<Solution> tableView = new TableView<>();
		tableView.getColumns().add(makeColumn("Title", "title"));
		tableView.getColumns().add(makeColumn("Version", "version"));
		tableView.getColumns().add(makeColumn("Description", "description"));
		addButton(tableView, 0, "About", solution -> uiService.show(solution.getName(), solution));
		addButton(tableView, 0, "Install",
				solution -> eventService.publish(new SolutionLaunchRequestEvent(collection.getParent(), solution, "install")));
		addButton(tableView, 0, "Run", solution -> eventService.publish(new SolutionLaunchRequestEvent(collection.getParent(), solution, "run")));
		collection.forEach(solution -> tableView.getItems().add(solution));
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
		HBox box = new HBox(createTypeButtons(), spacer, createScaleButtons());
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

	private HBox createScaleButtons() {
		Node btnAtom = createImageButton("scale-atom.png");
		Node btnMolecule = createImageButton("scale-molecule.png");
		Node btnCell = createImageButton("scale-cell.png");
		Node btnOrganism = createImageButton("scale-organism.png");
		Node btnTerrestrial = createImageButton("scale-terrestrial.png");
		Node btnUniverse = createImageButton("scale-universe.png");
		return new HBox(btnAtom, btnMolecule, btnCell, btnOrganism, btnTerrestrial, btnUniverse);
	}

	private ToggleButton createTextButton(String name) {
		ToggleButton btn = new ToggleButton(name);
		btn.setPrefHeight(42);
		return btn;
	}

	private Node createImageButton(String iconPath) {
		Image img = new Image(getClass().getResourceAsStream(iconPath));
		ImageView view = new ImageView(img);
		view.setFitHeight(42);
		view.setPreserveRatio(true);
		ToggleButton button = new ToggleButton();
		button.setPrefSize(42, 42);
		button.setPadding(Insets.EMPTY);
		button.setGraphic(view);
		button.setSelected(true);
		return button;
	}

	private void addButton(TableView<Solution> table, int index, String text, Consumer<Solution> action) {
		TableColumn<Solution, Void> colBtn = new TableColumn<>("");

		Callback<TableColumn<Solution, Void>, TableCell<Solution, Void>> cellFactory = new Callback<TableColumn<Solution, Void>, TableCell<Solution, Void>>() {
			@Override
			public TableCell<Solution, Void> call(final TableColumn<Solution, Void> param) {
				return new TableCell<>() {

					private final Button btn = new Button(text);

					{
						btn.setOnAction((ActionEvent event) -> {
							Solution data = getTableView().getItems().get(getIndex());
							new Thread(() -> action.accept(data)).start();
						});
					}

					@Override
					public void updateItem(Void item, boolean empty) {
						super.updateItem(item, empty);
						if (empty) {
							setGraphic(null);
						} else {
							setGraphic(btn);
						}
					}
				};
			}
		};

		colBtn.setCellFactory(cellFactory);

		table.getColumns().add(index, colBtn);

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
