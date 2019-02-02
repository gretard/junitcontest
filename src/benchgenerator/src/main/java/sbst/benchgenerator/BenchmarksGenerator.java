package sbst.benchgenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Stream;

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
		final Path readDir = Paths.get(baseDir);

		log.info(() -> String.format("Starting searching %s dir", readDir.toFile().getAbsolutePath()));
		int count = 0;

		final List<Path> projects = new LinkedList<>();
		try (Stream<Path> pathsStream = Files.find(readDir, 1, (path, attributes) -> attributes.isDirectory())) {
			pathsStream.sorted().forEach(p -> projects.add(p));
		}

		final StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append(System.lineSeparator());
		for (final Path project : projects) {
			final File classesDir = Paths.get(project.toFile().getAbsolutePath(), "target", "classes").toFile();
			final File src = Paths.get(project.toFile().getAbsolutePath(), "src", "main", "java").toFile();
			if (!classesDir.exists() || !src.exists()) {
				log.info(() -> String.format("Skipping dir %s as no source or compiled classes were found",
						project.getFileName().toString()));
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
			Set<ClassFile> classesFiles = scanner.getClasses();
			classesFiles.stream().filter(c -> !(c.isAbstract() || c.isInterface() || c.getName().contains("$")
					|| AccessFlag.isPrivate(c.getAccessFlags()))).forEach(c -> {
						classes.add(c.getName());
					});

			sb.append(String.format("%s={%n", projectName));
			sb.append(String.format(" src=%s%n", src.getAbsolutePath()));
			sb.append(String.format(" bin=%s%n", classesDir.getAbsolutePath()));
			sb.append(String.format(" classes=(%s)%n", String.join(",", classes)));
			sb.append(String.format(" classpath=(%s)%n", String.join(",", classpath)));

			sb.append(String.format("}%n"));

			log.info(() -> String.format("Added benchmark %s with %s classes for benchmarking", projectName,
					classes.size()));
		}
		sb.append("}");

		try (final FileWriter out = new FileWriter(outFile)) {

			out.write(sb.toString());

		}
		log.info(() -> String.format("Finished. Results saved at %s", outFile));
	}
}
