package sbst.pit.runner.junit;

import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sbst.pit.runner.BaseCollector;
import sbst.pit.runner.models.Request;

public class JacocoXMLMetricsCollector extends BaseCollector {

	public JacocoXMLMetricsCollector() {
		super("jacocoFull.csv", "project,benchmark,classz,method,run,tool,budget,counter,covered,missed\r\n");
	}

	boolean added = false;

	@Override
	protected void collect(Writer writer, Request request) throws Throwable {
		Files.walk(Paths.get(request.baseDir), 9999).filter(x -> x.getFileName().toString().contains("coverage.xml"))
				.forEach(x -> {
					try {
						if (!x.toFile().getAbsolutePath().contains("coverage-reports")) {
							return;
						}
						Path u = Paths.get(x.toString().split("temp")[0]);
						final String run = u.getFileName().toString();
						final String benchName = u.getFileName().toString().split("_")[0];
						final String project = extractProject(benchName);
						final String tool = u.getParent().toString().split("results_")[1].split("_")[0];
						final String budget = u.getParent().toString().split("results_")[1].split("_")[1];

						DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
						factory.setValidating(false);
						factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
						DocumentBuilder dBuilder = factory.newDocumentBuilder();

						Document doc = dBuilder.parse(x.toFile());

						NodeList counters = doc.getElementsByTagName("counter");
						for (int i = 0; i < counters.getLength(); i++) {
							Node counter = counters.item(i);
							if (counter.getParentNode() == null || counter.getParentNode().getParentNode() == null) {
								continue;
							}
							String type = counter.getAttributes().getNamedItem("type").getTextContent();
							String missed = counter.getAttributes().getNamedItem("missed").getTextContent();
							String covered = counter.getAttributes().getNamedItem("covered").getTextContent();
							String methodName = counter.getParentNode().getAttributes().getNamedItem("name")
									.getTextContent().replace('/', '.');

							String classzName = counter.getParentNode().getParentNode().getAttributes()
									.getNamedItem("name").getTextContent().replace('/', '.');

							StringBuilder sb = new StringBuilder();
							sb.append(project);
							sb.append(",");
							sb.append(benchName);
							sb.append(",");
							sb.append(classzName);
							sb.append(",");
							sb.append(methodName);
							sb.append(",");
							sb.append(run);
							sb.append(",");
							sb.append(tool);
							sb.append(",");
							sb.append(budget);
							sb.append(",");
							sb.append(type);
							sb.append(",");
							sb.append(covered);
							sb.append(",");
							sb.append(missed);
							sb.append("\r\n");

						}

					} catch (Exception e) {
						e.printStackTrace();
					}

				});

	}

}
