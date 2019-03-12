package sbst.pit.runner.junit;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.exec.CommandLine;

import sbst.pit.runner.BaseRunner;
import sbst.pit.runner.Utils;
import sbst.pit.runner.models.Bench;
import sbst.pit.runner.models.CompileRequest;
import sbst.pit.runner.models.Request;

public class JacocoMetricsReporter extends BaseRunner {
	public void execute(Request request) throws Throwable {
		String configFile = request.configFile;
		String baseDir = request.baseDir;

		final Map<String, Bench> benchmarks = Utils.getBenchmarks(configFile);
		Files.walk(Paths.get(baseDir), 9999).forEach(e -> {
			String[] temp = e.getFileName().toString().split("_");
			if (temp.length < 1) {
				return;
			}
			String benchName = temp[0];
			if (!benchmarks.containsKey(benchName)) {
				return;
			}

			final Path base = Paths.get(e.toFile().getAbsolutePath(), "temp");
			final Path mutatationsLog = Paths.get(e.toFile().getAbsolutePath(), "temp", "coverage-report.log");

			final String inputDir = Paths.get(e.toFile().getAbsolutePath(), "temp", "coverage-raw").toFile()
					.getAbsolutePath();
			
			final String reportsDir = Paths.get(e.toFile().getAbsolutePath(), "temp", "coverage-reports").toFile()
					.getAbsolutePath();
			
			if (mutatationsLog.toFile().exists() && !request.force) {
				return;
			}

			List<CompileRequest> tests = getTests(request.libsDir, benchmarks.get(benchName), base);

			if (!tests.isEmpty()) {
				Utils.deleteOld(Paths.get(reportsDir), true);
				Utils.deleteOld(mutatationsLog, false);
				List<CompileRequest> compiledTests = new ArrayList<>();
				log("Found: " + tests.size() + " tests at " + e.toString());
				tests.forEach(t -> {
					if (!t.testName.contains("_scaffolding")) {
						compiledTests.add(t);
					}

				});

				log("Running: " + compiledTests.size() + " tests");
				compiledTests.forEach(t -> {
					String inputFile = inputDir + "/" + t.testName + ".exec";
					if (runJacoco(t, mutatationsLog, inputFile, reportsDir+File.separatorChar+t.testName) == 0) {
						log("Run  jacoco on: " + t.testName);
					}

				});
			}

		});
	}

	public static int runJacoco(CompileRequest request, Path log, String inputFile, String reportsDir) {
		try {
			new File(reportsDir).mkdirs();
			CommandLine line = new CommandLine("java")
					.addArgument("-jar").addArgument("/home/junit/libs/jacococli.jar");
			line.addArgument("report");
			line.addArgument(inputFile);
			line.addArgument("--classfiles");
			line.addArgument(String.join(",", request.getJacocoAllCps()));
			line.addArgument("--sourcefiles");
			line.addArgument(String.join(",", request.bench.src));
			line.addArgument("--html");
			line.addArgument(reportsDir+File.separator+"html");
			line.addArgument("--csv");
			line.addArgument(reportsDir+File.separator+"coverage.csv");
			return Utils.launch(new File(request.workingDir), line, log);
		} catch (Throwable e) {
			log("was not able to run pit:" + request.testName + " at " + request.workingDir);
			return -1;
		}
	}
}
