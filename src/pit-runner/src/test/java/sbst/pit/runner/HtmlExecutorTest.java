package sbst.pit.runner;

import org.junit.Ignore;
import org.junit.Test;

import sbst.pit.runner.models.Request;

public class HtmlExecutorTest {

	@Test
	@Ignore
	public void testExecute() throws Throwable {
		HtmlExecutor sut = new HtmlExecutor();
		Request request = new Request();
		request.baseDir = "./junitcontest/tools/";
		sut.execute(request);
	}

}
