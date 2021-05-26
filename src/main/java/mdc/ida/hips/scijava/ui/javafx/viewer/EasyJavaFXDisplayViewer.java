package mdc.ida.hips.scijava.ui.javafx.viewer;

import javafx.scene.layout.VBox;
import mdc.ida.hips.scijava.ui.javafx.JavaFXUI;
import org.scijava.display.Display;
import org.scijava.display.event.DisplayDeletedEvent;
import org.scijava.object.ObjectService;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UserInterface;
import org.scijava.ui.viewer.AbstractDisplayViewer;
import org.scijava.ui.viewer.DisplayPanel;
import org.scijava.ui.viewer.DisplayViewer;
import org.scijava.ui.viewer.DisplayWindow;

abstract public class EasyJavaFXDisplayViewer< T >
		extends AbstractDisplayViewer< T > implements DisplayViewer< T >
{

	private final Class< T > classOfObject;

	@Parameter
	ObjectService objectService;

	protected EasyJavaFXDisplayViewer(Class< T > classOfObject)
	{
		this.classOfObject = classOfObject;
	}

	@Override
	public boolean isCompatible(final UserInterface ui) {
		return ui instanceof JavaFXUI;
	}

	@Override
	public boolean canView(final Display< ? > d) {
		Object object = d.get(0);
		if (!classOfObject.isInstance(object)) return false;
		T value = (T) object;
		return canView(value);
	}

	protected abstract boolean canView(T value);

	protected abstract void redoLayout();

	protected abstract void setLabel(final String s);

	protected abstract void redraw();

	protected abstract VBox createDisplayPanel(T value);

	@Override
	public void onDisplayDeletedEvent(DisplayDeletedEvent e)
	{
		super.onDisplayDeletedEvent(e);
		objectService.removeObject(getDisplay().get(0));
	}

	@Override
	public void view(final DisplayWindow w, final Display< ? > d) {
		objectService.addObject(d.get(0));
		super.view(w, d);
		final VBox content = createDisplayPanel(getDisplay().get(0));
		setPanel(new JavaFXDisplayPanel(w, d, this, content));
	}

	public static class JavaFXDisplayPanel extends VBox
			implements DisplayPanel
	{

		// -- instance variables --

		private final EasyJavaFXDisplayViewer< ? > viewer;
		private final DisplayWindow window;
		private final Display< ? > display;

		// -- PlotDisplayPanel methods --

		public JavaFXDisplayPanel(DisplayWindow window, Display< ? > display,
		                          EasyJavaFXDisplayViewer< ? > viewer, VBox panel)
		{
			this.window = window;
			this.display = display;
			this.viewer = viewer;
			window.setContent(this);
			getChildren().add(panel);
		}

		@Override
		public Display< ? > getDisplay() {
			return display;
		}

		// -- DisplayPanel methods --

		@Override
		public DisplayWindow getWindow() {
			return window;
		}

		@Override
		public void redoLayout()
		{
			viewer.redoLayout();
		}

		@Override
		public void setLabel(String s)
		{
			viewer.setLabel(s);
		}

		@Override
		public void redraw()
		{
			viewer.redraw();
		}
	}
}
