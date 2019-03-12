package sbst.pit.runner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;

import sbst.pit.runner.models.Bench;
import sbst.pit.runner.models.CompileRequest;
import sbst.pit.runner.models.Request;

public abstract class BaseRunner implements IExecutor {

	public static void log(String s) {
		System.out.println(s);
	}

	private String outDir;

	public BaseRunner(String outDir) {
		this.outDir = outDir;
	}

	public Path getOutputDir(Path current) {
		return Paths.get(current.toFile().getAbsolutePath(), this.outDir);
	}

	public void execute(Request request) throws Throwable {

		final ExecutorService service = Executors.newWorkStealingPool();
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
			final Path logFile = Paths.get(e.toFile().getAbsolutePath(), "temp",
					this.getClass().getSimpleName().toLowerCase() + ".log");

			final Path outDirectory = getOutputDir(base);

			if (logFile.toFile().exists() && !request.force) {
				return;
			}

			List<CompileRequest> tests = getTests(request.libsDir, benchmarks.get(benchName), base);
			log(this.getClass().getSimpleName() + " found " + tests.size());
			Utils.deleteOld(outDirectory, true);
			Utils.deleteOld(logFile, false);

			tests.forEach(t -> {
				service.submit(new Runnable() {

					@Override
					public void run() {
						if (innerExecute(base, request, logFile, t) != 0) {
							log(this.getClass().getSimpleName() + " ERROR " + t.testName + " "
									+ base.toAbsolutePath().toString());
							logError(request, t);
						} else {
							log(this.getClass().getSimpleName() + " OK " + t.testName + " "
									+ base.toAbsolutePath().toString());
						}

					}
				});

			});

		});

		service.shutdown();
		service.awaitTermination(2, TimeUnit.HOURS);
		service.shutdownNow();

	}

	public abstract int innerExecute(Path current, Request request, Path logFile, CompileRequest item);

	public void logError(Request request, CompileRequest data) {
		try {
			FileUtils.write(new File(request.baseDir, "errrors.txt"),
					this.getClass().getSimpleName() + "\t" + data.sourceFile + "\t" + data.testName + "\r\n", true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static List<CompileRequest> getTests(final List<String> libsDir, Bench bench, final Path base) {
		List<CompileRequest> tests = new ArrayList<>();
		final Path generatedTestsDirectory = Paths.get(base.toFile().getAbsolutePath(), "testcases");
		final Path compiledTestsDirectory = Paths.get(base.toFile().getAbsolutePath(), "bin");
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

}
