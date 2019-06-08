package sbst.pit.runner.metrics;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import sbst.pit.runner.models.BaseRequest;
import soot.Pack;
import soot.PackManager;
import soot.PhaseOptions;
import soot.Scene;
import soot.SootClass;
import soot.Transform;
import soot.options.Options;

public class MetricsCollector {
	public void collectMetrics(BaseRequest request, Writer outFile) {
		soot.G.reset();
		final StringBuffer sb = new StringBuffer();
		sb.append(System.getProperty("java.class.path") + File.pathSeparator);
		sb.append(System.getProperty("java.home"));
		sb.append(File.separator);
		sb.append("lib");
		sb.append(File.separator);
		sb.append("rt.jar");
		sb.append(File.pathSeparator);
		sb.append(System.getProperty("java.home"));
		sb.append(File.separator);
		sb.append("lib");
		sb.append(File.separator);
		sb.append("jce.jar");
		for (final String x : request.classpath) {
			String t = x;
			if (t.endsWith("/*")) {
				t = t.replace("/*", "");
			}
			if (t.endsWith("\\*")) {
				t = t.replace("\\*", "");
			}
			sb.append(File.pathSeparator);
			sb.append(t);
		}

		Options.v().set_keep_line_number(true);
		Options.v().set_verbose(false);
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_output_dir(request.tempDir);
		Options.v().set_ignore_resolution_errors(true);
		final List<String> options = new ArrayList<String>();
		options.add("-cp");
		options.add(sb.toString());
		options.add("--java-version");
		options.add("8");
		options.add("-f");
		options.add("j");
		options.add("-p");
		options.add("jb");
		options.add("use-original-names:true");
		Scene.v().addBasicClass(java.lang.invoke.LambdaMetafactory.class.getName(), SootClass.SIGNATURES);
		options.addAll(request.classes);
		final Pack pack = PackManager.v().getPack("jtp");
		MetricsTransformer transformer = new MetricsTransformer();
		pack.add(new Transform("jtp.atg", transformer));
		PhaseOptions.v().setPhaseOption("jtp.atg", "on");
		final String[] arguments = options.toArray(new String[0]);

		soot.Main.main(arguments);
		
	/*	*/
		Map<String, Metrics> info = transformer.getData();
		request.classes.forEach(k -> {
			Metrics v = info.getOrDefault(k, new Metrics());
			try {
				outFile.write(request.additionalInfo + k + "," + v.complexity + "," + v.instructions + ","
								+ v.numberOfTests + "," + v.noc + "," + v.nom + "," + v.nopm + "," + v.nosm + ","
								+ v.nor + "," + v.noom + "\r\n"
						);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

	}
}
