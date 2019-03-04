package sbst.pit.runner;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.plist.PropertyListConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;

public class Utils {
	public static Map<String, Bench> getBenchmarks(String configFile) throws ConfigurationException {
		PropertyListConfiguration benchmarkList = new PropertyListConfiguration();
		benchmarkList.load(new File(configFile));
		Map<String, Bench> exes = new HashMap<>();

		for (ConfigurationNode child : benchmarkList.getRoot().getChildren()) {
			Bench b = new Bench();
			b.name = child.getName();
			b.src = (String) child.getChildren("src").get(0).getValue();
			b.classpath.addAll((List<String>) child.getChildren("classpath").get(0).getValue());
			b.classes.addAll((List<String>) child.getChildren("classes").get(0).getValue());
			exes.put(b.name, b);

		}
		return exes;
	}
}
