package mdc.ida.album.ui.javafx.viewer;

import javafx.geometry.Insets;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import mdc.ida.album.model.Solution;
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
 * This class displays a {@link Solution}.
 */
@Plugin(type = DisplayViewer.class)
public class SolutionDisplayViewer extends EasyJavaFXDisplayViewer<Solution> {

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

	private Solution solution;

	public SolutionDisplayViewer() {
		super(Solution.class);
	}

	@Override
	protected boolean canView(Solution solution) {
		return true;
	}

	@Override
	protected VBox createDisplayPanel(Solution solution) {
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
