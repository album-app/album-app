package mdc.ida.album;

import mdc.ida.album.model.LocalAlbumInstallation;
import mdc.ida.album.model.LocalInstallationLoadedEvent;
import mdc.ida.album.model.RemoteAlbumInstallation;
import mdc.ida.album.scijava.ui.javafx.JavaFXService;
import mdc.ida.album.service.AlbumServerService;
import mdc.ida.album.service.conda.CondaService;
import org.scijava.AbstractGateway;
import org.scijava.Context;
import org.scijava.Gateway;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.SciJavaService;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@Plugin(type = Gateway.class)
public class Album extends AbstractGateway {

	@Parameter
	private AlbumServerService albumService;

	@Parameter
	private CondaService condaService;

	@Parameter
	private JavaFXService javaFXService;

	@Override
	public void launch(String... args) {
		super.launch(args);
	}

	public void launchHeadless(String... args) {
		// TODO catch args in launch and use this method if headless arg is provided
		setHeadless();
		super.launch(args);
	}

	public void setHeadless() {
		ui().setHeadless(true);
		javaFXService.setHeadless(true);
	}

	public void loadLocalInstallation(String...args) {
		loadLocalInstallation(e -> {}, args);
	}

	public void loadLocalInstallation(Consumer<LocalInstallationLoadedEvent> callback, String...args) {
		AlbumOptions.Values options = parseOptions(Arrays.asList(args));
		loadLocalInstallation(callback, options);
	}

	public void loadLocalInstallation(AlbumOptions.Values options) {
		loadLocalInstallation(e -> {}, options);
	}

	public void loadLocalInstallation(Consumer<LocalInstallationLoadedEvent> callback, AlbumOptions.Values options) {
		log().info("Loading local album installation..");
		LocalAlbumInstallation localInstallation = albumService.loadLocalInstallation();
		if(options.port().isPresent()) localInstallation.setPort(options.port().get());
		if(!ui().isHeadless()) {
			ui().show("Welcome", localInstallation);
		}
		albumService.runWithChecks(localInstallation, callback);
	}

	public RemoteAlbumInstallation loadRemoteInstallation(AlbumOptions.Values options) {
		if(options.host().isPresent() && !options.host().get().equals(DefaultValues.HOST_LOCAL)) {
			log().info("Loading remote album installation from " + options.host() + ":" + options.port() + "..");
			return albumService.loadRemoteInstallation(options.host().get(), options.port().get());
		} else {
			log().error("Cannot load remote installation");
			return null;
		}
	}

	private AlbumOptions.Values parseOptions(List<String> list) {
		AlbumOptions options = AlbumOptions.options();
		for (int i = 0; i < list.size(); i++) {
			String each = list.get(i);
			if (each.equals("--port") && list.size() > i+1) {
				options.port(Integer.parseInt(list.get(i+1)));
			}
		}
		return options.values;
	}

	/**
	 * Creates a new album application context with all
	 * SciJava services.
	 */
	public Album() {
		this(new Context(SciJavaService.class));
	}

	/**
	 * Creates a new album application context which wraps the given existing
	 * SciJava context.
	 * 
	 * @see Context
	 */
	public Album(final Context context) {
		super(mdc.ida.album.app.AlbumApp.NAME, context);
	}

	@Override
	public String getShortName() {
		return "album";
	}

	public AlbumServerService server() {
		return albumService;
	}

	public CondaService conda() {
		return condaService;
	}

	public static void main(final String... args) {
		final Album album = new Album();
		album.launch(args);
		album.loadLocalInstallation(args);
	}
}
