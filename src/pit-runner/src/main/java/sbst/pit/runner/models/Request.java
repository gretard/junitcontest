package sbst.pit.runner.models;

import sbst.pit.runner.App.Modes;

public class Request {
	public String configFile = "/var/benchmarks/conf/benchmarks.list";
	public int mode = Modes.DEFAULT.getMode();
	public String baseDir = ".";
	public String libsDir = "./libs";
	public int testsMode;

}
