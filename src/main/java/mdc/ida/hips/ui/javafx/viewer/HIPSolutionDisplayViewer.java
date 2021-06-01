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
import mdc.ida.hips.scijava.ui.javafx.viewer.EasyJavaFXDisplayViewer;
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
