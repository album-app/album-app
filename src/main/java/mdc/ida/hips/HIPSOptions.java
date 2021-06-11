package mdc.ida.hips;

import org.scijava.optional.AbstractOptions;

import java.util.Optional;

public class HIPSOptions extends AbstractOptions<HIPSOptions> {

	public final Values values = new Values();
	private static final String portKey = "port";
	private static final String hostKey = "host";

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
		return setValue(portKey, Optional.of(port));
	}

	/**
	 * @param host Which port to use to start a TCP server
	 */
	public HIPSOptions port(String host) {
		return setValue(hostKey, host);
	}

	public class Values extends AbstractValues
	{
		/**
		 * @return Which port to use to start a TCP server
		 */
		public Optional<Integer> port() {
			return getValueOrDefault(portKey, Optional.empty());
		}
		public Optional<String> host() {
			return getValueOrDefault(hostKey, Optional.empty());
		}

	}
}
