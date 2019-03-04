package sbst.pit.runner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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

/**
 * Hello world!
 *
 */
public class App {

	private static class CompileRequest {
		public String reportsDir = "./reports";
		public List<String> extra = new ArrayList<>();

		public Bench bench;
		public String sourceFile;
		public String testBinDir = "./bin";
		public String src = "testcases";

		public String testName;
		public String workingDir;

		public String getReportsDir() {
			return reportsDir + File.separator + testName;
		}

		public List<String> getAllCps() {
			List<String> cps = new ArrayList<>();
			for (String x : bench.classpath) {
				String t = x;
				if (x.endsWith("dependency")) {
					t = x + File.separator + "*";
				}
				cps.add(t);
			}
			cps.addAll(extra);
			return cps;
		}

		public List<String> getAllCpsForPit() {
			List<String> cps = new ArrayList<>();
			for (String x : bench.classpath) {
				String t = x;
				if (x.endsWith("dependency")) {
					t = x + File.separator + "*";
				}
				cps.add(t);
			}
			cps.addAll(extra);
			cps.add(testBinDir);
			return cps;
		}
	}

	public static void main(String[] args) throws Throwable {
		final boolean force = args.length > 0 ? Boolean.parseBoolean(args[0]) : false;
		String baseDir = ".";
		String configFile = "/var/benchmarks/conf/benchmarks.list";

		final List<String> libsDir = new ArrayList<>();
		libsDir.add("/home/junit/libs/*");

		if (args.length > 1) {
			baseDir = args[1];
		}
		if (args.length > 2) {
			libsDir.clear();
			libsDir.addAll(Arrays.asList(args[2].split(",")));
		}

		if (args.length > 3) {
			configFile = args[3];
		}

		final Map<String, Bench> benchmarks = Utils.getBenchmarks(configFile);
		final File summaryOutFile = new File(baseDir, "summary.txt");
		summaryOutFile.delete();

		Files.walk(Paths.get(baseDir), 4).forEach(e -> {
			String benchName = e.getFileName().toString().split("_")[0];
			if (!benchmarks.containsKey(benchName)) {
				return;
			}
			final String configName = e.getParent().getFileName().toString();

			final Path base = Paths.get(e.toFile().getAbsolutePath(), "temp");
			final Path compilationsLog = Paths.get(e.toFile().getAbsolutePath(), "temp", "compilation.log");
			final Path mutatationsLog = Paths.get(e.toFile().getAbsolutePath(), "temp", "mutations.log");
			final Path reportsDir = Paths.get(e.toFile().getAbsolutePath(), "temp", "reports");
			final Path generatedTestsDirectory = Paths.get(base.toFile().getAbsolutePath(), "testcases");
			final Path compiledTestsDirectory = Paths.get(base.toFile().getAbsolutePath(), "bin");

			if (!Files.exists(generatedTestsDirectory)) {
				return;
			}

			List<CompileRequest> tests = new ArrayList<>();

			if (!Files.exists(compilationsLog) || !Files.exists(mutatationsLog)
					|| reportsDir.toFile().listFiles().length == 0 || force) {
				tests.addAll(getTests(libsDir, benchmarks.get(benchName), base, reportsDir, generatedTestsDirectory,
						compiledTestsDirectory));
			}

			if (!tests.isEmpty()) {
				deleteOld(compiledTestsDirectory, true);
				deleteOld(reportsDir, true);
				deleteOld(mutatationsLog, false);
				deleteOld(compilationsLog, false);
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
			checkMutationsReports(benchName, configName, summaryOutFile, reportsDir);
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

	private static void checkMutationsReports(String benchName, String config, File outFile, final Path reportsDir) {
		if (!Files.exists(reportsDir)) {
			return;
		}
		try {
			log("Finding reports at " + reportsDir);
			Files.walk(reportsDir, 4).filter(x -> x.getFileName().toString().contains("mutations.csv")).forEach(x -> {

				try {
					final String tool = config.replace("results_", "").split("_")[0];
					final String budget = config.replace("results_", "").split("_")[1];
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

	private static void deleteOld(Path path, boolean isDir) {

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
			return launch(new File(request.workingDir), line, log);
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
			return launch(new File(request.workingDir), line, log);
		} catch (Throwable e) {
			log("was not able to compile file " + request.testName + " at " + request.workingDir);
			return -1;
		}
	}

	private static void log(String s) {
		System.out.println(s);
	}

	private static int launch(File baseDir, CommandLine line, Path outFile) throws Throwable {
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
			return exitValue;
		} catch (Throwable e) {
			e.printStackTrace();
			log("An exception occurred during the execution of command " + line.toString());
			return -1;
		} finally {
			outStream.close();
			errStream.close();
		}
	}
}
