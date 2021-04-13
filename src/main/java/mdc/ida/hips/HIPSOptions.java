package mdc.ida.hips;

import org.scijava.optional.AbstractOptions;

public class HIPSOptions extends AbstractOptions<HIPSOptions> {

	public final Values values = new Values();
	private static final String portKey = "port";

	public HIPSOptions() {
	}

	/**
	 * @return Default {@link HIPSOptions} instance
	 */
	public static HIPSOptions options()
	{
		return new HIPSOptions();
	}

	/**
	 * @param port Which port to use to start a TCP server
	 */
	public HIPSOptions port(int port) {
		return setValue(portKey, port);
	}

	public class Values extends AbstractValues
	{
		/**
		 * @return Which port to use to start a TCP server
		 */
		public int port() {
			return getValueOrDefault(portKey, 1234);
		}

	}
}
