package sbst.pit.runner;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sbst.pit.runner.compilation.CompilationRunner;
import sbst.pit.runner.junit.JacocoMetricsCollector;
import sbst.pit.runner.junit.JacocoMetricsReporter;
import sbst.pit.runner.junit.TestsRunner;
import sbst.pit.runner.metrics.ProjectMetricsCollector;
import sbst.pit.runner.metrics.TestMetricsCollector;
import sbst.pit.runner.models.Request;
import sbst.pit.runner.pit.PitMetricsCollector;
import sbst.pit.runner.pit.PitRuner;

/**
 * Hello world!
 *
 */
public class App {
	public static enum Modes {
		METRICS(1), MUTATIONS(2), FORCE(8), DEFAULT(4), ALL(16);

		private final int mode;

		public int getMode() {
			return mode;
		}

		private Modes(int mode) {
			this.mode = mode;

		}
	}

	public static void main(String[] args) throws Throwable {
		System.out.println("Usage: force baseDir libsDir projectsConfigFile");
		final Modes mode = args.length > 0 ? Modes.valueOf(args[0].toUpperCase()) : Modes.DEFAULT;

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

		IExecutor[] runners = new IExecutor[] { new CompilationRunner(), new PitRuner(), new TestsRunner(),
				new JacocoMetricsReporter(), new PitMetricsCollector(), new TestMetricsCollector(),
				new ProjectMetricsCollector(), new JacocoMetricsCollector() };
		
		Request request = new Request();
		request.baseDir = new File(baseDir).getAbsolutePath();
		request.configFile = configFile;
		request.force = mode == Modes.FORCE;

		for (IExecutor runner : runners) {
			runner.execute(request);
		}

	}

}
