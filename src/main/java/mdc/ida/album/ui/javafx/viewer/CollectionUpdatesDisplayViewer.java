package mdc.ida.album.ui.javafx.viewer;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import mdc.ida.album.UITextValues;
import mdc.ida.album.model.AlbumInstallation;
import mdc.ida.album.model.Catalog;
import mdc.ida.album.model.CatalogUpdate;
import mdc.ida.album.model.CollectionUpdates;
import mdc.ida.album.scijava.ui.javafx.viewer.EasyJavaFXDisplayViewer;
import mdc.ida.album.service.AlbumServerService;
import org.scijava.Context;
import org.scijava.command.CommandService;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;
import org.scijava.ui.viewer.DisplayViewer;

import java.io.IOException;

/**
 * This class displays {@link CollectionUpdates}.
 */
@Plugin(type = DisplayViewer.class)
public class CollectionUpdatesDisplayViewer extends EasyJavaFXDisplayViewer<CollectionUpdates> {

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
	@Parameter
	private AlbumServerService albumService;

	private Catalog collection;

	public CollectionUpdatesDisplayViewer() {
		super(CollectionUpdates.class);
	}

	@Override
	protected boolean canView(CollectionUpdates collectionUpdates) {
		return true;
	}

	@Override
	protected VBox createDisplayPanel(CollectionUpdates collectionUpdates) {
		TableView<CatalogPlusUpdate> tableView = new TableView<>();

		TableColumn<CatalogPlusUpdate, String> columnAction = new TableColumn<>(UITextValues.UPDATES_LIST_HEADER_ACTION);
		columnAction.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().update.getAction().toString()));

		TableColumn<CatalogPlusUpdate, String> columnCatalog = new TableColumn<>(UITextValues.UPDATES_LIST_HEADER_CATALOG_NAME);
		columnCatalog.setCellValueFactory(param -> param.getValue().catalog);

		TableColumn<CatalogPlusUpdate, String> columnCoordinates = new TableColumn<>(UITextValues.UPDATES_LIST_HEADER_COORDINATES);
		columnCoordinates.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().update.getCoordinates().toString()));

		TableColumn<CatalogPlusUpdate, String> columnChangelog = new TableColumn<>(UITextValues.UPDATES_LIST_HEADER_CHANGELOG);
		columnChangelog.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().update.getChangelog()));

		collectionUpdates.forEach((catalogName, catalogUpdates) -> {
			catalogUpdates.forEach(catalogUpdate -> tableView.getItems().add(
					new CatalogPlusUpdate(new SimpleStringProperty(catalogName), catalogUpdate))
			);
		});
		tableView.getColumns().add(columnAction);
		tableView.getColumns().add(columnCatalog);
		tableView.getColumns().add(columnCoordinates);
		tableView.getColumns().add(columnChangelog);
		tableView.setBorder(Border.EMPTY);
		tableView.setBackground(Background.EMPTY);
		VBox vBox = new VBox(createUpdatesOptions(collectionUpdates.getAlbumInstallation()), tableView);
		vBox.setPadding(new Insets(5));
		return vBox;
	}

	private Node createUpdatesOptions(AlbumInstallation albumInstallation) {
		final Pane spacer = new Pane();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		spacer.setMinSize(10, 1);
		Button applyUpdates = new Button(UITextValues.UPDATES_ACTIONS_APPLY_BTN);
		applyUpdates.setPrefHeight(42);
		applyUpdates.setOnAction(event -> {
			try {
				albumService.upgrade(albumInstallation, collectionUpgradeEvent -> {
					uiService.show(albumInstallation);
					this.getDisplay().close();
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		HBox box = new HBox(spacer, applyUpdates);
		box.setPadding(new Insets(5));
		return box;
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

	static class CatalogPlusUpdate {
		StringProperty catalog;
		CatalogUpdate update;
		public CatalogPlusUpdate(StringProperty catalog, CatalogUpdate update) {
			this.catalog = catalog;
			this.update = update;
		}
	}
}
