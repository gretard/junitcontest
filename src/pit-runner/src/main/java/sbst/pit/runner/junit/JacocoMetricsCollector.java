package sbst.pit.runner.junit;

import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FileUtils;

import sbst.pit.runner.BaseCollector;
import sbst.pit.runner.models.Request;

public class JacocoMetricsCollector extends BaseCollector {

	public JacocoMetricsCollector() {
		super("jacoco.csv");
	}

	boolean added = false;

	@Override
	protected void collect(Writer writer, Request request) throws Throwable {
		Files.walk(Paths.get(request.baseDir), 9999).filter(x -> x.getFileName().toString().contains("coverage.csv"))
				.forEach(x -> {
					try {
						if (!x.toFile().getAbsolutePath().contains("coverage-reports")) {
							return;
						}
						Path u = Paths.get(x.toString().split("temp")[0]);
						final String run = u.getFileName().toString();
						final String benchName = u.getFileName().toString().split("_")[0];
						final String tool = u.getParent().toString().split("results_")[1].split("_")[0];
						final String budget = u.getParent().toString().split("results_")[1].split("_")[1];
						final List<String> lines = FileUtils.readLines(x.toFile());
						int startLine = 1;
						if (!added) {
							startLine = 0;
							added = true;
						}

						for (int i = startLine; i < lines.size(); i++) {
							final String line = lines.get(i);
							if (i == 0) {
								writer.write("benchmark,run,tool,budget," + line + "\r\n");
								continue;
							}
							writer.write(benchName + "," + run + "," + tool + "," + budget + "," + line + "\r\n");
						}

					} catch (Exception e) {
						e.printStackTrace();
					}

				});

	}

}
