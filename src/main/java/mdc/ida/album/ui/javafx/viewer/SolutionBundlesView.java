package mdc.ida.album.ui.javafx.viewer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Callback;
import mdc.ida.album.DefaultValues;
import mdc.ida.album.UITextValues;
import mdc.ida.album.model.AlbumInstallation;
import mdc.ida.album.model.Solution;
import mdc.ida.album.model.SolutionBundle;
import mdc.ida.album.model.event.SolutionLaunchFinishedEvent;
import mdc.ida.album.model.event.SolutionLaunchRequestEvent;
import org.scijava.Context;
import org.scijava.display.DisplayService;
import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;

import java.util.HashMap;
import java.util.Map;

import static mdc.ida.album.ui.javafx.viewer.JavaFXUtils.addButtonColumn;

public class SolutionBundlesView extends TableView<SolutionBundle> {

	@Parameter
	private UIService uiService;

	@Parameter
	private EventService eventService;

	@Parameter
	private DisplayService displayService;

	public SolutionBundlesView(Context context, AlbumInstallation installation) {
		context.inject(this);
		getStyleClass().add("noheader");
		setPlaceholder(new Label(UITextValues.COLLECTION_SOLUTIONS_LIST_PLACEHOLDER));
		TableColumn<SolutionBundle, Void> titleCol = addButtonColumn(
				this, 0,
				bundle -> bundle.getFirstChoice().getTitle(), bundle -> {
					Solution solution = bundle.getFirstChoice();
					uiService.show(solution.getName(), solution);
		}, Pos.CENTER_LEFT);
		TableColumn<SolutionBundle, Void> installCol = addButtonColumn(this, 1,
				bundle -> {
					Solution s = bundle.getFirstChoice();
					return s.isBlocked() ? s.getBlockedMessage() : s.isInstalled() ? UITextValues.SOLUTION_LIST_UNINSTALL_BTN : UITextValues.SOLUTION_LIST_INSTALL_BTN;
				},
				bundle -> {
					Solution firstChoice = bundle.getFirstChoice();
					eventService.publish(
							new SolutionLaunchRequestEvent(installation, firstChoice, firstChoice.isInstalled()? "uninstall" : "install")
					);
				},
				bundle -> !bundle.getFirstChoice().isBlocked()
		);
		TableColumn<SolutionBundle, Void> runCol = addButtonColumn(this, 2,
				bundle -> UITextValues.SOLUTION_LIST_RUN_BTN,
				bundle -> eventService.publish(new SolutionLaunchRequestEvent(installation, bundle.getFirstChoice(), "run")),
				bundle -> {
					Solution firstChoice = bundle.getFirstChoice();
					return firstChoice.isInstalled() && !firstChoice.isBlocked();
				});
		installCol.setPrefWidth(100);
		runCol.setPrefWidth(70);
		TableColumn<SolutionBundle, String> versionCol = createVersionColumn();
		double width = installCol.widthProperty().get() + runCol.widthProperty().get() + versionCol.widthProperty().get() + 20;
		titleCol.prefWidthProperty().bind(widthProperty().subtract(width));
		getColumns().add(versionCol);
		setBorder(Border.EMPTY);
		setBackground(Background.EMPTY);
	}

	private TableColumn<SolutionBundle, String> createVersionColumn() {
		TableColumn<SolutionBundle, String> versionCol = new TableColumn<>("");
		Callback<TableColumn<SolutionBundle, String>, TableCell<SolutionBundle, String>> cellFactory = new Callback<>() {
			@Override
			public TableCell<SolutionBundle, String> call(final TableColumn<SolutionBundle, String> param) {
				return new TableCell<>() {
					private final ComboBox<String> comboBox = new ComboBox<>();
					private final Label updateLabel = new Label(UITextValues.SOLUTION_LIST_NEWER_VERSION_LABEL);
					private final HBox updatableVersion = new HBox(comboBox, updateLabel);
					private final Map<Integer, ObservableList<String>> versions = new HashMap<>();

					{
						comboBox.setOnAction((ActionEvent event) -> {
							SolutionBundle data = getTableView().getItems().get(getIndex());
							new Thread(() -> {
								data.setSelected(comboBox.getSelectionModel().getSelectedItem());
								refresh();
							}).start();
						});
						updateLabel.getStyleClass().add("state-icon-table");
						updateLabel.getStyleClass().add("state-info");
						updateLabel.setTooltip(new Tooltip(UITextValues.SOLUTION_LIST_NEWER_VERSION_TOOLTIP));
						comboBox.setMaxWidth(Double.MAX_VALUE);
						HBox.setHgrow(comboBox, Priority.ALWAYS);
						updatableVersion.setFillHeight(true);
						updatableVersion.setAlignment(Pos.CENTER_RIGHT);
					}

					@Override
					public void updateItem(String item, boolean empty) {
						super.updateItem(item, empty);
						if (empty) {
							setGraphic(null);
						} else {
							if(getTableRow() != null) {
								SolutionBundle rowItem = getTableRow().getItem();
								if(rowItem != null) {
									ObservableList<String> currentVersions;
									if(versions.containsKey(getIndex())) {
										currentVersions = versions.get(getIndex());
									} else {
										currentVersions = FXCollections.observableArrayList(rowItem.getSolutions().keySet());
										versions.put(getIndex(), currentVersions);
									}
									comboBox.setItems(currentVersions);
									comboBox.getSelectionModel().select(rowItem.getFirstChoice().getVersion());
									if(rowItem.firstChoiceIsMostRecent()) {
										setGraphic(comboBox);
									} else {
										setGraphic(updatableVersion);
									}
								}
							} else {
								setGraphic(null);
							}
						}
					}
				};
			}
		};
		versionCol.setCellFactory(cellFactory);
		versionCol.setPrefWidth(200);
		return versionCol;
	}

	@EventHandler
	private void solutionTaskStarted(SolutionLaunchRequestEvent e) {
		if(e.getAction().equals("install") || e.getAction().equals("uninstall")) {
			refresh();
		}
	}

	@EventHandler
	private void solutionTaskFinished(SolutionLaunchFinishedEvent e) {
		if(e.getAction().equals("install") || e.getAction().equals("uninstall")) {
			refresh();
		}
	}
}
