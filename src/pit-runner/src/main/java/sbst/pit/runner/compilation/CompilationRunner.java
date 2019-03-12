package sbst.pit.runner.compilation;

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

public class CompilationRunner extends BaseRunner {

	@Override
	public void execute(Request request) throws Throwable {
		final Map<String, Bench> benchmarks = Utils.getBenchmarks(request.configFile);
		//Path outPath = Paths.get(request.baseDir, "failed-compilations.log");
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
			final Path compilationsLog = Paths.get(e.toFile().getAbsolutePath(), "temp", "compilation.log");
			final Path compiledTestsDirectory = Paths.get(base.toFile().getAbsolutePath(), "bin");

			if (compilationsLog.toFile().exists() && !request.force) {
				return;
			}
			List<CompileRequest> tests = getTests(request.libsDir, benchmarks.get(benchName), base);
			log("Found: " + tests.size() + " tests at " + e.toString());
			if (!tests.isEmpty()) {
				Utils.deleteOld(compiledTestsDirectory, true);
				Utils.deleteOld(compilationsLog, false);
				List<CompileRequest> compiledTests = new ArrayList<>();

				tests.forEach(t -> {
					if (compile(t, compilationsLog) == 0) {
						if (!t.testName.contains("_scaffolding")) {
							compiledTests.add(t);
						}
					}
				});

				log("Compiled: " + compiledTests.size() + " tests");
			}

		});

	}

	public static int compile(CompileRequest request, Path log) {
		try {
			List<String> cps = new ArrayList<>(request.getAllCps());
			cps.add(request.testBinDir);
			CommandLine line = new CommandLine("javac").addArgument("-Xlint:-unchecked").addArgument("-verbose")
					.addArgument("-s").addArgument(request.src).addArgument("-cp")
					.addArgument(String.join(File.pathSeparator, cps)).addArgument("-d").addArgument(request.testBinDir)
					.addArgument(request.sourceFile);
			return Utils.launch(new File(request.workingDir), line, log);
		} catch (Throwable e) {
			log("was not able to compile file " + request.testName + " at " + request.workingDir);
			return -1;
		}
	}
}
