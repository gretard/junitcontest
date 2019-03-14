package sbst.pit.runner.junit;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.exec.CommandLine;

import sbst.pit.runner.BaseRunner;
import sbst.pit.runner.RunnerRequest;
import sbst.pit.runner.Utils;
import sbst.pit.runner.App.Modes;
import sbst.pit.runner.models.CompileRequest;
import sbst.pit.runner.models.Request;

public class JacocoMetricsReporter extends BaseRunner {
	public JacocoMetricsReporter() {
		super("coverage-reports", Modes.COVERAGE);
	}
	@Override
	public int innerExecute(RunnerRequest r) {
		
		List<String> tests = new ArrayList<String>();
		for (CompileRequest item : r.items) {
			if (item.testName.contains("_scaffolding")) {
				continue;
			}
			tests.add(item.testName);
		}
		if (tests.isEmpty()) {
			return -1;
		}
		final String inputDir = Paths.get(r.workingPath.toFile().getAbsolutePath(), "coverage-raw").toFile()
				.getAbsolutePath();
		final Path inputFile = Paths.get(inputDir, "data.exec");
		if (!inputFile.toFile().exists()) {
			System.out.println("Input file not found "+inputFile);
			return -1;
		}
		return runJacoco(r, inputFile);
		
		
	}
	

	public static int runJacoco(RunnerRequest request, Path inputFile) {
		try {
		
			CommandLine line = new CommandLine("java").addArgument("-jar")
					.addArgument("/home/junit/libs/jacococli.jar");
			line.addArgument("report");
			line.addArgument(inputFile.toFile().getAbsolutePath());
			for (String s : request.getJacocoAllCps()) {
				line.addArgument("--classfiles");
				line.addArgument(s);
			}
			line.addArgument("--sourcefiles");
			line.addArgument(String.join(",", request.bench.src));
			line.addArgument("--html");
			line.addArgument(request.outDirectory.toFile().getAbsolutePath() + File.separator + "html");
			line.addArgument("--csv");
			line.addArgument(request.outDirectory.toFile().getAbsolutePath() + File.separator + "coverage.csv");
			return Utils.launch(request.workingPath.toFile(), line, request.logFile);
		} catch (Throwable e) {
			e.printStackTrace();
			log("was not able instrument:" + request.workingPath);
			return -1;
		}
	}

}
