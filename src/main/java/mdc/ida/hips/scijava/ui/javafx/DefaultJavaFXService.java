package mdc.ida.hips.scijava.ui.javafx;

import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

@Plugin(type = Service.class)
public class DefaultJavaFXService extends AbstractService implements JavaFXService {

	boolean closing = false;
	boolean noUIThread = false;

	@Override
	public boolean isHeadless() {
		return noUIThread;
	}

	@Override
	public void setHeadless(boolean noUIThread) {
		this.noUIThread = noUIThread;
	}

	@Override
	public boolean isClosing() {
		return closing;
	}

	@Override
	public void setClosing(boolean closing) {
		this.closing = closing;
	}
}
