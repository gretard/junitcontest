package sbst.pit.runner;

import java.io.File;
import java.util.Arrays;

import sbst.pit.runner.compilation.CompilationRunner;
import sbst.pit.runner.junit.JacocoMetricsReporter;
import sbst.pit.runner.junit.JacocoXMLMetricsCollector;
import sbst.pit.runner.junit.TestsRunner;
import sbst.pit.runner.metrics.ProjectMetricsCollector;
import sbst.pit.runner.models.Request;
import sbst.pit.runner.pit.PitMetricsCollector;
import sbst.pit.runner.pit.PitRuner;
import sbst.pit.runner.testmetrics.TestMetrics2Collector;
import sbst.pit.runner.testmetrics.TestMetricsCollector0;
import sbst.pit.runner.testmetrics.TestMetricsRuner;

/**
 * Hello world!
 *
 */
public class App {
	public static enum TestModes {
		RUNNEW(1), RUNOLD(2), RUNALL(3);

		private final int mode;

		public int getMode() {
			return mode;
		}

		private TestModes(int mode) {
			this.mode = mode;

		}

		public boolean isSet(int mode) {
			return (this.mode & mode) == this.mode;
		}

	}

	public static enum Modes {
		COMPILE(1), MUTATIONS(2), COVERAGE(4), METRICS(8), DEFAULT(0), FORCE(15);

		private final int mode;

		public int getMode() {
			return mode;
		}

		private Modes(int mode) {
			this.mode = mode;

		}

		public boolean isSet(int mode) {
			return (this.mode & mode) == this.mode;
		}
	}

	public static void main(String[] args) throws Throwable {
		System.out.println("Usage: force baseDir libsDir projectsConfigFile runAllTests");
		final int mode = args.length > 0 ? Integer.parseInt(args[0]) : Modes.DEFAULT.getMode();
		System.out.println("Args were: " + Arrays.toString(args));
		final String baseDir = args.length > 1 ? args[1] : ".";

		final String libsDir = args.length > 2 ? args[2] : "/home/junit/libs";

		final String configFile = args.length > 3 ? args[3] : "/var/benchmarks/conf/benchmarks.list";
		final int testsMode = args.length > 4 ? Integer.parseInt(args[4]) : TestModes.RUNNEW.getMode();

		IExecutor[] runners = new IExecutor[] {

				new CompilationRunner(), new PitRuner(), new PitMetricsCollector(), new TestsRunner(),
				new JacocoMetricsReporter(), new JacocoXMLMetricsCollector(), new ProjectMetricsCollector(),
				new TestMetricsRuner(), new TestMetricsCollector0(), new TestMetrics2Collector(), new HtmlExecutor()

		};
		Request request = new Request();
		request.baseDir = new File(baseDir).getAbsolutePath();
		request.configFile = configFile;
		request.mode = mode;
		request.libsDir = libsDir;
		request.testsMode = testsMode;
		for (IExecutor runner : runners) {
			runner.execute(request);
		}

	}

}
