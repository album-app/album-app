/*
 * #%L
 * SciJava UI components for Java Swing.
 * %%
 * Copyright (C) 2010 - 2020 SciJava developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package mdc.ida.album.scijava.ui.javafx.widget;

import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import org.scijava.log.LogService;
import org.scijava.plugin.Plugin;
import org.scijava.widget.InputWidget;
import org.scijava.widget.TextWidget;
import org.scijava.widget.WidgetModel;

/**
 * Swing implementation of text field widget.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = InputWidget.class)
public class JavaFXTextWidget extends JavaFXInputWidget<String> implements TextWidget<HBox>
{

	private LogService log;

	private TextField textComponent;

	// -- InputWidget methods --

	@Override
	public String getValue() {
		return textComponent.getText();
	}

	// -- WrapperPlugin methods --

	@Override
	public void set(final WidgetModel model) {
		super.set(model);
		log = model.getContext().getService(LogService.class);

		final int columns = model.getItem().getColumnCount();

		// construct text widget of the appropriate style, if specified
		boolean addScrollPane = false;
		if (model.isStyle(TextWidget.AREA_STYLE)) {
			textComponent = new TextField();
			addScrollPane = true;
		}
		else if (model.isStyle(TextWidget.PASSWORD_STYLE)) {
			textComponent = new PasswordField();
		}
		else {
			textComponent = new TextField();
		}
		textComponent.textProperty().addListener((observable, oldValue, newValue) -> {
			updateModel();
		});
		getComponent().getChildren().add(addScrollPane ? new ScrollPane(textComponent) : textComponent);


		refreshWidget();
	}

	// -- Typed methods --

	@Override
	public boolean supports(final WidgetModel model) {
		return super.supports(model) && model.isText() &&
			!model.isMultipleChoice() && !model.isMessage();
	}

	// -- AbstractUIInputWidget methods ---

	@Override
	public void doRefresh() {
		final String text = get().getText();
		if (textComponent.getText().equals(text)) return; // no change
		textComponent.setText(text);
	}

}
