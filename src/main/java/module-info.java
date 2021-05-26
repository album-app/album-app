module mdc.ida.hips {
	opens mdc.ida.hips;
	opens mdc.ida.hips.app;
	opens mdc.ida.hips.commands;
	opens mdc.ida.hips.service;
	opens mdc.ida.hips.ui.javafx;
	opens mdc.ida.hips.scijava.ui.javafx;
	requires org.scijava;
	requires javafx.graphics;
	requires javafx.controls;
	requires com.fasterxml.jackson.databind;
	requires org.apache.commons.lang3;
	requires org.apache.commons.io;
}
