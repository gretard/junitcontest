package sbst.pit.runner;

import java.io.File;
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
	
	public List<String> getJacocoAllCps() {
		List<String> cps = new ArrayList<>();
		for (String x : bench.classpath) {
			String t = x;
			if (!new File(t).exists() || x.endsWith("dependency")) {
				continue;
			}
		
			cps.add(t);
		}

		return cps;
	}
	
	public List<String> allPaths() {
		List<String> arr = new ArrayList<String>();
		arr.addAll(paths);
		for (String p : bench.classpath) {
			if (p.endsWith("dependency")) {
				String t  = p + File.separator + "*";
				arr.add(t);
				continue;
			}
			arr.add(p);
		}
		
		arr.add(testsPath);
		return arr;
	}

}