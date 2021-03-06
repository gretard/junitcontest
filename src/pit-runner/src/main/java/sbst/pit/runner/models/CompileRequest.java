package sbst.pit.runner.models;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CompileRequest {
	public String reportsDir = "./reports";
	public List<String> extra = new ArrayList<>();

	public Bench bench;
	public String sourceFile;
	public String testBinDir = "./bin";
	public String src = "testcases";

	public String testName;
	public String workingDir;

	public String getReportsDir() {
		return reportsDir + File.separator + testName;
	}
	
	public List<String> getAllCps() {
		List<String> cps = new ArrayList<>();
		cps.addAll(extra);
		for (String x : bench.classpath) {
			String t = x;
			if (!new File(t).exists()) {
				continue;
			}
			if (x.endsWith("dependency")) {
				t = x + File.separator + "*";
			}
			cps.add(t);
		}

		return cps;
	}
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
	public List<String> getAllCpsForPit() {
		List<String> cps = new ArrayList<>();
		cps.addAll(extra);
		for (String x : bench.classpath) {

			String t = x;
			if (x.endsWith("dependency")) {
				t = x + File.separator + "*";
			}
			cps.add(t);
		}
		cps.add(testBinDir);
		return cps;
	}
}