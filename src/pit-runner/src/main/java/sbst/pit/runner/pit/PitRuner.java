package sbst.pit.runner.pit;

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

public class PitRuner extends BaseRunner {

	@Override
	public void execute(Request request) throws Throwable {
		final Map<String, Bench> benchmarks = Utils.getBenchmarks(request.configFile);
		Files.walk(Paths.get(request.baseDir), 9999).forEach(e -> {
			String[] temp = e.getFileName().toString().split("_");
			if (temp.length < 1) {
				return;
			}
			String benchName = temp[0];
			if (!benchmarks.containsKey(benchName)) {
				return;
			}
			final Path base = Paths.get(e.toFile().getAbsolutePath(), "temp");
			final Path mutatationsLog = Paths.get(e.toFile().getAbsolutePath(), "temp", "mutations.log");

			final String reportsDir = Paths.get(e.toFile().getAbsolutePath(), "temp", "pit-reports").toFile()
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
					if (runPit(t, mutatationsLog, reportsDir) == 0) {
						log("Run pit ok on: " + t.testName);
					}

				});
			}

		});

	}

	public static int runPit(CompileRequest request, Path log, String reportsDir) {
		try {
			CommandLine line = new CommandLine("java").addArgument("-cp")
					.addArgument(String.join(File.pathSeparator, request.getAllCpsForPit()));
			line.addArgument("org.pitest.mutationtest.commandline.MutationCoverageReport");
			line.addArgument("--verbose");

			line.addArgument("--timestampedReports");
			line.addArgument("false");
			line.addArgument("--exportLineCoverage");
			line.addArgument("true");
			line.addArgument("--mutators");
			line.addArgument("ALL");
			line.addArgument("--targetClasses");
			line.addArgument(String.join(",", request.bench.classes));
			line.addArgument("--targetTests");
			line.addArgument(request.testName);
			line.addArgument("--sourceDirs");
			line.addArgument(request.bench.src);
			line.addArgument("--reportDir");
			line.addArgument(reportsDir);
			line.addArgument("--outputFormats");
			line.addArgument("csv,html,xml");
			return Utils.launch(new File(request.workingDir), line, log);
		} catch (Throwable e) {
			log("was not able to run pit:" + request.testName + " at " + request.workingDir);
			return -1;
		}
	}
}
