package mdc.ida.album.ui.javafx.viewer;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.VBox;
import mdc.ida.album.UITextValues;
import mdc.ida.album.model.AlbumInstallation;
import mdc.ida.album.model.Catalog;
import mdc.ida.album.model.event.CatalogListEvent;
import mdc.ida.album.model.SolutionCollection;
import mdc.ida.album.control.AlbumServerService;
import org.scijava.Context;
import org.scijava.event.EventHandler;
import org.scijava.plugin.Parameter;

import java.io.IOException;
import java.util.Optional;

import static mdc.ida.album.ui.javafx.viewer.JavaFXUtils.addButtonColumn;
import static mdc.ida.album.ui.javafx.viewer.JavaFXUtils.makeColumn;

public class CatalogListView extends VBox {

	@Parameter
	private AlbumServerService albumService;

	private final TableView<Catalog> catalogList;

	public CatalogListView(Context context, SolutionCollection collection) {
		this(context, collection.getInstallation());
		collection.forEach(catalogList.getItems()::add);
	}

	public CatalogListView(Context context, AlbumInstallation installation) {
		context.inject(this);
		catalogList = new TableView<>();
		catalogList.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		catalogList.getStyleClass().add("noheader");
		catalogList.setPlaceholder(new Label(UITextValues.CATALOG_LIST_PLACEHOLDER));
		catalogList.setBorder(Border.EMPTY);
		catalogList.setBackground(Background.EMPTY);
		TableColumn<Catalog, Void> removeCatalogCol = addButtonColumn(catalogList, 0, catalog -> UITextValues.CATALOG_LIST_REMOVE_CATALOG_BTN,
				catalog -> {
					try {
						albumService.removeCatalog(installation, catalog);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}, catalog -> true);
		removeCatalogCol.setMaxWidth(555);
		catalogList.getColumns().add(makeColumn("Name", "name"));
		Button addCatalogBtn = new Button(UITextValues.CATALOG_LIST_ADD_CATALOG_BTN);
		addCatalogBtn.setMaxWidth(Double.MAX_VALUE);
		addCatalogBtn.setOnAction(event -> initAddCatalogAction(installation));
		getChildren().addAll(catalogList, addCatalogBtn);
	}

	TableView<Catalog> getCatalogView() {
		return catalogList;
	}

	private void initAddCatalogAction(AlbumInstallation installation) {
		TextInputDialog dialog = new TextInputDialog(UITextValues.CATALOG_LIST_ADD_CATALOG_DIALOG_DEFAULT_VALUE);
		dialog.setTitle(UITextValues.CATALOG_LIST_ADD_CATALOG_DIALOG_TITLE);
		dialog.setHeaderText(UITextValues.CATALOG_LIST_ADD_CATALOG_DIALOG_TEXT);
		dialog.setContentText(UITextValues.CATALOG_LIST_ADD_CATALOG_DIALOG_INPUT_LABEL);
		Optional<String> result = dialog.showAndWait();
		result.ifPresent(src -> {
			try {
				albumService.addCatalog(installation, src);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	@EventHandler
	private void catalogListUpdated(CatalogListEvent e) {
		catalogList.getItems().clear();
		e.getCollection().forEach(catalogList.getItems()::add);
	}
}
