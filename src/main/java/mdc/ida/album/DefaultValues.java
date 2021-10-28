package mdc.ida.album;

import javafx.scene.text.Font;

public class DefaultValues {

	// server setup
	public static final String HOST_LOCAL = "http://127.0.0.1";

	// app artifact
	public static final String GROUP_ID = "mdc.ida";
	public static final String ARTIFACT_ID = "album-app";

	// behaviour
	public static final String DEFAULT_RECENT_SOLUTIONS_ACTION = "run";

	// GUI
	public static final int UI_SPACING = 15;
	public static final Font FONT_CONSOLE = Font.font("monospace", 14);
	public static final Font FONT_TITLE = Font.font(Font.getDefault().getFamily(), 20);
	public static final Font FONT_SECOND_TITLE = Font.font(Font.getDefault().getFamily(), 16);
	public static final long TASK_UPDATE_INTERVAL = 1000L;
	public static int UI_BUTTON_MIN_WIDTH = 200;

	public static final String ALBUM_PREF_LOCAL_PORT = "album.local.port";
	public static final String ALBUM_PREF_LOCAL_DEFAULT_CATALOG = "album.local.default_catalog";
	public static final String ALBUM_DEFAULT_CATALOG_URL = "https://gitlab.com/ida-mdc/capture-knowledge";
	public static final String ALBUM_ENVIRONMENT_NAME = "album";

}
