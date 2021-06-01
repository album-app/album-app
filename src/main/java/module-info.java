module mdc.ida.hips {
	opens mdc.ida.hips to org.scijava;
	opens mdc.ida.hips.model to javafx.base;
	opens mdc.ida.hips.app to org.scijava;
	opens mdc.ida.hips.commands to org.scijava;
	opens mdc.ida.hips.service to org.scijava;
	opens mdc.ida.hips.service.conda to org.scijava;
	opens mdc.ida.hips.ui.javafx to org.scijava;
	opens mdc.ida.hips.ui.javafx.viewer to org.scijava;
	opens mdc.ida.hips.scijava.ui.javafx to org.scijava;
	opens mdc.ida.hips.scijava.ui.javafx.viewer to org.scijava;
	opens mdc.ida.hips.scijava.ui.javafx.widget to org.scijava;
	requires org.scijava;
	requires javafx.graphics;
	requires javafx.controls;
	requires javafx.fxml;
	requires com.fasterxml.jackson.databind;
	requires org.apache.commons.lang3;
	requires org.apache.commons.io;
	requires org.scijava.optional;
	requires commons.exec;
	requires org.apache.httpcomponents.httpclient;
	requires org.apache.httpcomponents.httpcore;
}
