package mdc.ida.hips.service.conda;

import org.scijava.service.SciJavaService;

import java.io.File;
import java.io.IOException;

public interface CondaService extends SciJavaService {
	boolean checkIfCondaInstalled(File condaPath);
	void installConda(File condaPath) throws IOException;
	boolean checkIfEnvironmentExists(File condaPath, String environmentName);

	String[] createCondaCommandLinuxMac(File condaPath, String commandInCondaEnv);

	String[] createCondaCommandWindows(File condaPath, String commandInCondaEnv) throws IOException;

	void setDefaultCondaPath(File condaPath);
	File getDefaultCondaPath();
	void createEnvironment(File condaPath, File environmentYamlFile) throws IOException, InterruptedException;
	String getCondaExecutable(File condaPath);
	String getEnvironmentPath(File condaPath);
}
