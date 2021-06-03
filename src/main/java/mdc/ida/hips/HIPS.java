package mdc.ida.hips;

import mdc.ida.hips.app.HIPSApp;
import mdc.ida.hips.model.HIPSInstallation;
import mdc.ida.hips.model.LocalHIPSInstallation;
import mdc.ida.hips.scijava.ui.javafx.JavaFXService;
import mdc.ida.hips.service.HIPSServerService;
import mdc.ida.hips.service.conda.CondaService;
import org.scijava.AbstractGateway;
import org.scijava.Context;
import org.scijava.Gateway;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.SciJavaService;

import java.io.IOException;
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

	private final String DEFAULT_HOST_LOCAL = "localhost";

	@Override
	public void launch(String... args) {
		HIPSOptions.Values options = parseOptions(Arrays.asList(args));
		super.launch(args);
		try {
			loadInstallation(options);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void launchHeadless(String... args) {
		HIPSOptions.Values options = parseOptions(Arrays.asList(args));
		ui().setHeadless(true);
		javaFXService.setHeadless(true);
		super.launch(args);
		try {
			HIPSInstallation installation = loadInstallation(options);
			ui().show("Welcome", installation);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void initHeadless(String... args) {
		HIPSOptions.Values options = parseOptions(Arrays.asList(args));
		ui().setHeadless(true);
		javaFXService.setHeadless(true);
		if(options.host().isPresent() && !options.host().get().equals(DEFAULT_HOST_LOCAL)) {
			hipsService.loadRemoteInstallation(options.host().get(), options.port().get());
		} else {
			LocalHIPSInstallation localInstallation = hipsService.loadLocalInstallation();
			if(options.port().isPresent()) localInstallation.setPort(options.port().get());
		}
	}

	private HIPSInstallation loadInstallation(HIPSOptions.Values options) throws IOException, InterruptedException {
		if(options.host().isPresent() && !options.host().get().equals(DEFAULT_HOST_LOCAL)) {
			return hipsService.loadRemoteInstallation(options.host().get(), options.port().get());
		} else {
			LocalHIPSInstallation localInstallation = hipsService.loadLocalInstallation();
			if(options.port().isPresent()) localInstallation.setPort(options.port().get());
			if(!ui().isHeadless()) {
				ui().show("Welcome", localInstallation);
				hipsService.runWithChecks(localInstallation);
			}
			return localInstallation;
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
	}
}
