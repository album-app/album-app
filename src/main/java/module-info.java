module mdc.ida.album {
	opens mdc.ida.album to org.scijava;
	opens mdc.ida.album.model to javafx.base;
	opens mdc.ida.album.app to org.scijava;
	opens mdc.ida.album.commands to org.scijava;
	opens mdc.ida.album.control to org.scijava;
	opens mdc.ida.album.control.conda to org.scijava;
	opens mdc.ida.album.ui.javafx;
	opens mdc.ida.album.ui.javafx.viewer;
	opens mdc.ida.album.scijava.ui.javafx to org.scijava;
	opens mdc.ida.album.scijava.ui.javafx.viewer to org.scijava;
	opens mdc.ida.album.scijava.ui.javafx.widget to org.scijava;
	opens mdc.ida.album.model.event to javafx.base, org.scijava;
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
