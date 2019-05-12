package sbst.pit.runner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;

import sbst.pit.runner.models.Request;

public abstract class BaseCollector implements IExecutor {

	private final String fileName;
	private final String header;

	public BaseCollector(String fileName) {
		this(fileName, null);
	}

	public BaseCollector(String fileName, String header) {
		this.fileName = fileName;
		this.header = header;
	}

	@Override
	public void execute(Request request) throws Throwable {
		final Path outPath = Paths.get(request.baseDir, this.fileName);
		final File outFile = outPath.toFile();
		Utils.deleteOld(outPath, false);
		try (final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outFile))) {
			try {
				if (header != null) {
					bufferedWriter.append(this.header);
				}
				collect(bufferedWriter, request);
			} catch (Throwable e) {
				e.printStackTrace();
			}
			bufferedWriter.flush();
		}
	}

	protected abstract void collect(Writer writer, Request request) throws Throwable;
}
