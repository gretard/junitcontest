package sbst.pit.runner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.plist.PropertyListConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;

import sbst.pit.runner.models.Bench;

public class Utils {
	public static void deleteOld(Path path, boolean isDir) {

		File f = path.toFile();
		if (isDir) {
			try {

				FileUtils.deleteDirectory(f);
				f.delete();
				f.mkdirs();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			f.delete();
			f.getParentFile().mkdirs();
		}
	}

	public static int launch(File baseDir, CommandLine line, Path outFile) throws Throwable {
		DefaultExecutor executor = new DefaultExecutor();
		File f = new File(outFile.toFile().getAbsolutePath());
		if (!f.exists()) {
			f.createNewFile();
		}

		FileOutputStream outStream = new FileOutputStream(f, true);
		FileOutputStream errStream = new FileOutputStream(f, true);
		try {
			FileUtils.write(f, "cd " + baseDir.getAbsolutePath() + "\r\n", true);
			FileUtils.write(f, line.toString() + "\r\n", true);
			PumpStreamHandler streamHandler = new PumpStreamHandler(outStream, errStream, null);
			executor.setStreamHandler(streamHandler);
			executor.setWorkingDirectory(baseDir);
			int exitValue = executor.execute(line);
			// System.out.println("Run ok: "+line);
			return exitValue;
		} catch (Throwable e) {
			System.out.println("An exception occurred during the execution of command " + line.toString());
			return -1;
		} finally {
			outStream.close();
			errStream.close();
		}
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Bench> getBenchmarks(String configFile) throws ConfigurationException {
		PropertyListConfiguration benchmarkList = new PropertyListConfiguration();
		benchmarkList.load(new File(configFile));
		Map<String, Bench> exes = new HashMap<>();

		for (ConfigurationNode child : benchmarkList.getRoot().getChildren()) {
			Bench b = new Bench();
			b.name = child.getName();
			b.src = (String) child.getChildren("src").get(0).getValue();
			b.classpath.addAll((List<String>) child.getChildren("classpath").get(0).getValue());
			b.classes.addAll((List<String>) child.getChildren("classes").get(0).getValue());
			exes.put(b.name, b);

		}
		return exes;
	}
}
