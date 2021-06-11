package mdc.ida.hips.scijava.ui.javafx;

import org.scijava.service.SciJavaService;

public interface JavaFXService extends SciJavaService {
	boolean isHeadless();

	void setHeadless(boolean noUIThread);

	boolean isClosing();

	void setClosing(boolean closing);
}
