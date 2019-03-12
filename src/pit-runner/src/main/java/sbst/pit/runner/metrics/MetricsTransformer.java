package sbst.pit.runner.metrics;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.PatchingChain;
import soot.Unit;
import soot.jimple.IfStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.SwitchStmt;
import soot.tagkit.Tag;

public class MetricsTransformer extends BodyTransformer {
	public Map<String, Metrics> data = new HashMap<>();

	public Map<String, Metrics> getData() {
		return data;
	}

	@Override
	protected void internalTransform(Body body, String phaseName, Map<String, String> options) {
		final PatchingChain<Unit> units = body.getUnits();
		final Iterator<Unit> stmtIt = units.snapshotIterator();
		int complexity = 1;
		int instructions = 0;
		int returns = 0;
		while (stmtIt.hasNext()) {
			instructions++;
			Unit current = stmtIt.next();
			if (current instanceof IfStmt) {
				complexity++;
				continue;
			}
			if (current instanceof SwitchStmt) {
				complexity += (((SwitchStmt) current).getTargets()).size();
				continue;
			}
			if (current instanceof ReturnStmt) {
				returns++;
			}

		}
		String key = body.getMethod().getDeclaringClass().getName();
		Metrics metrics = data.getOrDefault(key, new Metrics());
		for (Tag tag : body.getMethod().getTags()) {
			if (tag.toString().contains("org/junit/Test")) {
				metrics.numberOfTests+=1;
				break;
			}
		}
		
		metrics.complexity += complexity;
		metrics.nor += returns;
		metrics.noc += body.getMethod().isConstructor() ? 1 : 0;
		metrics.nosm += body.getMethod().isStatic() ? 1 : 0;
		metrics.nopm += body.getMethod().isPublic() ? 1 : 0;
		metrics.instructions += instructions;
		metrics.noom += body.getMethod().getName().toLowerCase().contains("tostring") ? 1 : 0;
		metrics.noom += body.getMethod().getName().toLowerCase().contains("hascode") ? 1 : 0;
		metrics.noom += body.getMethod().getName().toLowerCase().contains("equals") ? 1 : 0;

		metrics.nom += 1;
		data.put(key, metrics);
	}

}
