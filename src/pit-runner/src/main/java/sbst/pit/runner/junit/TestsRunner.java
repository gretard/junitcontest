package sbst.pit.runner.junit;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.exec.CommandLine;

import sbst.pit.runner.BaseRunner;
import sbst.pit.runner.RunnerRequest;
import sbst.pit.runner.Utils;
import sbst.pit.runner.models.CompileRequest;

public class TestsRunner extends BaseRunner {

	public TestsRunner() {
		super("coverage-raw");
	}

	@Override
	public int innerExecute(RunnerRequest request) {

		List<String> names = new ArrayList<String>();
		for (CompileRequest item : request.items) {
			if (item.testName.contains("_scaffolding")) {
				continue;
			}

			names.add(item.testName);
		}
		if (names.isEmpty()) {
			return -1;
		}
		final Path outFile = Paths.get(request.outDirectory.toFile().getAbsolutePath(), "data.exec");
		return runJacoco(names, request, outFile);

	}

	public static int runJacoco(List<String> tests, RunnerRequest request, Path outFile) {
		try {
			CommandLine line = new CommandLine("java")
					.addArgument("-javaagent:/home/junit/libs/jacocoagent.jar=destfile="
							+ outFile.toFile().getAbsolutePath())
					.addArgument("-cp").addArgument(String.join(File.pathSeparator, request.allPaths()));
			line.addArgument("org.junit.runner.JUnitCore");
			for (String test : tests) {
				line.addArgument(test);
			}

			return Utils.launch(request.workingPath.toFile(), line, request.logFile);
		} catch (Throwable e) {
			log("was not able to runs tests:" + " at " + request.workingPath);
			return -1;
		}
	}

}
