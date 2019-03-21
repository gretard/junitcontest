package sbst.pit.runner.pit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;

import sbst.pit.runner.IExecutor;
import sbst.pit.runner.Utils;
import sbst.pit.runner.App.Modes;
import sbst.pit.runner.models.Request;

public class PitMetricsCollector implements IExecutor {

	@Override
	public void execute(Request request) throws Throwable {

		try {

			Path outPath = Paths.get(request.baseDir, "mutations-summary.csv");
			File outFile = outPath.toFile();

			
			Utils.deleteOld(outPath, false);

			Files.walk(Paths.get(request.baseDir), 9999)
					.filter(x -> x.getFileName().toString().contains("mutations.csv")).forEach(x -> {
						try {
							if (!x.toFile().getAbsolutePath().contains("pit-reports")) {
								return;
							}
							Path u = Paths.get(x.toString().split("temp")[0]);
							final String run = u.getFileName().toString();
							final String benchName = u.getFileName().toString().split("_")[0];
							final String tool = u.getParent().toString().split("results_")[1].split("_")[0];
							final String budget = u.getParent().toString().split("results_")[1].split("_")[1];
							for (String line : FileUtils.readLines(x.toFile())) {
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
