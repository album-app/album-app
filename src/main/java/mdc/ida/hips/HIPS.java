package mdc.ida.hips;

import mdc.ida.hips.app.HIPSApp;
import mdc.ida.hips.service.HIPSServerService;
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

	@Override
	public void launch(String... args) {
		HIPSOptions.Values options = parseOptions(Arrays.asList(args));
		super.launch(args);
		hipsService.init(options);
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

	public static void main(final String... args) {
		final HIPS hips = new HIPS();
		hips.launch(args);
	}
}
