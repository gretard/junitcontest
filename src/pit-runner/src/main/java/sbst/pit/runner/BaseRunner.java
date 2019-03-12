package sbst.pit.runner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import sbst.pit.runner.models.Bench;
import sbst.pit.runner.models.CompileRequest;
import sbst.pit.runner.models.Request;

public abstract class BaseRunner {
	public static void log(String s) {
		System.out.println(s);
	}

	public abstract void execute(Request request) throws Throwable;

	public void logError(Request request, String data) {
		try {
			FileUtils.write(new File(request.baseDir, "errrors.txt"), this.getClass().getSimpleName() + "\t" + data+"\r\n",
					true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static List<CompileRequest> getTests(final List<String> libsDir, Bench bench, final Path base) {
		List<CompileRequest> tests = new ArrayList<>();
		final Path generatedTestsDirectory = Paths.get(base.toFile().getAbsolutePath(), "testcases");
		final Path compiledTestsDirectory = Paths.get(base.toFile().getAbsolutePath(), "bin");
		try {
			log("Searching for tests at " + generatedTestsDirectory);
			Files.walk(generatedTestsDirectory).filter(x -> x.toFile().getAbsolutePath().contains(".java"))
					.forEach(t -> {

						String sourceFilePath = t.toString().split("temp")[1].substring(1);
						String testName = sourceFilePath.replace("testcases", "").substring(1).replace(".java", "")
								.replace(File.separatorChar, '.');
						if ("SBSTDummyForCoverageAndMutationCalculation".equals(testName)) {
							return;
						}
						CompileRequest r = new CompileRequest();
						r.bench = bench;
						r.sourceFile = sourceFilePath;
						r.workingDir = base.toFile().getAbsolutePath();
						r.testName = testName;
						r.testBinDir = compiledTestsDirectory.toFile().getAbsolutePath();
						r.extra.addAll(libsDir);
						if (testName.toLowerCase().contains("_scaffolding")) {
							tests.add(0, r);
						} else {
							tests.add(r);
						}

					});
		} catch (Throwable e1) {
			e1.printStackTrace();
		}
		return tests;
	}

}
