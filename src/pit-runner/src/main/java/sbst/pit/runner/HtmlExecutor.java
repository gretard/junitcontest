package sbst.pit.runner;

import java.io.File;
import java.io.IOException;
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
		File outDir = Paths.get(request.baseDir, "summaryDirs").toFile();

		File out = Paths.get(request.baseDir, "summary.html").toFile();
		out.delete();
		FileUtils.deleteDirectory(outDir);
		FileUtils.write(out, "<html><body><ul>", true);
		Path base = Paths.get(request.baseDir);
		Files.walk(base).filter(p -> {

			return p.endsWith(Paths.get("temp", "pit-reports", "index.html"))

					|| p.endsWith(Paths.get("temp", "coverage-reports", "html", "index.html"));
		}).forEach(p -> {
			try {
				Path t = base.relativize(p);
				File copyTo = Paths.get(outDir.getAbsolutePath(), t.getParent().toString()).toFile();
				File copyFrom = p.getParent().toFile();
				FileUtils.copyDirectory(copyFrom, copyTo);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		Files.walk(outDir.toPath()).filter(p -> {
			return p.endsWith(Paths.get("temp", "pit-reports", "index.html"))
					|| p.endsWith(Paths.get("temp", "coverage-reports", "html", "index.html"));
		}).forEach(p -> {
			Path t = outDir.toPath().relativize(p);
			try {
				StringBuilder sb = new StringBuilder();
				sb.append(String.format("<li><a href='%s'>", t.toString()));
				sb.append(t.toString());
				sb.append("</a></li>\r\n");
				FileUtils.write(out, sb.toString(), true);
			} catch (IOException e) {
				e.printStackTrace();
			}

		});
		FileUtils.write(out, "</ul></body></html>", true);

	}

}
