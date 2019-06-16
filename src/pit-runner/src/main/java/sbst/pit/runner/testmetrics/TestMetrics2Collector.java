package sbst.pit.runner.testmetrics;

import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import sbst.pit.runner.BaseCollector;
import sbst.pit.runner.models.Request;

public class TestMetrics2Collector extends BaseCollector {

	public TestMetrics2Collector() {
		super("tests-runtime.csv", "project,benchmark,run,tool,budget,genreationOk,compilationOk,covOk,pitOk\r\n");
	}

	private void extract(final Writer bufferedWriter, final Stream<Path> stream) {
		stream.filter(x -> x.getFileName().toString().contains("log_my_tool.log")).forEach(x -> {
			try {
				Path parent = x.getParent();

				Path u = Paths.get(parent.toString().split("temp")[0]);
				final String run = u.getFileName().toString();
				final String benchName = u.getFileName().toString().split("_")[0];
				final String project = extractProject(benchName);
				final String tool = u.getParent().toString().split("results_")[1].split("_")[0];
				final String budget = u.getParent().toString().split("results_")[1].split("_")[1];

				final Path testcases = Paths.get(parent.toFile().getAbsolutePath(), "testcases");
				final Path pit = Paths.get(parent.toFile().getAbsolutePath(), "pit-reports");
				final Path cov = Paths.get(parent.toFile().getAbsolutePath(), "coverage-raw");
				final Path bin = Paths.get(parent.toFile().getAbsolutePath(), "bin");
				final boolean testCasesOk = Files.exists(testcases) && testcases.toFile().listFiles().length > 1;
				final boolean pitOk = Files.exists(pit) && pit.toFile().listFiles().length > 1;
				final boolean covOk = Files.exists(cov) && cov.toFile().listFiles().length > 0;
				final boolean compilationOk = Files.exists(bin) && bin.toFile().listFiles().length > 0;
				bufferedWriter.append(project + "," + benchName + "," + run + "," + tool + "," + budget + ","
						+ testCasesOk + "," + compilationOk + "," + covOk + "," + pitOk + "\r\n");

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
