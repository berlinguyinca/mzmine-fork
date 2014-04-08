package net.sf.mzmine.modules.deconvolutedanalysis.spectrafilter;

import junit.framework.Assert;
import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.data.Scan;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.modules.batchmode.BatchModeModule;
import net.sf.mzmine.util.ExitCode;
import net.sf.mzmine.util.Range;
import org.junit.Test;

import java.io.File;

public class SpectraFilterTest {
	@Test
	public void testFilterData() throws Exception {
		MZmineCore.initializeHeadless();

		File batchFile = new File(
				"src/test/resources/deconvolutedanalysis/spectrafiltertest.xml");
		Assert.assertTrue(batchFile.exists());
		ExitCode exitCode = BatchModeModule.runBatch(batchFile);
		assert (exitCode == ExitCode.OK);

		// Confirm that a new file was made
		int filesCount = MZmineCore.getCurrentProject().getDataFiles().length;
		assert (filesCount > 0);

		// Get data file and scan
		RawDataFile dataFile = MZmineCore.getCurrentProject().getDataFiles()[0];
		assert (dataFile != null);

		Scan spectrum = dataFile.getScan(1);

		assert (dataFile.getName().contains("filtered"));
		assert (spectrum.getDataPointsByMass(new Range(146, 149)).length == 1);
		assert (spectrum.getDataPointsByMass(new Range(234, 246)).length == 1);
		assert (spectrum.getDataPointsByMass(new Range(249, 261)).length == 1);
	}
}
