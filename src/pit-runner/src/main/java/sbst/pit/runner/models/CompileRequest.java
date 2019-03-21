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
	
}