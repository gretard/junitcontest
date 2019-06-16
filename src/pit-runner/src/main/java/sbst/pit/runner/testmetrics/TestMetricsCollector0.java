package sbst.pit.runner.testmetrics;

import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;

import sbst.pit.runner.BaseCollector;
import sbst.pit.runner.models.Request;

public class TestMetricsCollector0 extends BaseCollector {

	public TestMetricsCollector0() {
		super("tests-summary.csv", "project,benchmark,run,tool,budget,class,complexity,instructions,numberOfTests,"
				+ "numberOfConsturctors,numberOfMethods,publicMethods,staticMethods,numberOfReturns,overridenMethods\r\n");
	}

	private void extract(final Writer bufferedWriter, final Stream<Path> stream) {
		stream.filter(x -> x.getFileName().toString().contains("metrics.csv")).forEach(x -> {
			try {
				if (!x.toFile().getAbsolutePath().contains("test-metrics")) {
					return;
				}
				Path u = Paths.get(x.toString().split("temp")[0]);
				final String run = u.getFileName().toString();
				final String benchName = u.getFileName().toString().split("_")[0];
				final String project = extractProject(benchName);
				final String tool = u.getParent().toString().split("results_")[1].split("_")[0];
				final String budget = u.getParent().toString().split("results_")[1].split("_")[1];
				for (String line : FileUtils.readLines(x.toFile())) {
					bufferedWriter.append(project+","+benchName + "," + run + "," + tool + "," + budget + "," + line + "\r\n");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		});
	}

	@Override
	protected void collect(Writer writer, Request request) throws Throwable {
		try (final Stream<Path> stream = Files.walk(Paths.get(request.baseDir), 9999)) {
			extract(writer, stream);
		}

	}
}
