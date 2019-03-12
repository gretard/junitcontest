package sbst.pit.runner;

import sbst.pit.runner.models.Request;

public interface IExecutor {
	public void execute(Request request) throws Throwable;

}
