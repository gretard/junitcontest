package sbst.pit.runner.junit;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.exec.CommandLine;

import sbst.pit.runner.BaseRunner;
import sbst.pit.runner.Utils;
import sbst.pit.runner.models.CompileRequest;
import sbst.pit.runner.models.Request;

public class JacocoMetricsReporter extends BaseRunner {
	public JacocoMetricsReporter() {
		super("coverage-reports");
	}

	@Override
	public int innerExecute(Path current, Request request, Path logFile, CompileRequest item) {

		if (item.testName.contains("_scaffolding")) {
			return 0;
		}

		final String inputDir = Paths.get(current.toFile().getAbsolutePath(), "coverage-raw").toFile()
				.getAbsolutePath();
		final Path inputFile = Paths.get(inputDir, item.testName + ".exec");

		if (inputFile.toFile().exists()) {
			System.out.println("Input file not found "+inputFile);
			return -2;
		}

		final Path outDirectory = Paths.get(getOutputDir(current).toFile().getAbsolutePath(), item.testName);

		return runJacoco(item, logFile, inputFile, outDirectory.toFile().getAbsoluteFile().getAbsolutePath());

	}

	public static int runJacoco(CompileRequest request, Path log, Path inputFile, String reportsDir) {
		try {
			new File(reportsDir).mkdirs();
			CommandLine line = new CommandLine("java").addArgument("-jar")
					.addArgument("/home/junit/libs/jacococli.jar");
			line.addArgument("report");
			line.addArgument(inputFile.toFile().getAbsolutePath());
			line.addArgument("--classfiles");
			line.addArgument(String.join(",", request.getJacocoAllCps()));
			line.addArgument("--sourcefiles");
			line.addArgument(String.join(",", request.bench.src));
			line.addArgument("--html");
			line.addArgument(reportsDir + File.separator + "html");
			line.addArgument("--csv");
			line.addArgument(reportsDir + File.separator + "coverage.csv");
			return Utils.launch(new File(request.workingDir), line, log);
		} catch (Throwable e) {
			log("was not able to run pit:" + request.testName + " at " + request.workingDir);
			return -1;
		}
	}

}
