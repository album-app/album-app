package mdc.ida.hips.service.conda;

import mdc.ida.hips.service.DefaultHIPSServerService;
import mdc.ida.hips.utils.StreamGobbler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.scijava.event.EventService;
import org.scijava.platform.PlatformService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.prefs.PrefService;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@Plugin(type = Service.class)
public class DefaultCondaService extends AbstractService implements CondaService {

	@Parameter
	private PlatformService platformService;

	@Parameter
	private PrefService prefService;

	@Parameter
	private EventService eventService;

	private static final String CONDA_DOWNLOAD_Linux = "https://repo.anaconda.com/miniconda/Miniconda3-latest-Linux-x86_64.sh";
	private static final String CONDA_DOWNLOAD_MacOSX = "https://repo.anaconda.com/miniconda/Miniconda3-latest-MacOSX-x86_64.sh";
	private static final String CONDA_DOWNLOAD_Windows = "https://repo.anaconda.com/miniconda/Miniconda3-latest-Windows-x86_64.exe";
	private static final String DEFAULT_CONDA_PATH_KEY = "conda.path.default";

	@Override
	public boolean checkIfCondaInstalled(File condaPath) {
		if(condaPath == null || !condaPath.exists()) return false;
		try {
			String condaExecutable = getCondaExecutable(condaPath);
			if(!new File(condaExecutable).exists()) {
				log().warn("Cannot find conda executable " + condaExecutable);
				return false;
			}
			Process p = new ProcessBuilder(condaExecutable, "--version").start();
			StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), log());
			StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), log());
			errorGobbler.start();
			outputGobbler.start();
			p.waitFor();
			if(errorGobbler.failed()) return false;

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return false;
		}
		eventService.publish(new HasCondaInstalledEvent(condaPath));
		return true;
	}

	@Override
	public void installConda(File condaPath) throws IOException {
		if(!condaPath.exists()) condaPath.mkdirs();
		if(SystemUtils.IS_OS_LINUX) installCondaLinux(condaPath);
		else if(SystemUtils.IS_OS_MAC_OSX) installCondaMacOSX(condaPath);
		else if(SystemUtils.IS_OS_WINDOWS) installCondaWindows(condaPath);
		else log().error("Cannot install conda for your operating system");
	}

	@Override
	public boolean checkIfEnvironmentExists(File condaPath, String environmentName) {
		if(condaPath == null || !condaPath.exists()) return false;
		boolean exists = new File(new File(condaPath, "envs"), environmentName).exists();
		if(exists) eventService.publish(new CondaEnvironmentDetectedEvent(condaPath, environmentName));
		return exists;
	}

	@Override
	public void createEnvironment(File condaPath, File environmentYamlFile) throws IOException, InterruptedException {
		String[] command;
		String commandInCondaEnv = "env create -f "+environmentYamlFile.getAbsolutePath();
		if(SystemUtils.IS_OS_WINDOWS) {
			command = createCondaCommandWindows(condaPath, commandInCondaEnv);
		} else {
			command = createCondaCommandLinuxMac(condaPath, commandInCondaEnv);
		}
		log().info("Running " + Arrays.toString(command));
		Process p = new ProcessBuilder(command)
				.inheritIO().start();
		p.waitFor();
	}

	@Override
	public String[] createCondaCommandLinuxMac(File condaPath, String commandInCondaEnv) {
		String[] commands = commandInCondaEnv.split(" ");
		String[] res = new String[commands.length+1];
		res[0] = getCondaExecutable(condaPath);
		System.arraycopy(commands, 0, res, 1, commands.length);
		return res;
	}

	@Override
	public String[] createCondaCommandWindows(File condaPath, String commandInCondaEnv) throws IOException {
		Path file = Files.createTempFile("hips", ".bat");
		String content =
				"set root="+condaPath.getAbsolutePath()+"\n" +
				"call %root%\\Scripts\\activate.bat %root%\n" +
				"call conda " + commandInCondaEnv;
		FileUtils.writeStringToFile(file.toFile(), content, Charset.defaultCharset());
		return new String[] {file.toFile().getAbsolutePath()};
	}

	@Override
	public void setDefaultCondaPath(File condaPath) {
		prefService.put(DefaultHIPSServerService.class, DEFAULT_CONDA_PATH_KEY, condaPath.getAbsolutePath());
	}

	@Override
	public File getDefaultCondaPath() {
		String path = prefService.get(DefaultHIPSServerService.class, DEFAULT_CONDA_PATH_KEY, null);
		if(path == null) return null;
		return new File(path);
	}

	@Override
	public String getCondaExecutable(File condaPath) {
		if(SystemUtils.IS_OS_WINDOWS) return new File(new File(condaPath, "Scripts"), "conda.exe").getAbsolutePath();
		else return new File(new File(condaPath, "bin"), "conda").getAbsolutePath();
	}

	@Override
	public String getEnvironmentPath(File condaPath) {
		return new File(new File(condaPath, "envs"), "hips").getAbsolutePath();
	}

	private void installCondaLinux(File target) throws IOException {
		installConda(target, CONDA_DOWNLOAD_Linux);
	}

	private void installCondaMacOSX(File target) throws IOException {
		installConda(target, CONDA_DOWNLOAD_MacOSX);
	}

	private void installConda(File target, String url) throws IOException {
		File scriptFile = downloadTmpFile(url, "miniconda.sh");
		String minicondaPath = target.getAbsolutePath();
		log().info("Installing miniconda to " + minicondaPath + "..");
		platformService.exec("bash", scriptFile.getAbsolutePath(), "-b", "-f", "-p", minicondaPath);
	}

	private void installCondaWindows(File target) throws IOException {
		File scriptFile = downloadTmpFile(CONDA_DOWNLOAD_Windows, "conda.exe");
		String minicondaPath = target.getAbsolutePath();
		log().info("Installing miniconda to " + minicondaPath + "..");
		platformService.exec(scriptFile.getAbsolutePath(), "/AddToPath=0", "/InstallationType=JustMe",
				"/RegisterPython=0", "/S", "/D=" + minicondaPath);
	}

	private File downloadTmpFile(String url, String name) throws IOException {
		Path dir = Files.createTempDirectory("hips-installer");
		File scriptFile = new File(dir.toFile(), name);
		log().info("Downloading " + url + " to " + scriptFile.getAbsolutePath() + "..");
		FileUtils.copyURLToFile(new URL(url), scriptFile, 10000, 1000000);
		return scriptFile;
	}



}
