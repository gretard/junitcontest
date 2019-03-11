package sbst.pit.runner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;

import sbst.pit.runner.metrics.MetricsCollector;
import sbst.pit.runner.models.BaseRequest;
import sbst.pit.runner.models.Bench;

/**
 * Hello world!
 *
 */
public class App {

	public static void main(String[] args) throws Throwable {
		System.out.println("Usage: force baseDir libsDir projectsConfigFile");
		final boolean force = args.length > 0 ? Boolean.parseBoolean(args[0]) : false;
		final String baseDir = args.length > 1 ? args[1] : ".";
		String configFile = "/var/benchmarks/conf/benchmarks.list";

		final List<String> libsDir = new ArrayList<>();
		libsDir.add("/home/junit/libs/*");

		if (args.length > 2) {
			libsDir.clear();
			libsDir.addAll(Arrays.asList(args[2].split(",")));
		}

		if (args.length > 3) {
			configFile = args[3];
		}

		compileAndRunPit(configFile, force, baseDir, libsDir);
		checkMutationsReports(Paths.get(baseDir, "summary.csv"), Paths.get(baseDir));
		collectMetrics(baseDir);
		collectBencharmkMetrics(baseDir, configFile);
	}

	private static void collectBencharmkMetrics(final String baseDir, String configFile) throws ConfigurationException {
		MetricsCollector collector = new MetricsCollector();

		final Map<String, Bench> benchmarks = Utils.getBenchmarks(configFile);
		Utils.deleteOld(Paths.get(baseDir, "metrics.csv"), false);
		File metricsFile = Paths.get(baseDir, "metrics.csv").toFile();
		benchmarks.forEach((k, v) -> {
			collector.collectMetrics(v, metricsFile);
		});

	}

	private static void collectMetrics(final String baseDir) throws IOException {
		MetricsCollector collector = new MetricsCollector();
		Utils.deleteOld(Paths.get(baseDir, "testMetrics.csv"), false);
		File metricsFile = Paths.get(baseDir, "testMetrics.csv").toFile();
		Files.walk(Paths.get(baseDir), 20)
				.filter(x -> x.toFile().getAbsolutePath().contains("temp" + File.separator + "bin" + File.separator))
				.forEach(path -> {
					if (!path.toFile().getAbsolutePath().endsWith(".class")) {
						return;
					}

					String filePath = path.toFile().getAbsolutePath().split("results")[1];
					String classPath = Paths.get(path.toFile().getAbsolutePath().split("temp.bin")[0], "temp", "bin")
							.toFile().getAbsolutePath();

					String[] temp = filePath.replace(File.separatorChar, '.').split("_", 4);
					String tool = temp[1];
					String budget = temp[2].split("\\.")[0];
					String benchmark = temp[2].split("\\.")[1];
					String classzName = temp[3].split("temp\\.bin")[1];
					classzName = classzName.substring(1, classzName.length() - 6);
					BaseRequest request = new BaseRequest();
					request.additionalInfoHeader = "benchmark\ttool\tbudget\t";
					request.additionalInfo = benchmark + "\t" + tool + "\t" + budget + "\t";
					request.classpath.add(classPath);
					request.classes.add(classzName);
					collector.collectMetrics(request, metricsFile);
				});

	}

