package sbst.pit.runner.pit;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.exec.CommandLine;

import sbst.pit.runner.BaseRunner;
import sbst.pit.runner.RunnerRequest;
import sbst.pit.runner.Utils;
import sbst.pit.runner.models.CompileRequest;

public class PitRuner extends BaseRunner {

	public PitRuner() {
		super("pit-reports");
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
			System.out.println("No tests found");
			return -1;
		}

		return runPit(names, request);

	}

	public static int runPit(List<String> tests, RunnerRequest request) {
		try {
			CommandLine line = new CommandLine("java").addArgument("-cp")
					.addArgument(String.join(File.pathSeparator, request.allPaths()));
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
			line.addArgument(String.join(",", tests));
			line.addArgument("--sourceDirs");
			line.addArgument(request.bench.src);
			line.addArgument("--reportDir");
			line.addArgument(request.outDirectory.toAbsolutePath().toFile().getAbsolutePath());
			line.addArgument("--outputFormats");
			line.addArgument("csv,html,xml");
			return Utils.launch(request.workingPath.toFile(), line, request.logFile);
		} catch (Throwable e) {
			e.printStackTrace();
			log("was not able to run pit: at " + request.workingPath);
			return -1;
		}
	}

}
