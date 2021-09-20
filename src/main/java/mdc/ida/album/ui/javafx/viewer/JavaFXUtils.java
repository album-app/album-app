package mdc.ida.album.ui.javafx.viewer;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Callback;
import mdc.ida.album.DefaultValues;

import java.util.function.Consumer;
import java.util.function.Function;

import static mdc.ida.album.DefaultValues.UI_SPACING;

public class JavaFXUtils {

	static VBox makePretty(VBox res) {
		res.setSpacing(UI_SPACING);
		res.setPadding(new Insets(UI_SPACING));
		res.setBackground(new Background(new BackgroundFill(new Color(1.0, 1.0, 1.0, 0.5), new CornerRadii(10), Insets.EMPTY)));
		VBox.setVgrow(res, Priority.ALWAYS);
		return res;
	}

	static HBox makePretty(HBox res) {
		res.setSpacing(UI_SPACING);
		res.setPadding(new Insets(UI_SPACING));
		res.setBackground(new Background(new BackgroundFill(new Color(1.0, 1.0, 1.0, 0.5), new CornerRadii(10), Insets.EMPTY)));
		VBox.setVgrow(res, Priority.ALWAYS);
		return res;
	}

	static ScrollPane scrollable(Node box) {
		ScrollPane scrollPane = new ScrollPane(box);
		scrollPane.getStyleClass().add("transparent");
		scrollPane.getStyleClass().add("edge-to-edge");
		scrollPane.setFitToHeight(true);
		scrollPane.setFitToWidth(true);
		return scrollPane;
	}

	static void debug(Pane pane) {
		pane.setBackground(new Background(new BackgroundFill(new Color(1.0, 0.0, 0.0, 1.), CornerRadii.EMPTY, Insets.EMPTY)));
	}

	static void debug(Control control) {
		control.setBackground(new Background(new BackgroundFill(new Color(1.0, 0.0, 0.0, 1.), CornerRadii.EMPTY, Insets.EMPTY)));
	}

	static Pane createStatefulExpandable(StringBinding titleBinding, BooleanProperty stateHappyProperty, Node expandable) {
		TitledPane titledPane = new TitledPane(titleBinding.get(), expandable);
		titledPane.setBackground(new Background(new BackgroundFill(new Color(1.0, 1.0, 1.0, 0.5), new CornerRadii(10), Insets.EMPTY)));
		Label statusIcon = new Label();
		statusIcon.getStyleClass().add("state-icon");
		statusIcon.textProperty().bind(Bindings.createStringBinding(() -> stateHappyProperty.get()? "✓" : "❌", stateHappyProperty));
		HBox res = new HBox(statusIcon, titledPane);
		stateHappyProperty.addListener((observable, oldValue, newValue) -> {
			if(newValue) {
				statusIcon.getStyleClass().add("state-happy");
				statusIcon.getStyleClass().remove("state-unhappy");
			} else {
				statusIcon.getStyleClass().remove("state-happy");
				statusIcon.getStyleClass().add("state-unhappy");
			}
			TitledPane pane = new TitledPane(titleBinding.get(), expandable);
			pane.setBackground(new Background(new BackgroundFill(new Color(1.0, 1.0, 1.0, 0.5), new CornerRadii(10), Insets.EMPTY)));
			pane.setExpanded(!stateHappyProperty.get());
			HBox.setHgrow(pane, Priority.ALWAYS);
			res.getChildren().remove(1);
			res.getChildren().add(pane);
		});
		HBox.setHgrow(titledPane, Priority.ALWAYS);
		HBox.setHgrow(res, Priority.ALWAYS);
		return res;
	}

	static TitledPane createExpandable(String title, Node expandable) {
		TitledPane res = new TitledPane(title, expandable);
		res.setExpanded(false);
		return res;
	}

	static StringBinding createStatusBinding(BooleanProperty isOK, String titleOK, String titleNotOK) {
		return Bindings.createStringBinding(() -> isOK.get() ? titleOK : titleNotOK, isOK);
	}

	static StringBinding createStatusBindingWithIcon(BooleanProperty isOK, String titleOK, String titleNotOK) {
		return Bindings.createStringBinding(() -> isOK.get() ? "✓ " + titleOK : "❌ " + titleNotOK, isOK);
	}

