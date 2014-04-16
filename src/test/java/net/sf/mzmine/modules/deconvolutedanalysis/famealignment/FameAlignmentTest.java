package net.sf.mzmine.modules.deconvolutedanalysis.famealignment;

import com.google.common.collect.Ordering;
import junit.framework.Assert;
import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.modules.batchmode.BatchModeModule;
import net.sf.mzmine.modules.deconvolutedanalysis.CorrectedSpectrum;
import net.sf.mzmine.util.ExitCode;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FameAlignmentTest {
	@Test
	public void testAlignData() throws Exception {
		MZmineCore.initializeHeadless();

		for (RawDataFile dataFile : MZmineCore.getCurrentProject()
				.getDataFiles())
			MZmineCore.getCurrentProject().removeFile(dataFile);

		File batchFile = new File(
				"src/test/resources/deconvolutedanalysis/famealignment.xml");
		Assert.assertTrue(batchFile.exists());
		ExitCode exitCode = BatchModeModule.runBatch(batchFile);
		assert (exitCode == ExitCode.OK);

		// Check that new files have been created
		assert (MZmineCore.getCurrentProject().getDataFiles().length > 0);

		for (RawDataFile dataFile : MZmineCore.getCurrentProject()
				.getDataFiles()) {
			assert (dataFile != null);

			Map<String, FameCorrection> results = null;

			// Check that each spectrum is retention corrected
			List<Integer> retentionIndices = new ArrayList<Integer>();

			for (int scanNumber : dataFile.getScanNumbers(1)) {
				CorrectedSpectrum s = (CorrectedSpectrum) dataFile
						.getScan(scanNumber);
				assert s.isRetentionCorrected();
				assert (s.getRetentionCorrection() != null);

				results = s.getRetentionCorrectionResults();
				retentionIndices.add(s.getRetentionIndex());
			}

			// Check that the retention indices are increasing
			assert Ordering.natural().isOrdered(retentionIndices);

			// Check results
			assert (results != null);

			List<Double> retentionTimes = new ArrayList<Double>();
			for (String name : results.keySet())
				retentionTimes.add(results.get(name).getRetentionTime());

			assert Ordering.natural().isOrdered(retentionTimes);

			for (int i = 0; i < results.size() - 1; i++)
				assert (retentionTimes.get(i + 1) - retentionTimes.get(i) > 1);
		}
	}
}
