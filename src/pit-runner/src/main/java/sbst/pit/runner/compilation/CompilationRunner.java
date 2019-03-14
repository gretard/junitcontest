package sbst.pit.runner.compilation;

import java.io.File;

import org.apache.commons.exec.CommandLine;

import sbst.pit.runner.BaseRunner;
import sbst.pit.runner.RunnerRequest;
import sbst.pit.runner.Utils;
import sbst.pit.runner.models.CompileRequest;

public class CompilationRunner extends BaseRunner {

	public CompilationRunner() {
		super("bin");
	}

	@Override
	public int innerExecute(RunnerRequest r) {
		int status = 0;

		for (CompileRequest request : r.items) {
			try {

				CommandLine line = new CommandLine("javac").addArgument("-Xlint:-unchecked").addArgument("-verbose")
						.addArgument("-s").addArgument(request.src).addArgument("-cp")
						.addArgument(String.join(File.pathSeparator, r.allPaths())).addArgument("-d")
						.addArgument(request.testBinDir).addArgument(request.sourceFile);
				System.out.println(line.toString());
				status += Utils.launch(r.workingPath.toFile(), line, r.logFile); 
			} catch (Throwable e) {
				e.printStackTrace();
				log("was not able to compile file " + r.workingPath);
				return -1;
			}
		}
		return status;
	}
}
