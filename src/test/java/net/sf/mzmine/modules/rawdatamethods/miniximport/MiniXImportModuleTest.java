package net.sf.mzmine.modules.rawdatamethods.miniximport;

import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.taskcontrol.Task;
import net.sf.mzmine.util.ExitCode;
import org.junit.Test;

import java.util.Collection;
import java.util.Vector;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA. User: wohlgemuth Date: 12/12/13 Time: 5:04 PM
 */
public class MiniXImportModuleTest {
	@Test
	public void testRunModuleFailsWithMissingParameter() throws Exception {
		MZmineCore.initializeHeadless();

		MiniXImportModule module = new MiniXImportModule();

		Collection<Task> taskCollection = new Vector<Task>();
		MiniXImportParameters parameterSet = new MiniXImportParameters();

		ExitCode code = module.runModule(parameterSet, taskCollection);
		assertTrue(code.equals(ExitCode.ERROR));
	}

	@Test
	public void testRunModuleFailsWithMiniXServiceOffline() throws Exception {
		MZmineCore.initializeHeadless();

		MiniXImportModule module = new MiniXImportModule();

		Collection<Task> taskCollection = new Vector<Task>();
		MiniXImportParameters parameterSet = new MiniXImportParameters();
		parameterSet.getParameter(MiniXImportParameters.MINIX_ID_PARAMETER)
				.setValue("150994");
		parameterSet.getParameter(MiniXImportParameters.MINIX_URL_PARAMETER)
				.setValue(
						"http://www.thisurlshallnotexist.fiehnlab.ucdavis.edu");

		ExitCode code = module.runModule(parameterSet, taskCollection);
		assertTrue(code.equals(ExitCode.ERROR));
	}

}
