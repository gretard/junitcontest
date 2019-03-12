package sbst.pit.runner.pit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;

import sbst.pit.runner.BaseRunner;
import sbst.pit.runner.Utils;
import sbst.pit.runner.models.Request;

public class PitMetricsCollector extends BaseRunner {

	@Override
	public void execute(Request request) throws Throwable {

		try {
			Path outPath = Paths.get(request.baseDir, "summary.csv");
			Utils.deleteOld(outPath, false);
			File outFile = outPath.toFile();
			log("Finding reports at " + request.baseDir);
			Files.walk(Paths.get(request.baseDir), 9999)
					.filter(x -> x.getFileName().toString().contains("mutations.csv")).forEach(x -> {
						try {
							Path u = Paths.get(x.toString().split("temp")[0]);
							final String benchName = u.getFileName().toString().split("_")[0];
							final String tool = u.getParent().toString().split("results_")[1].split("_")[0];
							final String budget = u.getParent().toString().split("results_")[1].split("_")[1];
							for (String line : FileUtils.readLines(x.toFile())) {
								FileUtils.write(outFile, benchName + "," + tool + "," + budget + "," + line + "\r\n",
										true);
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
