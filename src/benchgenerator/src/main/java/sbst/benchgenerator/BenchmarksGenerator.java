package sbst.benchgenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;

import com.github.baev.ClasspathScanner;

import javassist.bytecode.AccessFlag;
import javassist.bytecode.ClassFile;

/**
 * Hello world!
 *
 */
public class BenchmarksGenerator {
	private static final Logger log = Logger.getLogger(BenchmarksGenerator.class.getName());

	public static void main(String[] args) throws IOException {
		final String baseDir = args.length > 0 ? args[0] : ".";
		final String outFile = args.length > 1 ? args[1] : "benchmarks.list";
		final int classesCount = args.length > 2 ? Integer.parseInt(args[2]) : 5;
		final boolean singleClassInBench = args.length > 3 ? Boolean.parseBoolean(args[3]) : false;
		final String reportFile = args.length > 4 ? args[4] : "./benchmark-stats.csv";
		
		final Path readDir = Paths.get(baseDir);

		log.info(() -> String.format("Starting searching %s dir", readDir.toFile().getAbsolutePath()));
		int count = 0;

		final List<Path> projects = new LinkedList<>();
		try (Stream<Path> pathsStream = Files.find(readDir, 1,
				(path, attributes) -> attributes.isDirectory() && !path.toString().endsWith("_skip"))) {
			pathsStream.sorted().forEach(p -> projects.add(p));
		}
		FileUtils.write(Paths.get(reportFile).toFile(), "project\ttotal\tclasses\tselected\r\n", false);
		
		final StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append(System.lineSeparator());
		for (final Path project : projects) {
			final File classesDir = Paths.get(project.toFile().getAbsolutePath(), "target", "classes").toFile();
			final File src = Paths.get(project.toFile().getAbsolutePath(), "src", "main", "java").toFile();
			if (!classesDir.exists() || !src.exists()) {
				log.info(() -> String.format("Skipping dir %s as no source or compiled classes were found at %s",
						project.getFileName().toString(), classesDir.getAbsolutePath()));
				continue;
			}
			final String projectName = String.format("%s-%s", project.getFileName().toString().toUpperCase(), ++count);

			final List<String> classes = new LinkedList<>();
			final List<String> classpath = new ArrayList<>();
			classpath.add(classesDir.getAbsolutePath());

			final File depsDir = Paths.get(project.toFile().getAbsolutePath(), "target", "dependency").toFile();
			if (depsDir.exists()) {
				classpath.add(depsDir.getAbsolutePath());
			}

			final ClasspathScanner scanner = new ClasspathScanner();
			scanner.scanFrom(classesDir.toPath());
			final Set<ClassFile> classesFiles = scanner.getClasses();
			classesFiles.stream().filter(c -> !(c.isAbstract() || c.isInterface() || c.getName().contains("$")
					|| AccessFlag.isPrivate(c.getAccessFlags()))).forEach(c -> {
						classes.add(c.getName());
					});
			final Set<String> selectedClasses = new TreeSet<>();
			if (count > 0) {
				Collections.shuffle(classes);
				selectedClasses.addAll(classes.subList(0, Math.min(classes.size(), classesCount)));
			} else {
				selectedClasses.addAll(classes);
			}

			FileUtils.write(Paths.get(reportFile).toFile(), projectName + "\t" + classesFiles.size() + "\t"
					+ classes.size() + "\t" + selectedClasses.size() + "\r\n", true);
			
			log.info(() -> String.format("Adding  benchmark %s with %s classes for benchmarking", projectName,
					selectedClasses.size()));
			if (singleClassInBench) {
				int i = 0;
				for (String selectedClass : selectedClasses) {
					sb.append(String.format("%s-%s={%n", projectName, i++));
					sb.append(String.format(" src=%s%n", src.getAbsolutePath()));
					sb.append(String.format(" bin=%s%n", classesDir.getAbsolutePath()));
					sb.append(String.format(" classes=(%s)%n", String.join(",", selectedClass)));
					sb.append(String.format(" classpath=(%s)%n", String.join(",", classpath)));
					sb.append(String.format("}%n"));
				}
				continue;

			}
			sb.append(String.format("%s={%n", projectName));
			sb.append(String.format(" src=%s%n", src.getAbsolutePath()));
			sb.append(String.format(" bin=%s%n", classesDir.getAbsolutePath()));
			sb.append(String.format(" classes=(%s)%n", String.join(",", selectedClasses)));
			sb.append(String.format(" classpath=(%s)%n", String.join(",", classpath)));
			sb.append(String.format("}%n"));

		
		}
		sb.append("}");

		try (final FileWriter out = new FileWriter(outFile)) {

			out.write(sb.toString());

		}
		log.info(() -> String.format("Finished. Results saved at %s", outFile));
	}
}