	static Tab createLocalInstallationTab(Node content, String tabName) {
		VBox contentBox = new VBox(content);
		contentBox.getStyleClass().add("installation-contentpane");
		Tab tab = new Tab(tabName, contentBox);
		Label label = new Label(tab.getText());
		label.getStyleClass().add("tab-label");
		label.setRotate(90);
		StackPane stp = new StackPane(new Group(label));
		stp.setRotate(90);
		tab.setGraphic(stp);
		tab.setClosable(false);
		tab.setText("");
		return tab;
	}

	static <T> TableColumn<T, String> makeColumn(String text, String property) {
		TableColumn<T, String> column = new TableColumn<>(text);
		column.setCellValueFactory(new PropertyValueFactory<>(property));
		return column;
	}

	static <T> TableColumn<T, Void> addButtonColumn(TableView<T> table, int index, Function<T, String> text, Consumer<T> action) {
		return addButtonColumn(table, index, text, action, s -> true, Pos.CENTER);
	}

	static <T> TableColumn<T, Void> addButtonColumn(TableView<T> table, int index, Function<T, String> text, Consumer<T> action, Function<T, Boolean> condition) {
		return addButtonColumn(table, index, text, action, condition, Pos.CENTER);
	}

	static <T> TableColumn<T, Void> addButtonColumn(TableView<T> table, int index, Function<T, String> text, Consumer<T> action, Pos position) {
		return addButtonColumn(table, index, text, action, s -> true, position);
	}

	static <T> TableColumn<T, Void> addButtonColumn(TableView<T> table, int index, Function<T, String> text, Consumer<T> action, Function<T, Boolean> condition, Pos position) {
		TableColumn<T, Void> colBtn = new TableColumn<>("");

		Callback<TableColumn<T, Void>, TableCell<T, Void>> cellFactory = new Callback<>() {
			@Override
			public TableCell<T, Void> call(final TableColumn<T, Void> param) {
				return new TableCell<>() {

					private final Button btn = new Button();

					{
						btn.setOnAction((ActionEvent event) -> {
							T data = getTableView().getItems().get(getIndex());
							new Thread(() -> action.accept(data)).start();
						});
					}

					@Override
					public void updateItem(Void item, boolean empty) {
						super.updateItem(item, empty);
						if (empty) {
							setGraphic(null);
						} else {
							if(getTableRow() != null) {
								T rowItem = getTableRow().getItem();
								if(rowItem != null) {
									btn.setText(text.apply(rowItem));
									btn.setAlignment(position);
									if(condition != null) btn.setDisable(!condition.apply(rowItem));
								}
								btn.setMaxWidth(Double.MAX_VALUE);
							}
							setGraphic(btn);
						}
					}
				};
			}
		};

		colBtn.setCellFactory(cellFactory);
		table.getColumns().add(index, colBtn);
		return colBtn;

	}

	static Node asTextFlow(String text) {
		TextFlow textFlow = new TextFlow(new Text(text));
		textFlow.setPadding(new Insets(UI_SPACING));
		return textFlow;
	}

	static VBox asConsoleTextFlow(String text) {
		Text text1 = new Text(text);
		text1.setFill(Color.WHITE);
		text1.setFont(DefaultValues.FONT_CONSOLE);
		TextFlow textFlow = new TextFlow(text1);
		textFlow.setPadding(new Insets(UI_SPACING));
		textFlow.setBackground(new Background(new BackgroundFill(Color.BLACK, new CornerRadii(5.), Insets.EMPTY)));
		Button copyBtn = new Button("Copy code");
		copyBtn.setOnAction(event -> {
			final Clipboard clipboard = Clipboard.getSystemClipboard();
			final ClipboardContent content = new ClipboardContent();
			content.putString(text);
			clipboard.setContent(content);
		});
		copyBtn.setTranslateY(-10);
		HBox copyBtnBox = new HBox(copyBtn);
		copyBtnBox.setAlignment(Pos.BASELINE_RIGHT);
		return new VBox(textFlow, copyBtnBox);
	}

}
