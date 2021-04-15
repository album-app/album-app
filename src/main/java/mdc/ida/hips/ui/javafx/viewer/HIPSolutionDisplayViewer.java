package mdc.ida.hips.ui.javafx.viewer;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Callback;
import mdc.ida.hips.model.HIPSolution;
import org.scijava.Context;
import org.scijava.command.CommandService;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;
import org.scijava.ui.javafx.viewer.EasyJavaFXDisplayViewer;
import org.scijava.ui.viewer.DisplayViewer;

import java.util.function.Consumer;

/**
 * This class displays a {@link HIPSolution}.
 */
@Plugin(type = DisplayViewer.class)
public class HIPSolutionDisplayViewer extends EasyJavaFXDisplayViewer<HIPSolution> {

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

	private HIPSolution solution;

	public HIPSolutionDisplayViewer() {
		super(HIPSolution.class);
	}

	@Override
	protected boolean canView(HIPSolution solution) {
		return true;
	}

	@Override
	protected VBox createDisplayPanel(HIPSolution solution) {
		this.solution = solution;
		Text title = new Text(solution.getTitle() != null? solution.getTitle() : solution.getName());
		Text idText = new Text(solution.getGroup() + " | " + solution.getName() + " | " + solution.getVersion());
		idText.setStyle("-fx-font-family: 'monospaced';");
		Text description = new Text(solution.getDescription() != null? solution.getDescription() : "No description");
		VBox content = new VBox(title, idText, description);
		content.setPadding(new Insets(5));
		return content;
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
		Node btnAtom = createImageButton("/scale-atom.png");
		Node btnMolecule = createImageButton("/scale-molecule.png");
		Node btnCell = createImageButton("/scale-cell.png");
		Node btnOrganism = createImageButton("/scale-organism.png");
		Node btnTerrestrial = createImageButton("/scale-terrestrial.png");
		Node btnUniverse = createImageButton("/scale-universe.png");
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

	private void addButton(TableView<HIPSolution> table, int index, String text, Consumer<HIPSolution> action) {
		TableColumn<HIPSolution, Void> colBtn = new TableColumn<>("");

		Callback<TableColumn<HIPSolution, Void>, TableCell<HIPSolution, Void>> cellFactory = new Callback<>() {
			@Override
			public TableCell<HIPSolution, Void> call(final TableColumn<HIPSolution, Void> param) {
				return new TableCell<>() {

					private final Button btn = new Button(text);

					{
						btn.setOnAction((ActionEvent event) -> {
							HIPSolution data = getTableView().getItems().get(getIndex());
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
