package mdc.ida.hips;

import mdc.ida.hips.app.HIPSApp;
import mdc.ida.hips.model.HIPSInstallation;
import mdc.ida.hips.model.LocalHIPSInstallation;
import mdc.ida.hips.model.RemoteHIPSInstallation;
import mdc.ida.hips.scijava.ui.javafx.JavaFXService;
import mdc.ida.hips.service.HIPSServerService;
import mdc.ida.hips.service.conda.CondaService;
import org.scijava.AbstractGateway;
import org.scijava.Context;
import org.scijava.Gateway;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.SciJavaService;

import java.util.Arrays;
import java.util.List;

@Plugin(type = Gateway.class)
public class HIPS extends AbstractGateway {

	@Parameter
	private HIPSServerService hipsService;

	@Parameter
	private CondaService condaService;

	@Parameter
	private JavaFXService javaFXService;

	private final String DEFAULT_HOST_LOCAL = "http://127.0.0.1";

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

	public LocalHIPSInstallation loadLocalInstallation(String...args) {
		HIPSOptions.Values options = parseOptions(Arrays.asList(args));
		return loadLocalInstallation(options);
	}

	public LocalHIPSInstallation loadLocalInstallation(HIPSOptions.Values options) {
		log().info("Loading local HIPS installation..");
		LocalHIPSInstallation localInstallation = hipsService.loadLocalInstallation();
		if(options.port().isPresent()) localInstallation.setPort(options.port().get());
		if(!ui().isHeadless()) {
			ui().show("Welcome", localInstallation);
		}
		hipsService.runWithChecks(localInstallation);
		return localInstallation;
	}

	public RemoteHIPSInstallation loadRemoteInstallation(HIPSOptions.Values options) {
		if(options.host().isPresent() && !options.host().get().equals(DEFAULT_HOST_LOCAL)) {
			log().info("Loading remote HIPS installation from " + options.host() + ":" + options.port() + "..");
			return hipsService.loadRemoteInstallation(options.host().get(), options.port().get());
		} else {
			log().error("Cannot load remote installation");
			return null;
		}
	}

	private HIPSOptions.Values parseOptions(List<String> list) {
		HIPSOptions options = HIPSOptions.options();
		for (int i = 0; i < list.size(); i++) {
			String each = list.get(i);
			if (each.equals("--port") && list.size() > i+1) {
				options.port(Integer.parseInt(list.get(i+1)));
			}
		}
		return options.values;
	}

	/**
	 * Creates a new HIPS application context with all
	 * SciJava services.
	 */
	public HIPS() {
		this(new Context(SciJavaService.class));
	}

	/**
	 * Creates a new HIPS application context which wraps the given existing
	 * SciJava context.
	 * 
	 * @see Context
	 */
	public HIPS(final Context context) {
		super(HIPSApp.NAME, context);
	}

	@Override
	public String getShortName() {
		return "hips";
	}

	public HIPSServerService server() {
		return hipsService;
	}

	public CondaService conda() {
		return condaService;
	}

	public static void main(final String... args) {
		final HIPS hips = new HIPS();
		hips.launch(args);
		hips.loadLocalInstallation(args);
	}
}
