package net.sf.mzmine.modules.deconvolutedanalysis.massdetection;

import junit.framework.Assert;
import net.sf.mzmine.data.ChromatographicPeak;
import net.sf.mzmine.data.PeakList;
import net.sf.mzmine.data.PeakListRow;
import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.modules.batchmode.BatchModeModule;
import net.sf.mzmine.util.ExitCode;
import org.junit.Test;

import java.io.File;
import java.util.logging.Logger;

public class MassDetectionTest {
	private Logger logger = Logger.getLogger(this.getClass().getName());

	@Test
	public void testMassDetectionSet1() throws Exception {
		MZmineCore.initializeHeadless();

		for (RawDataFile dataFile : MZmineCore.getCurrentProject()
				.getDataFiles())
			MZmineCore.getCurrentProject().removeFile(dataFile);

		for (PeakList peakList : MZmineCore.getCurrentProject().getPeakLists())
			MZmineCore.getCurrentProject().removePeakList(peakList);

		File batchFile = new File(
				"src/test/resources/deconvolutedanalysis/massdetection_set1.xml");
		Assert.assertTrue(batchFile.exists());
		ExitCode exitCode = BatchModeModule.runBatch(batchFile);
		assert (exitCode == ExitCode.OK);

		assert (MZmineCore.getCurrentProject().getPeakLists().length > 0);

		PeakList peakList = MZmineCore.getCurrentProject().getPeakLists()[0];
		assert (peakList.getNumberOfRows() > 0);

		logger.info("Set #1 Mass Candidates: " + peakList.getNumberOfRows());

		for (PeakListRow row : peakList.getRows()) {
			assert (row.getNumberOfPeaks() > 0);
			assert (row.getRawDataFiles().length > 0);
			assert (row.getNumberOfPeaks() > 0);

			for (ChromatographicPeak p : row.getPeaks()) {
				MassCandidate m = (MassCandidate) p;
				assert (m.getRetentionIndex() > Integer.MIN_VALUE);
				assert (m.getAdductMatches().length > 0);
			}
		}
	}

	@Test
	public void testMassDetectionSet2() throws Exception {
		MZmineCore.initializeHeadless();

		File batchFile = new File(
				"src/test/resources/deconvolutedanalysis/massdetection_set2.xml");
		Assert.assertTrue(batchFile.exists());
		ExitCode exitCode = BatchModeModule.runBatch(batchFile);
		assert (exitCode == ExitCode.OK);

		assert (MZmineCore.getCurrentProject().getPeakLists().length > 0);

		PeakList peakList = MZmineCore.getCurrentProject().getPeakLists()[0];
		assert (peakList.getNumberOfRows() > 0);

		logger.info("Set #2 Mass Candidates: " + peakList.getNumberOfRows());

		for (PeakListRow row : peakList.getRows()) {
			assert (row.getNumberOfPeaks() > 0);
			assert (row.getRawDataFiles().length > 0);
			assert (row.getNumberOfPeaks() > 0);

			for (ChromatographicPeak p : row.getPeaks()) {
				MassCandidate m = (MassCandidate) p;
				assert (m.getRetentionIndex() > Integer.MIN_VALUE);
				assert (m.getAdductMatches().length > 0);
			}
		}
	}

	@Test
	public void testMassDetectionSet3() throws Exception {
		MZmineCore.initializeHeadless();

		File batchFile = new File(
				"src/test/resources/deconvolutedanalysis/massdetection_set3.xml");
		Assert.assertTrue(batchFile.exists());
		ExitCode exitCode = BatchModeModule.runBatch(batchFile);
		assert (exitCode == ExitCode.OK);

		assert (MZmineCore.getCurrentProject().getPeakLists().length > 0);

		PeakList peakList = MZmineCore.getCurrentProject().getPeakLists()[0];
		assert (peakList.getNumberOfRows() > 0);

		logger.info("Set #3 Mass Candidates: " + peakList.getNumberOfRows());

		for (PeakListRow row : peakList.getRows()) {
			assert (row.getNumberOfPeaks() > 0);
			assert (row.getRawDataFiles().length > 0);
			assert (row.getNumberOfPeaks() > 0);

			for (ChromatographicPeak p : row.getPeaks()) {
				MassCandidate m = (MassCandidate) p;
				assert (m.getRetentionIndex() > Integer.MIN_VALUE);
				assert (m.getAdductMatches().length > 0);
			}
		}
	}
}
