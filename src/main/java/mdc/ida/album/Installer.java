package mdc.ida.album;

public class Installer {
	public static void main(final String... args) {
		final Album album = new Album();
		album.launch(args);
		album.loadLocalInstallation(args);
	}
}
