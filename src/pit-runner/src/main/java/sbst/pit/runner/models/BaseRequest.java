package sbst.pit.runner.models;

import java.util.ArrayList;
import java.util.List;

public class BaseRequest {
	public String additionalInfo = "";
	public List<String> classes = new ArrayList<>();
	public List<String> classpath = new ArrayList<>();
	public String tempDir = "./temp";
	public String additionalInfoHeader = "";
}
