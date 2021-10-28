package mdc.ida.album.ui.javafx.viewer;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;
import mdc.ida.album.DefaultValues;
import mdc.ida.album.model.InstallationTasks;
import mdc.ida.album.model.LogAddedEvent;
import mdc.ida.album.model.LogEntry;
import mdc.ida.album.model.Task;
import mdc.ida.album.scijava.ui.javafx.viewer.EasyJavaFXDisplayViewer;
import org.scijava.Context;
import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.viewer.DisplayViewer;

import java.util.List;

import static mdc.ida.album.ui.javafx.viewer.JavaFXUtils.makeColumn;
import static mdc.ida.album.ui.javafx.viewer.JavaFXUtils.scrollable;

/**
 * This class displays {@link mdc.ida.album.model.InstallationTasks}.
 */
@Plugin(type = DisplayViewer.class)
public class InstallationTasksDisplayViewer extends EasyJavaFXDisplayViewer<InstallationTasks> {

	@Parameter
	private Context context;

	@Parameter
	private EventService eventService;
	private TableView<Task> tasksView;
	private TableView<LogEntry> logView;

	public InstallationTasksDisplayViewer() {
		super(InstallationTasks.class);
	}

	@Override
	protected boolean canView(InstallationTasks tasks) {
		return true;
	}

	@Override
	protected Node createDisplayPanel(InstallationTasks tasks) {
		logView = new TableView<>();
		TableColumn<LogEntry, String> levelCol = makeColumn("Level", "levelName");
		logView.getColumns().add(levelCol);
		TableColumn<LogEntry, String> msgCol = makeColumn("Message", "msg");
		msgCol.setCellFactory(tc -> {
			TableCell<LogEntry, String> cell = new TableCell<>();
			Text text = new Text();
			cell.setGraphic(text);
			cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
			text.wrappingWidthProperty().bind(msgCol.widthProperty());
			text.textProperty().bind(cell.itemProperty());
			return cell ;
		});
		logView.getColumns().add(msgCol);
		double width = levelCol.widthProperty().get() + 20;
		msgCol.prefWidthProperty().bind(logView.widthProperty().subtract(width));
		tasksView = new TableView<>();
		TableColumn<Task, String> statusCol = makeColumn("Status", "status");
		statusCol.setMinWidth(100);
		tasksView.getColumns().add(statusCol);
		tasksView.getColumns().add(makeColumn("ID", "id"));
		tasksView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null) {
				updateLogView(logView, newSelection);
			}
		});
		SplitPane split = new SplitPane(scrollable(tasksView), scrollable(logView));
		split.setDividerPosition(0, 0.3);
		SplitPane.setResizableWithParent(tasksView, Boolean.FALSE);
		split.setPadding(new Insets(DefaultValues.UI_SPACING));
		return split;
	}

	private void updateLogView(TableView<LogEntry> logView, Task task) {
		logView.getItems().clear();
		logView.getItems().addAll(task.getLogs());
	}

	@EventHandler
	private void tasksUpdated(LogAddedEvent e) {
		if(tasksView != null) {
			List<LogEntry> logs = e.getLogEntries();
			Task task = e.getTask();
			if(!tasksView.getItems().contains(task)) {
				tasksView.getItems().add(task);
			}
			Task selectedItem = tasksView.getSelectionModel().getSelectedItem();
			if(selectedItem == null) {
				tasksView.getSelectionModel().select(task);
			}
			else if(selectedItem.equals(task)) {
				updateLogView(logView, task);
			}
			tasksView.refresh();
		}
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
