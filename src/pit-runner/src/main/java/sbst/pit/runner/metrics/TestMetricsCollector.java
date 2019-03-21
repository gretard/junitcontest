package sbst.pit.runner.metrics;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import sbst.pit.runner.BaseCollector;
import sbst.pit.runner.models.BaseRequest;
import sbst.pit.runner.models.Request;

public class TestMetricsCollector extends BaseCollector {
	public TestMetricsCollector() {
		super("testMetrics.csv");
	}

	MetricsCollector collector = new MetricsCollector();

	private void writeHeader(Writer writer) throws IOException {
		writer.write("benchmark\ttool\tbudget\trun\tclassz" + "\tcomplexity" + "\tinstructions" + "\tnumberOfTests"
				+ "\tnumberOfConsturctors" + "\tnumberOfMethods" + "\tpublicMethods" + "\tstaticMethods"
				+ "\tnumberOfReturns" + "\toverridenMethods" + "\r\n");
	}

	@Override
	protected void collect(Writer writer, Request request) throws Throwable {
		writeHeader(writer);
		Files.walk(Paths.get(request.baseDir), 9999)
				.filter(x -> x.toFile().getAbsolutePath().contains("temp" + File.separator + "bin")).forEach(x -> {
					if (!x.toFile().getAbsolutePath().endsWith(".class")) {
						return;
					}
					Path u = Paths.get(x.toString().split("temp", 2)[0]);

					final String run = u.getFileName().toString();
					final String benchmark = u.getFileName().toString().split("_", 2)[0];
					final String tool = u.getParent().toString().split("results_")[1].split("_")[0];
					final String budget = u.getParent().toString().split("results_")[1].split("_")[1];
					final String classPath = Paths.get(u.toString(), "temp", "bin").toFile().getAbsolutePath();
					final String classzRaw = x.toString().split("temp", 2)[1].split("bin", 2)[1];
					final String classz = classzRaw.substring(1, classzRaw.length() - 6).replace(File.separatorChar,
							'.');

					BaseRequest baseRequest = new BaseRequest();
					baseRequest.additionalInfoHeader = "benchmark\ttool\tbudget\trun\t";
					baseRequest.additionalInfo = benchmark + "\t" + tool + "\t" + budget + "\t" + run + "\t";
					baseRequest.classpath.add(classPath);
					baseRequest.classes.add(classz);
					collector.collectMetrics(baseRequest, writer);
				});

	}

}
