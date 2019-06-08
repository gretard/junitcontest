package sbst.pit.runner.testmetrics;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import sbst.pit.runner.App.Modes;
import sbst.pit.runner.BaseRunner;
import sbst.pit.runner.RunnerRequest;
import sbst.pit.runner.metrics.MetricsCollector;
import sbst.pit.runner.models.BaseRequest;

public class TestMetricsRuner extends BaseRunner {

	public TestMetricsRuner() {
		super("test-metrics", Modes.METRICS);
	}

	private final MetricsCollector collector = new MetricsCollector();

	@Override
	public int innerExecute(RunnerRequest request) {

		
		BaseRequest r = new BaseRequest();
		request.items.stream().map(x -> x.testName).forEach(c -> r.classes.add(c));
		r.classpath.addAll(request.allPaths());
		File outFile = new File(request.outDirectory.toFile(), "metrics.csv");
		try (FileWriter writer = new FileWriter(outFile)) {
			collector.collectMetrics(r, writer);
		} catch (Exception e) {
			return 1;
		}

		return 0;

	}

}
