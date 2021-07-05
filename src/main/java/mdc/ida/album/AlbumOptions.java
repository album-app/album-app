package mdc.ida.album;

import org.scijava.optional.AbstractOptions;

import java.util.Optional;

public class AlbumOptions extends AbstractOptions<AlbumOptions> {

	public final Values values = new Values();
	private static final String portKey = "port";
	private static final String hostKey = "host";

	public AlbumOptions() {
	}

	/**
	 * @return Default {@link AlbumOptions} instance
	 */
	public static AlbumOptions options()
	{
		return new AlbumOptions();
	}

	/**
	 * @param port Which port to use to start a TCP server
	 */
	public AlbumOptions port(int port) {
		return setValue(portKey, Optional.of(port));
	}

	/**
	 * @param host Which port to use to start a TCP server
	 */
	public AlbumOptions port(String host) {
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
