/**
  * Copyright (c) 2017 Universitat Politècnica de València (UPV)

  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

  * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

  * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

  * 3. Neither the name of the UPV nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  **/
package sbst.runtool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;

public class AtgTool implements ITestingTool {

	private PrintStream logOut = null;

	static final String homeDirName = "."; // current folder

	static final String appJar = String.join(File.separator, homeDirName, "lib", "main.jar");

	static final String tempDir = String.join(File.separator, homeDirName, "temp");

	static final String outDir = String.join(File.separator, tempDir, "testcases");

	static final String tempDataDir = String.join(File.separator, tempDir, "data");

	public AtgTool() {
		super();
	}

	private File binFile;
	private List<File> classPathList;
	private File src;

	public List<File> getExtraClassPath() {
		return new ArrayList<File>();
	}

	public void initialize(File src, File bin, List<File> classPath) {
		this.src = src;
		this.binFile = bin;
		this.classPathList = classPath;
		initDir(tempDir);
		initDir(tempDataDir);
		initDir(outDir);
		initOut();

		String libs = String.join(",",
				classPathList.stream().map(x -> x.getAbsolutePath()).collect(Collectors.toList()));

		String classes = binFile.getAbsolutePath();

		StringBuffer cmdLine = new StringBuffer();

		String javaCommand = buildJavaCommand();
		cmdLine.append(String.format("%s -Xmx1g -jar %s ", javaCommand, appJar));
		cmdLine.append(String.format(" -classesDir %s ", classes));
		cmdLine.append(String.format(" -libs %s ", libs));
		cmdLine.append(String.format("-mode  INSTRUMENT "));
		cmdLine.append(String.format(" -resultsDir %s ", outDir));
		cmdLine.append(String.format(" -baseDir %s ", tempDataDir));

		String cmdToExecute = cmdLine.toString();
		log("Starting tool with: " + cmdToExecute);
		File homeDir = new File(homeDirName);
		int retVal = launch(homeDir, cmdToExecute);
		log("Execution finished with exit code " + retVal);
	}

	public void run(String cName, long timeBudget) {

		initDir(tempDir);
		initDir(tempDataDir);
		initDir(outDir);
		initOut();

		log("Execution of tool my STARTED");
		log("user.home=" + homeDirName);

		String libs = String.join(",",
				classPathList.stream().map(File::getAbsolutePath).collect(Collectors.toList()));

		String classes = binFile.getAbsolutePath();

		StringBuilder cmdLine = new StringBuilder();

		String javaCommand = buildJavaCommand();
		cmdLine.append(String.format("%s -Xmx1g -jar %s ", javaCommand, appJar));
		cmdLine.append(String.format(" -classesDir %s ", classes));
		cmdLine.append(String.format(" -c %s ", cName));
		cmdLine.append(String.format(" -libs %s ", libs));
		cmdLine.append(String.format("-mode GA -timeOutGlobal %s ", (int) (1000 * (timeBudget))));
		cmdLine.append(String.format(" -resultsDir %s ", outDir));
		cmdLine.append(String.format(" -baseDir %s ", tempDataDir));

		String cmdToExecute = cmdLine.toString();
		log("Starting tool with: " + cmdToExecute);
		File homeDir = new File(homeDirName);
		int retVal = launch(homeDir, cmdToExecute);
		log("Execution finished with exit code " + retVal);
		log("Execution of tool my FINISHED");
	}

	private void initOut() {
		if (logOut == null) {
			final String logRandoopFileName = String.join(File.separator, tempDir, "log_my_tool.log");
			PrintStream outStream;
			try {
				outStream = new PrintStream(new FileOutputStream(logRandoopFileName, true));
				this.logOut = outStream;
			} catch (FileNotFoundException e) {
				throw new RuntimeException(
						"FileNotFoundException signaled while creating output stream for  " + logRandoopFileName);
			}
		}
	}

	private String initDir(final String junitOutputDirName) {
		File junitOutputDir = new File(junitOutputDirName);
		if (!junitOutputDir.exists()) {
			log("Creating directory " + junitOutputDir);
			junitOutputDir.mkdirs();
		}
		return junitOutputDirName;
	}

	private String buildJavaCommand() {
		return "java";
	}

	private void log(String msg) {
		if (logOut != null) {
			logOut.println(msg);
		}
	}

	private int launch(File baseDir, String cmdString) {
		DefaultExecutor executor = new DefaultExecutor();

		PumpStreamHandler streamHandler;
		streamHandler = new PumpStreamHandler(logOut, logOut, null);
		executor.setStreamHandler(streamHandler);
		if (baseDir != null) {
			executor.setWorkingDirectory(baseDir);
		}

		int exitValue;
		try {
			log("Spawning new process of command " + cmdString);
			exitValue = executor.execute(CommandLine.parse(cmdString));
			log("Execution of subprocess finished with ret_code " + exitValue);
			return exitValue;
		} catch (IOException e) {
			log("An IOException occurred during the execution of Randoop");
			return -1;
		}
	}

}
