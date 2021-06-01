package mdc.ida.hips;

public class Main {
	public static void main(final String... args) {
		final HIPS hips = new HIPS();
//		hips.log().setLevel(LogLevel.DEBUG);
		hips.launch(args);
	}
}
