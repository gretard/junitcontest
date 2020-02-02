package sbst.pit.runner;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;

import sbst.pit.runner.models.Request;

public class HtmlExecutor implements IExecutor {

	Path[] paths = new Path[] { Paths.get("temp", "pit-reports", "index.html"),
			Paths.get("temp", "coverage-reports", "html", "index.html") };

	@Override
	public void execute(Request request) throws Throwable {

		File out = Paths.get(request.baseDir, "summary.html").toFile();
		out.delete();
		// FileUtils.deleteDirectory(outDir);
		FileUtils.write(out, "<html><body><table>", true);
		FileUtils.write(out, "<tr>", true);
		FileUtils.write(out, "<th>tool</th>", true);
		FileUtils.write(out, "<th>budget</th>", true);
		FileUtils.write(out, "<th>benchmark</th>", true);
		FileUtils.write(out, "<th>run</th>", true);
		FileUtils.write(out, "<th>compiled</th>", true);
		FileUtils.write(out, "<th>coverage</th>", true);
		FileUtils.write(out, "<th>mutations</th>", true);
		FileUtils.write(out, "</tr>", true);

		StringBuilder sb = new StringBuilder();
		Path base = Paths.get(request.baseDir);
		Files.walk(base).filter(p -> {
			File f = p.toFile();
			if (f.getName().contains("results")) {
				return true;
			}
			return false;
		}).distinct().forEach(p -> {
			File root = p.toFile();
			String[] info = root.getName().split("_");
			String tool = info[1];
			String budget = info[2];

			for (File f : p.toFile().listFiles()) {

				Path muts = Paths.get(f.getAbsolutePath(), "temp", "pit-reports", "index.html");

				Path coverage = Paths.get(f.getAbsolutePath(), "temp", "coverage-reports", "html", "index.html");

				Path compiled = Paths.get(f.getAbsolutePath(), "temp", "bin");
				String run = f.getName();
				String benchmark = f.getName().split("_")[0];
	
				sb.append("<tr>");

				sb.append("<td>");
				sb.append(tool);
				sb.append("</td>");

				sb.append("<td>");
				sb.append(budget);
				sb.append("</td>");

				sb.append("<td>");
				sb.append("<a href='" + out.toPath().getParent().relativize(f.toPath()) + "' target='_blank'>");
				sb.append(benchmark);
				sb.append("</a>");
				sb.append("</td>");

				sb.append("<td>");
				sb.append(run);
				sb.append("</td>");

				sb.append("<td>");
				sb.append("<a href='" + out.toPath().getParent().relativize(compiled).toString() + "' target='_blank'>");
				sb.append(compiled.toFile().listFiles().length > 0);
				sb.append("</a>");
				sb.append("</td>");

				sb.append("<td>");
				sb.append("<a href='" + out.toPath().getParent().relativize(coverage).toString() + "' target='_blank'>");
				sb.append(coverage.toFile().exists());
				sb.append("</a>");
				sb.append("</td>");

				sb.append("<td>");
				sb.append("<a href='" + out.toPath().getParent().relativize(muts).toString() + "' target='_blank'>");
				sb.append(muts.toFile().exists());
				sb.append("</a>");
				sb.append("</td>");

				sb.append("</tr>");

			}
		});
		FileUtils.write(out, sb.toString(), true);
		
		FileUtils.write(out, "</table></body></html>", true);
		System.out.println(out.getAbsolutePath());
	}

}
