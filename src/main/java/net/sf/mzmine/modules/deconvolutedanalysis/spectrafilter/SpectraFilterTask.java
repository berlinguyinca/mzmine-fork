package net.sf.mzmine.modules.deconvolutedanalysis.spectrafilter;

import com.google.common.collect.Lists;
import net.sf.mzmine.data.DataPoint;
import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.data.Scan;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.modules.deconvolutedanalysis.CorrectedSpectrum;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.project.MZmineProject;
import net.sf.mzmine.project.impl.RawDataFileImpl;
import net.sf.mzmine.taskcontrol.AbstractTask;
import net.sf.mzmine.taskcontrol.TaskStatus;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SpectraFilterTask extends AbstractTask {
	/** Logger */
	private final Logger logger = Logger.getLogger(getClass().getName());

	/** Double value tolerance */
	public static final double EPSILON = 1.0e-14;

	/*
	 * Task variables
	 */

	/** Original data file to be processed */
	private final RawDataFile dataFile;

	/** Filtered data file */
	private RawDataFile filteredDataFile = null;

	/** Number of processed scans */
	private int processedScans = 0;

	/** Number of scans to process */
	private int totalScans;

	/*
	 * User parameters
	 */

	/** User parameter for the C13 Isotope Cut */
	private double c13IsotopeCut;

	/** User parameter for the base peak intensity threshold */
	private int basePeakThreshold;

	/** User parameter for the unique mass intensity threshold */
	private int uniqueMassThreshold;

	/** User parameter for the intensity threshold cut */
	private int intensityThreshold;

	/** User parameter for intensity percentage threshold */
	private double intensityPercentageThreshold;

	/** Filename suffix */
	private String suffix;

	/** Remove original data file */
	private boolean removeOriginal;

	/**
	 * Default constructor, initializes the task with the data file to process
	 * and the set of user-defined parameters.
	 * 
	 * @param dataFile
	 *            data file to filter
	 * @param parameters
	 *            user-defined parameter set
	 */
	public SpectraFilterTask(final RawDataFile dataFile,
			final ParameterSet parameters) {

		// Set original data file
		this.dataFile = dataFile;

		// Get user parameters
		c13IsotopeCut = parameters.getParameter(
				SpectraFilterParameters.C13_ISOTOPE_CUT).getValue();
		basePeakThreshold = parameters.getParameter(
				SpectraFilterParameters.BASE_PEAK_THRESHOLD).getValue();
		uniqueMassThreshold = parameters.getParameter(
				SpectraFilterParameters.UNIQUE_MASS_THRESHOLD).getValue();
		intensityThreshold = parameters.getParameter(
				SpectraFilterParameters.INTENSITY_THRESHOLD).getValue();
		intensityPercentageThreshold = parameters.getParameter(
				SpectraFilterParameters.INTENSITY_PERCENTAGE_THRESHOLD)
				.getValue();
		suffix = parameters.getParameter(SpectraFilterParameters.SUFFIX)
				.getValue();
		removeOriginal = parameters.getParameter(
				SpectraFilterParameters.REMOVE_ORIGINAL).getValue();
	}

	@Override
	public String getTaskDescription() {
		return "Filtering spectra in " + dataFile;
	}

	@Override
	public double getFinishedPercentage() {
		return totalScans == 0 ? 0.0 : (double) processedScans
				/ (double) totalScans;
	}

	@Override
	public Object[] getCreatedObjects() {
		return new Object[]{filteredDataFile};
	}

	/**
	 *
	 */
	public void run() {
		// Update the status of this task
		setStatus(TaskStatus.PROCESSING);
		logger.info("Started spectrum filter on " + dataFile);

		// Set total number of scans to process
		totalScans = dataFile.getNumOfScans();

		try {
			// Create a new file
			final RawDataFileImpl rawDataFileWriter = (RawDataFileImpl) MZmineCore
					.createNewFile(dataFile.getName() + ' ' + suffix);

			// Process each deconvoluted spectrum
			for (int scanNumber : dataFile.getScanNumbers(1)) {
				// Canceled?
				if (isCanceled())
					return;

				// Duplicate current spectrum
				Scan spectrum = dataFile.getScan(scanNumber);

				// Exclude the entire spectrum if its base peak intensity is
				// less than the given threshold
				if (spectrum.getBasePeak().getIntensity() < basePeakThreshold)
					continue;

				// Exclude the entire spectrum if its unique mass intensity is
				// less than the given threshold
				if (spectrum instanceof CorrectedSpectrum) {
					CorrectedSpectrum s = ((CorrectedSpectrum) spectrum);

					if (s.getUniqueMass() != null
							&& s.getUniqueMass().getIntensity() < uniqueMassThreshold)
						continue;
				}

				// Get the data points from the spectrum and sort by m/z
				List<DataPoint> dataPoints = Lists.newArrayList(spectrum
						.getDataPoints());
				Collections.sort(dataPoints, new Comparator<DataPoint>() {
					@Override
					public int compare(DataPoint a, DataPoint b) {
						return a.getMZ() < b.getMZ() ? -1 : a.getMZ() > b
								.getMZ() ? 1 : 0;
					}
				});

				// Create a list for the filtered points
				List<DataPoint> filteredDataPoints = new ArrayList<DataPoint>();

				// Filter the data points given pre-defined conditions
				for (int i = dataPoints.size() - 1; i >= 0; i--) {
					// Step #1: Remove C13 Isotope ions
					if (i > 0
							&& dataPoints.get(i).getMZ()
									- dataPoints.get(i - 1).getMZ() < 1 + EPSILON
							&& dataPoints.get(i - 1).getIntensity() >= (1 + c13IsotopeCut)
									* dataPoints.get(i).getIntensity())
						continue;

					// Step #2: Remove all ions < 100 counts
					else if (dataPoints.get(i).getIntensity() < intensityThreshold)
						continue;

					// Step #3: Remove all ions < 1% of base peak
					else if (dataPoints.get(i).getIntensity() < intensityPercentageThreshold
							* spectrum.getBasePeak().getIntensity())
						continue;

					// If the data point passes all filters, keep it.
					else
						filteredDataPoints.add(0, dataPoints.get(i));
				}

				// Add scan to new data file
				int storageID = rawDataFileWriter
						.storeDataPoints(filteredDataPoints
								.toArray(new DataPoint[0]));
				CorrectedSpectrum newSpectrum = new CorrectedSpectrum(spectrum,
						rawDataFileWriter, filteredDataPoints.size(), storageID);
				rawDataFileWriter.addScan(newSpectrum);

				processedScans++;
			}

			// If this task was canceled, stop processing
			if (!isCanceled()) {
				// Finalize writing
				filteredDataFile = rawDataFileWriter.finishWriting();

				// Add the newly created file to the project
				final MZmineProject project = MZmineCore.getCurrentProject();
				project.addFile(filteredDataFile);

				// Remove the original data file if requested
				if (removeOriginal)
					project.removeFile(dataFile);

				setStatus(TaskStatus.FINISHED);
				logger.info("Finished spectrum filter " + dataFile.getName());
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Spectrum filtering error", e);
			setStatus(TaskStatus.ERROR);
			errorMessage = e.getMessage();
		}
	}
}