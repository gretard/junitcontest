package sbst.pit.runner.junit;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.exec.CommandLine;

import sbst.pit.runner.BaseRunner;
import sbst.pit.runner.Utils;
import sbst.pit.runner.models.CompileRequest;
import sbst.pit.runner.models.Request;

public class TestsRunner extends BaseRunner {
	public TestsRunner() {
		super("coverage-raw");
	}

	@Override
	public int innerExecute(Path current, Request request, Path logFile, CompileRequest item) {
		if (item.testName.contains("_scaffolding")) {
			return 0;
		}

		final Path outFile = Paths.get(getOutputDir(current).toFile().getAbsolutePath(), item.testName + ".exec");

		return runJacoco(item, logFile, outFile);

	}

	public static int runJacoco(CompileRequest request, Path log, Path outFile) {
		try {
			CommandLine line = new CommandLine("java")
					.addArgument("-javaagent:/home/junit/libs/jacocoagent.jar=destfile="
							+ outFile.toFile().getAbsolutePath())
					.addArgument("-cp").addArgument(String.join(File.pathSeparator, request.getAllCpsForPit()));
			line.addArgument("org.junit.runner.JUnitCore");
			line.addArgument(request.testName);
			return Utils.launch(new File(request.workingDir), line, log);
		} catch (Throwable e) {
			log("was not able to run pit:" + request.testName + " at " + request.workingDir);
			return -1;
		}
	}

}
