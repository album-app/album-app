package mdc.ida.hips.scijava.ui.javafx;

import org.scijava.service.SciJavaService;

public interface JavaFXService extends SciJavaService {
	boolean isClosing();

	void setClosing(boolean closing);
}
