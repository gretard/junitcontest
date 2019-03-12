package sbst.pit.runner.compilation;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.exec.CommandLine;

import sbst.pit.runner.BaseRunner;
import sbst.pit.runner.Utils;
import sbst.pit.runner.models.CompileRequest;
import sbst.pit.runner.models.Request;

public class CompilationRunner extends BaseRunner {

	public CompilationRunner() {
		super("bin");
	}

	@Override
	public int innerExecute(Path current, Request baseRequest, Path log, CompileRequest request) {
		try {
			List<String> cps = new ArrayList<>(request.getAllCps());
			cps.add(request.testBinDir);
			CommandLine line = new CommandLine("javac").addArgument("-Xlint:-unchecked").addArgument("-verbose")
					.addArgument("-s").addArgument(request.src).addArgument("-cp")
					.addArgument(String.join(File.pathSeparator, cps)).addArgument("-d").addArgument(request.testBinDir)
					.addArgument(request.sourceFile);
			return Utils.launch(new File(request.workingDir), line, log);
		} catch (Throwable e) {
			log("was not able to compile file " + request.testName + " at " + request.workingDir);
			return -1;
		}
	}

}
