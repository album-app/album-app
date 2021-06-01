package mdc.ida.hips.model;

public interface HIPSInstallation {
	boolean canBeLaunched();
	void launch();
	String getHost();
	int getPort();
}
