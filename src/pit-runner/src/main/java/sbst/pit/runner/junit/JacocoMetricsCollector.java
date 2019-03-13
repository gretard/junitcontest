package sbst.pit.runner.junit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FileUtils;

import sbst.pit.runner.IExecutor;
import sbst.pit.runner.Utils;
import sbst.pit.runner.models.Request;

public class JacocoMetricsCollector implements IExecutor {

	@Override
	public void execute(Request request) throws Throwable {

		try {
			Path outPath = Paths.get(request.baseDir, "jacoco.csv");
			File outFile = outPath.toFile();
			if (outFile.exists() && !request.force) {
				return;
			}
			Utils.deleteOld(outPath, false);

			Files.walk(Paths.get(request.baseDir), 9999)
					.filter(x -> x.getFileName().toString().contains("coverage.csv")).forEach(x -> {
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
							if (outFile.length() == 0) {
								startLine = 0;
							}
							for (int i = startLine; i < lines.size(); i++) {
								final String line = lines.get(i);
								if (i == 0) {
									FileUtils.write(outFile, "benchmark,run,tool,budget," + line + "\r\n", true);
									continue;
								}
								FileUtils.write(outFile,
										benchName + "," + run + "," + tool + "," + budget + "," + line + "\r\n", true);
							}

						} catch (Exception e) {
							e.printStackTrace();
						}

					});
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

}
