package sbst.pit.runner.metrics;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import sbst.pit.runner.BaseCollector;
import sbst.pit.runner.Utils;
import sbst.pit.runner.models.Bench;
import sbst.pit.runner.models.Request;

public class ProjectMetricsCollector extends BaseCollector {
	public ProjectMetricsCollector() {
		super("metrics.csv");
	}

	MetricsCollector collector = new MetricsCollector();

	private void writeHeader(Writer writer) throws IOException {
		writer.write("project,benchmark,classz" + ",complexity" + ",instructions" + ",numberOfTests"
				+ ",numberOfConsturctors" + ",numberOfMethods" + ",publicMethods" + ",staticMethods"
				+ ",numberOfReturns" + ",overridenMethods" + "\r\n");
	}

	@Override
	protected void collect(Writer writer, Request request) throws Throwable {
		writeHeader(writer);
		final Map<String, Bench> benchmarks = Utils.getBenchmarks(request.configFile);
		benchmarks.forEach((k, v) -> {
			v.additionalInfo = extractProject(k)+","+k + ",";
			v.additionalInfoHeader = "project,benchmark,";
			collector.collectMetrics(v, writer);
		});

	}
}