	private static void compileAndRunPit(String configFile, final boolean force, String baseDir,
			final List<String> libsDir) throws IOException, ConfigurationException {
		final Map<String, Bench> benchmarks = Utils.getBenchmarks(configFile);
		Files.walk(Paths.get(baseDir), 8).forEach(e -> {
			String benchName = e.getFileName().toString().split("_")[0];
			if (!benchmarks.containsKey(benchName)) {
				return;
			}

			final Path base = Paths.get(e.toFile().getAbsolutePath(), "temp");
			final Path compilationsLog = Paths.get(e.toFile().getAbsolutePath(), "temp", "compilation.log");
			final Path mutatationsLog = Paths.get(e.toFile().getAbsolutePath(), "temp", "mutations.log");
			final Path reportsDir = Paths.get(e.toFile().getAbsolutePath(), "temp", "reports");
			final Path generatedTestsDirectory = Paths.get(base.toFile().getAbsolutePath(), "testcases");
			final Path compiledTestsDirectory = Paths.get(base.toFile().getAbsolutePath(), "bin");

			if (!generatedTestsDirectory.toFile().exists()) {
				return;
			}

			if (compilationsLog.toFile().exists() && mutatationsLog.toFile().exists() && !force) {
				return;
			}
			List<CompileRequest> tests = getTests(libsDir, benchmarks.get(benchName), base, reportsDir,
					generatedTestsDirectory, compiledTestsDirectory);

			if (!tests.isEmpty()) {
				Utils.deleteOld(compiledTestsDirectory, true);
				Utils.deleteOld(reportsDir, true);
				Utils.deleteOld(mutatationsLog, false);
				Utils.deleteOld(compilationsLog, false);
				List<CompileRequest> compiledTests = new ArrayList<>();
				log("Found: " + tests.size() + " tests at " + e.toString());
				tests.forEach(t -> {
					if (compile(t, compilationsLog) == 0) {
						if (!t.testName.contains("_scaffolding")) {
							compiledTests.add(t);
						}
					}
				});

				log("Compiled: " + compiledTests.size() + " tests");
				compiledTests.forEach(t -> {
					if (runPit(t, mutatationsLog) == 0) {
						log("Run pit ok on: " + t.testName);
					}

				});
			}

		});
	}

	private static List<CompileRequest> getTests(final List<String> libsDir, Bench bench, final Path base,
			final Path reportsDir, final Path generatedTestsDirectory, final Path compiledTestsDirectory) {
		List<CompileRequest> tests = new ArrayList<>();
		try {
			log("Searching for tests at " + generatedTestsDirectory);
			Files.walk(generatedTestsDirectory).filter(x -> x.toFile().getAbsolutePath().contains(".java"))
					.forEach(t -> {

						String sourceFilePath = t.toString().split("temp")[1].substring(1);
						String testName = sourceFilePath.replace("testcases", "").substring(1).replace(".java", "")
								.replace(File.separatorChar, '.');
						if ("SBSTDummyForCoverageAndMutationCalculation".equals(testName)) {
							return;
						}
						CompileRequest r = new CompileRequest();
						r.bench = bench;
						r.sourceFile = sourceFilePath;
						r.workingDir = base.toFile().getAbsolutePath();
						r.testName = testName;
						r.reportsDir = reportsDir.toFile().getAbsolutePath();
						r.testBinDir = compiledTestsDirectory.toFile().getAbsolutePath();
						r.extra.addAll(libsDir);
						if (testName.toLowerCase().contains("_scaffolding")) {
							tests.add(0, r);

						} else {
							tests.add(r);
						}

					});
		} catch (Throwable e1) {
			e1.printStackTrace();
		}
		return tests;
	}

	private static void checkMutationsReports(Path outPath, final Path reportsDir) {

		try {
			Utils.deleteOld(outPath, false);
			File outFile = outPath.toFile();
			log("Finding reports at " + reportsDir);
			Files.walk(reportsDir, 10).filter(x -> x.getFileName().toString().contains("mutations.csv")).forEach(x -> {

				try {
					Path u = Paths.get(x.toString().split("temp")[0]);
					final String benchName = u.getFileName().toString().split("_")[0];
					final String tool = u.getParent().toString().split("results_")[1].split("_")[0];
					final String budget = u.getParent().toString().split("results_")[1].split("_")[1];
					for (String line : FileUtils.readLines(x.toFile())) {
						FileUtils.write(outFile, benchName + "," + tool + "," + budget + "," + line + "\r\n", true);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			});
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

	public static int runPit(CompileRequest request, Path log) {
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
			line.addArgument(request.getReportsDir());
			line.addArgument("--outputFormats");
			line.addArgument("csv,html,xml");
			return Utils.launch(new File(request.workingDir), line, log);
		} catch (Throwable e) {
			log("was not able to run pit:" + request.testName + " at " + request.workingDir);
			return -1;
		}
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

	public static void log(String s) {
		System.out.println(s);
	}

}
