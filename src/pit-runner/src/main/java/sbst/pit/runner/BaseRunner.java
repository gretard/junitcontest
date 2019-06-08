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
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.FileUtils;

import sbst.pit.runner.App.Modes;
import sbst.pit.runner.models.Bench;
import sbst.pit.runner.models.CompileRequest;
import sbst.pit.runner.models.Request;

public abstract class BaseRunner implements IExecutor {

	private Modes mode;

	public static void log(String s) {
		System.out.println(s);
	}

	private String outDir;

	public BaseRunner(String outDir, Modes mode) {
		this.outDir = outDir;
		this.mode = mode;
	}

	public Path getOutputDir(Path current) {
		return Paths.get(current.toFile().getAbsolutePath(), this.outDir);
	}

	public void execute(Request request) throws Throwable {
		final ExecutorService service = Executors.newWorkStealingPool(8);
		final Map<String, Bench> benchmarks = Utils.getBenchmarks(request.configFile);

		AtomicLong tasksCount = new AtomicLong();
		AtomicLong totalCount = new AtomicLong();
		AtomicLong totalRun = new AtomicLong();

		Files.walk(Paths.get(request.baseDir), 9999).forEach(e -> {

			try {
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

				boolean forceRun = this.mode.isSet(request.mode);
				totalCount.incrementAndGet();
				if (!forceRun && logFile.toFile().exists() && outDirectory.toFile().exists()
						&& outDirectory.toFile().listFiles().length > 0) {
					return;
				}

				RunnerRequest r = getTests(request.libsDir, benchmarks.get(benchName), base);
				if (r.items.size() == 0) {
					return;
				}

				Utils.deleteOld(outDirectory, true);
				Utils.deleteOld(logFile, false);
				tasksCount.incrementAndGet();
				service.submit(new Runnable() {

					@Override
					public void run() {
						totalRun.incrementAndGet();
						r.logFile = logFile;
						r.workingPath = base;
						r.outDirectory = outDirectory;
						if (innerExecute(r) != 0) {
							logError(request.baseDir, " ERROR " + base.toFile().getAbsolutePath());
						}
					}
				});
			} catch (Exception ex) {
				logError(request.baseDir, " ERROR " + ex.getMessage());
				ex.printStackTrace();
			}

		});

		service.shutdown();
		logError(request.baseDir, " waiting for finish... tasks: " + tasksCount.get() + " total: " + totalCount.get());
		long start = System.currentTimeMillis();
		while (!service.isTerminated() && (System.currentTimeMillis() - start) < TimeUnit.HOURS.toMillis(4)) {
			Thread.sleep(TimeUnit.MINUTES.toMillis(2));
			logError(request.baseDir, " waiting for tasks: " + tasksCount.get() + " run: " + totalRun.get());

		}
		service.shutdownNow();
		logError(request.baseDir, " finished... tasks: " + tasksCount.get() + " total: " + totalCount.get()
				+ " total run: " + totalRun.get());

	}

	public abstract int innerExecute(RunnerRequest request);

	public void logError(String base, String line) {
		try {
			final String name = this.getClass().getSimpleName();

			System.out.println(name + " " + line);
			FileUtils.write(new File(base, "errrors.txt"), name + " " + line + "\r\n", true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static RunnerRequest getTests(final String libsDir, Bench bench, final Path base) {
		List<CompileRequest> tests = new ArrayList<>();
		List<CompileRequest> other = new ArrayList<>();
		List<CompileRequest> post = new ArrayList<>();
		// RegTest
		final Path generatedTestsDirectory = Paths.get(base.toFile().getAbsolutePath(), "testcases");
		final Path compiledTestsDirectory = Paths.get(base.toFile().getAbsolutePath(), "bin");
		try {

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
						if (testName.toLowerCase().contains("_scaffolding")) {
							other.add(0, r);
							return;
						}
						if (testName.toLowerCase().endsWith("regtest")) {
							post.add(r);
							return;
						}

						tests.add(r);

					});
		} catch (Throwable e1) {
			e1.printStackTrace();
		}
		List<CompileRequest> main = new ArrayList<>();
		main.addAll(other);
		main.addAll(tests);
		main.addAll(post);

		RunnerRequest r = new RunnerRequest();
		r.bench = bench;
		r.paths = libsDir;
		r.testsPath = compiledTestsDirectory.toAbsolutePath().toFile().getAbsolutePath();
		r.items.addAll(main);
		return r;
	}

}
