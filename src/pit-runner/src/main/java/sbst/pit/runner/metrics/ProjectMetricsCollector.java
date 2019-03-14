package sbst.pit.runner.metrics;

import java.io.File;
import java.nio.file.Paths;
import java.util.Map;

import sbst.pit.runner.IExecutor;
import sbst.pit.runner.Utils;
import sbst.pit.runner.App.Modes;
import sbst.pit.runner.models.Bench;
import sbst.pit.runner.models.Request;

public class ProjectMetricsCollector implements IExecutor {
	MetricsCollector collector = new MetricsCollector();

	@Override
	public void execute(Request request) throws Throwable {
		String configFile = request.configFile;
		File metricsFile = Paths.get(request.baseDir, "metrics.csv").toFile();

		if (metricsFile.exists() && !Modes.METRICS.isSet(request.mode)) {
			return;
		}
		
		final Map<String, Bench> benchmarks = Utils.getBenchmarks(configFile);
		Utils.deleteOld(Paths.get(request.baseDir, "metrics.csv"), false);
		benchmarks.forEach((k, v) -> {
			v.additionalInfo = k + "\t";
			v.additionalInfoHeader = "benchmark\t";
			collector.collectMetrics(v, metricsFile);
		});
	}
}
