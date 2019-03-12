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

public class TestsRunner extends BaseRunner {
	public void execute(Request request) throws Throwable {
		String configFile = request.configFile;
		String baseDir = request.baseDir;

		final Map<String, Bench> benchmarks = Utils.getBenchmarks(configFile);
		Files.walk(Paths.get(baseDir), 9999).forEach(e -> {
			String benchName = e.getFileName().toString().split("_")[0];
			if (!benchmarks.containsKey(benchName)) {
				return;
			}

			final Path base = Paths.get(e.toFile().getAbsolutePath(), "temp");
			final Path mutatationsLog = Paths.get(e.toFile().getAbsolutePath(), "temp", "coverage.log");

			final String reportsDir = Paths.get(e.toFile().getAbsolutePath(), "temp", "cov-reports").toFile()
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
					if (runJacoco(t, mutatationsLog, reportsDir + "/" + t.testName + ".exec") == 0) {
						log("Run tests on: " + t.testName);
					}

				});
			}

		});
	}

	public static int runJacoco(CompileRequest request, Path log, String reportsDir) {
		try {
			CommandLine line = new CommandLine("java")
					.addArgument("-javaagent:/home/junit/libs/jacocoagent.jar=destfile=" + reportsDir)
					.addArgument("-cp").addArgument(String.join(File.pathSeparator, request.getAllCpsForPit()));
			line.addArgument("org.junit.runner.JUnitCore");
			line.addArgument(request.testName);
			return Utils.launch(new File(request.workingDir), line, log);
		} catch (Throwable e) {
			log("was not able to run pit:" + request.testName + " at " + request.workingDir);
			return -1;
		}
	}
}
