package mdc.ida.hips;

public final class Main {

	private Main() {}

	public static void main(final String... args) {
		System.out.println("Launching HIPS..");
		final HIPS hips = new HIPS();
		hips.launch(args);
	}

}
