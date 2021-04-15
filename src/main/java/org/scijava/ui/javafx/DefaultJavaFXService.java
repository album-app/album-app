package org.scijava.ui.javafx;

import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

@Plugin(type = Service.class)
public class DefaultJavaFXService extends AbstractService implements JavaFXService {
	boolean closing = false;

	@Override
	public boolean isClosing() {
		return closing;
	}

	@Override
	public void setClosing(boolean closing) {
		this.closing = closing;
	}
}
