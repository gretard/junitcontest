package sbst.pit.runner;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sbst.pit.runner.models.Bench;
import sbst.pit.runner.models.CompileRequest;

public class RunnerRequest {
	public Bench bench;

	public Path logFile;
	public Path current;
	public final List<CompileRequest> items = new ArrayList<CompileRequest>();

	public Path workingPath;

	public Set<String> paths = new HashSet<String>();

	public String testsPath;

	public Path outDirectory;

	public List<String> allCpPaths() {
		List<String> arr = new ArrayList<String>();
		arr.addAll(paths);
		arr.addAll(bench.classpath);
		return arr;
	}
	
	public List<String> allPaths() {
		List<String> arr = new ArrayList<String>();
		arr.addAll(paths);
		arr.addAll(bench.classpath);
		arr.add(testsPath);
		return arr;
	}

}
