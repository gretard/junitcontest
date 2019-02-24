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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;

public class AtgTool implements ITestingTool {

	private PrintStream logOut = null;

	private String prepareCommand;

	private String executeCommand;

	private List<File> extraCp = new ArrayList<File>();

	private String separatorChar;

	static final String homeDirName = "."; // current folder

	static final String baseTempDir = String.join(File.separator, homeDirName, "temp");

	static final String outDir = String.join(File.separator, baseTempDir, "testcases");

	static final String tempDir = String.join(File.separator, baseTempDir, "data");

	public AtgTool() throws IOException {
		super();
		List<String> lines = Files.readAllLines(Paths.get("./run.config"));
		this.separatorChar = lines.get(0).isEmpty() ? File.pathSeparator : lines.get(0).trim();
		this.prepareCommand = lines.get(1);
		this.executeCommand = lines.get(2);
		if (lines.size() > 3) {
			extraCp.addAll(Arrays.stream(lines.get(3).split(separatorChar)).map(File::new).collect(Collectors.toList()));
		}

	}

	private File binFile;
	private List<File> classPathList;
	private File src;

	public List<File> getExtraClassPath() {
		return this.extraCp;
	}

	public void initialize(File src, File bin, List<File> classPath) {

		this.src = src;
		this.binFile = bin;
		this.classPathList = classPath;
		initDir(baseTempDir);
		initDir(tempDir);
		initDir(outDir);
		initOut();

		String libs = String.join(separatorChar,
				classPathList.stream().map(x -> x.getAbsolutePath()).collect(Collectors.toList()));

		String classes = binFile.getAbsolutePath();

		if (this.prepareCommand == null || this.prepareCommand.isEmpty()) {
			log("Skipping init for tool");
			return;
		}

		String cmdToExecute = prepareCommand.replace("{classes}", classes).replace("{src}", src.getAbsolutePath()).replace("{libs}", libs)
				.replace("{outDir}", outDir).replace("{tempDir}", tempDir);
		log("Starting tool with: " + cmdToExecute);
		File homeDir = new File(homeDirName);
		int retVal = launch(homeDir, cmdToExecute);
		log("Execution finished with exit code " + retVal);
	}

	public void run(String cName, long timeBudget) {

		initDir(baseTempDir);
		initDir(tempDir);
		initDir(outDir);
		initOut();
		if (this.executeCommand == null || this.executeCommand.isEmpty()) {
			log("Skipping exec for tool");
			return;
		}
		log("Execution of tool my STARTED");
		log("user.home=" + homeDirName);

		String libs = String.join(separatorChar, classPathList.stream().map(File::getAbsolutePath).collect(Collectors.toList()));
		
		String classes = binFile.getAbsolutePath();
		int i = cName.lastIndexOf(".");
		String packageName = i > -1 ? cName.substring(0, i) : "";
		String shortClassName = i > -1 ? cName.substring(i+1) : "";

		String cmdToExecute = executeCommand.replace("{classes}", classes).replace("{shortClassName}", shortClassName).replace("{packName}", packageName).replace("{src}", src.getAbsolutePath()).replace("{libs}", libs)
				.replace("{classz}", cName).replace("{outDir}", outDir).replace("{tempDir}", tempDir).replace("{timeOut}", timeBudget+""); 
		log("Starting tool with: " + cmdToExecute);
		File homeDir = new File(homeDirName);
		int retVal = launch(homeDir, cmdToExecute);
		log("Execution finished with exit code " + retVal);
		log("Execution of tool my FINISHED");
	}

	private void initOut() {
		if (logOut == null) {
			final String logRandoopFileName = String.join(File.separator, baseTempDir, "log_my_tool.log");
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

		
		try {
			log("Spawning new process of command " + cmdString);
			int exitValue = executor.execute(CommandLine.parse(cmdString));
			log("Execution of subprocess finished with ret_code " + exitValue);
			return exitValue;
		} catch (IOException e) {
			log("An IOException occurred during the execution of Randoop");
			return -1;
		}
	}

}
