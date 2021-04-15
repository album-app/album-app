/*
 * 
 * #%L
 * SciJava UI components for Java Swing.
 * %%
 * Copyright (C) 2010 - 2017 Board of Regents of the University of
 * Wisconsin-Madison.
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

package org.scijava.ui.javafx.console;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import mdc.ida.hips.ui.javafx.HIPSApplicationFrame;
import org.scijava.Context;
import org.scijava.console.OutputEvent;
import org.scijava.ui.console.AbstractConsolePane;
import org.scijava.ui.console.ConsolePane;

/**
 * JavaFX implementation of {@link ConsolePane}.
 *
 */
public class JavaFXConsolePane extends AbstractConsolePane<TextFlow> {

	private final TextFlow box;
	private final Tab tab;
	private TabPane tabs;

	public JavaFXConsolePane(final Context context) {
		super(context);
		this.box = new TextFlow();
		box.setPadding(new Insets(10));
		box.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
		ScrollPane content = new ScrollPane(box);
		content.setStyle("-fx-background: #000000;");
		tab = new Tab("Console", content);
	}

	@Override
	public void append(final OutputEvent event) {
		Text text = new Text(event.getOutput());
		if(event.isStderr()) {
			text.setFill(Color.RED);
		} else {
			text.setFill(Color.WHITE);
		}
		box.getChildren().add(text);
	}

	@Override
	public void show() {
		Platform.runLater(() -> {
			if(tabs == null) return;
			if(tabs.getTabs() == null) return;
			if(tabs.getTabs().contains(tab)) {
				tabs.getSelectionModel().select(tab);
			} else {
				tabs.getTabs().add(tab);
			}
		});
	}

	@Override
	public TextFlow getComponent() {
		return box;
	}

	@Override
	public Class<TextFlow> getComponentType() {
		return TextFlow.class;
	}

	public void setTabPane(TabPane tabs) {
		this.tabs = tabs;
	}
}
