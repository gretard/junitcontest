package sbst.pit.runner.pit;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.exec.CommandLine;

import sbst.pit.runner.BaseRunner;
import sbst.pit.runner.Utils;
import sbst.pit.runner.models.Bench;
import sbst.pit.runner.models.CompileRequest;
import sbst.pit.runner.models.Request;

public class PitRuner extends BaseRunner {

	public PitRuner() {
		super("pit-reports");
	}
	
	@Override
	public int innerExecute(Path current, Request request, Path logFile, CompileRequest item) {
		if (item.testName.contains("_scaffolding")) {
			return 0;
		}
		Path reportsDir= getOutputDir(current);
		
		return runPit(item, logFile, reportsDir);
		
	}

	public static int runPit(CompileRequest request, Path log, Path reportsDir) {
		try {
			CommandLine line = new CommandLine("java").addArgument("-cp")
					.addArgument(String.join(File.pathSeparator, request.getAllCpsForPit()));
			line.addArgument("org.pitest.mutationtest.commandline.MutationCoverageReport");
			line.addArgument("--verbose");

			line.addArgument("--timestampedReports");
			line.addArgument("false");
			line.addArgument("--exportLineCoverage");
			line.addArgument("true");
			line.addArgument("--mutators");
			line.addArgument("ALL");
			line.addArgument("--targetClasses");
			line.addArgument(String.join(",", request.bench.classes));
			line.addArgument("--targetTests");
			line.addArgument(request.testName);
			line.addArgument("--sourceDirs");
			line.addArgument(request.bench.src);
			line.addArgument("--reportDir");
			line.addArgument(reportsDir.toAbsolutePath().toFile().getAbsolutePath());
			line.addArgument("--outputFormats");
			line.addArgument("csv,html,xml");
			return Utils.launch(new File(request.workingDir), line, log);
		} catch (Throwable e) {
			log("was not able to run pit:" + request.testName + " at " + request.workingDir);
			return -1;
		}
	}

	
}
