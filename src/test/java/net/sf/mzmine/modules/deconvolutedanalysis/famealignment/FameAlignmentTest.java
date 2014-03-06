package net.sf.mzmine.modules.deconvolutedanalysis.famealignment;

import junit.framework.Assert;
import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.data.Scan;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.modules.batchmode.BatchModeModule;
import net.sf.mzmine.util.ExitCode;
import net.sf.mzmine.util.Range;
import org.junit.Test;

import java.io.File;

public class FameAlignmentTest {
	@Test
	public void testAlignData() throws Exception {
		MZmineCore.initializeHeadless();

		File batchFile = new File(
				"src/test/resources/deconvolutedanalysis/famealignment.xml");
		Assert.assertTrue(batchFile.exists());
		ExitCode exitCode = BatchModeModule.runBatch(batchFile);
		assert (exitCode == ExitCode.OK);

		RawDataFile dataFile = MZmineCore.getCurrentProject().getDataFiles()[0];
		assert(dataFile != null);
	}
}
