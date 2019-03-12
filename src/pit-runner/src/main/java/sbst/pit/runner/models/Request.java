package sbst.pit.runner.models;

import java.util.ArrayList;
import java.util.List;

public class Request {
	public String configFile = "/var/benchmarks/conf/benchmarks.list";
	public boolean force = false;
	public String baseDir = ".";
	public final List<String> libsDir = new ArrayList<>();

	public Request() {
		libsDir.add("/home/junit/libs/*");
	}

}
