package sbst.pit.runner.metrics;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import sbst.pit.runner.BaseRunner;
import sbst.pit.runner.Utils;
import sbst.pit.runner.models.BaseRequest;
import sbst.pit.runner.models.Request;

public class TestMetricsCollector extends BaseRunner {
	MetricsCollector collector = new MetricsCollector();

	@Override
	public void execute(Request request) throws Throwable {
		String baseDir = request.baseDir;
		File metricsFile = Paths.get(baseDir, "testMetrics.csv").toFile();

		if (metricsFile.exists() && !request.force) {
			return;
		}
		Utils.deleteOld(Paths.get(baseDir, "testMetrics.csv"), false);
		Files.walk(Paths.get(baseDir), 9999)
				.filter(x -> x.toFile().getAbsolutePath().contains("temp" + File.separator + "bin" + File.separator))
				.forEach(path -> {
					if (!path.toFile().getAbsolutePath().endsWith(".class")) {
						return;
					}

					String filePath = path.toFile().getAbsolutePath().split("results")[1];
					String classPath = Paths.get(path.toFile().getAbsolutePath().split("temp.bin")[0], "temp", "bin")
							.toFile().getAbsolutePath();

					String[] temp = filePath.replace(File.separatorChar, '.').split("_", 4);
					String tool = temp[1];
					String budget = temp[2].split("\\.")[0];
					String benchmark = temp[2].split("\\.")[1];
					String classzName = temp[3].split("temp\\.bin")[1];
					classzName = classzName.substring(1, classzName.length() - 6);
					BaseRequest baseRequest = new BaseRequest();
					baseRequest.additionalInfoHeader = "benchmark\ttool\tbudget\t";
					baseRequest.additionalInfo = benchmark + "\t" + tool + "\t" + budget + "\t";
					baseRequest.classpath.add(classPath);
					baseRequest.classes.add(classzName);
					collector.collectMetrics(baseRequest, metricsFile);
				});

	}

}
