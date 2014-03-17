package net.sf.mzmine.modules.deconvolutedanalysis.massdetection;

import net.sf.mzmine.data.DataPoint;
import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.data.Scan;
import net.sf.mzmine.modules.peaklistmethods.identification.adductsearch.AdductType;
import net.sf.mzmine.modules.deconvolutedanalysis.SpectrumType;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.taskcontrol.AbstractTask;
import net.sf.mzmine.taskcontrol.TaskStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class SpectraMatcherProcessingTask extends AbstractTask {
	/** Logger */
	private Logger LOG = Logger.getLogger(this.getClass().getName());

	/** Data file to be processed */
	private final RawDataFile dataFile;

	/** Ionization method used for this data file */
	SpectrumType ionizationType;

	/** Adducts to search for */
	private final AdductType[] adducts;

	/** Number of adducts to match for a mass to be considered a candidate */
	private final int matchesThreshold;

	/** Collection of mass candidates for a specific ionization method */
	private List<MassCandidate> massCandidates;

	// Progress counters
	private int processedScans = 0;
	private int totalScans;

	public SpectraMatcherProcessingTask(RawDataFile dataFile,
			final ParameterSet parameters, SpectrumType ionizationType,
			List<MassCandidate> massCandidates) {
		this.dataFile = dataFile;
		this.ionizationType = ionizationType;
		this.adducts = parameters
				.getParameter(
						SpectraMatcherParameters.ADDUCT_PARAMS[ionizationType
								.ordinal()]).getValue();
		this.matchesThreshold = parameters.getParameter(
				SpectraMatcherParameters.ADDUCT_MATCHES[ionizationType
						.ordinal()]).getValue();
		this.massCandidates = massCandidates;
	}

	@Override
	public String getTaskDescription() {
		return "Detecting candidate masses in " + dataFile.getName();
	}

	@Override
	public double getFinishedPercentage() {
		return totalScans == 0 ? 0.0 : (double) processedScans
				/ (double) totalScans;
	}

	public void run() {
		// Update the status of this task
		setStatus(TaskStatus.PROCESSING);
		LOG.info("Started spectra mass matching on " + dataFile);

		// Set total number of scans to process
		totalScans = dataFile.getNumOfScans();

		// Count of matches found
		int matchesFound = 0;

		// Process each deconvoluted spectrum
		for (int scanNumber : dataFile.getScanNumbers(1)) {
			// Canceled?
			if (isCanceled())
				return;

			// Get the current spectrum
			Scan spectrum = dataFile.getScan(scanNumber);

			// Produce List of ion masses in the current spectrum
			// IMPORTANT: We assume the truncating the double-valued ion masses
			// will
			// since the deconvoluted data only gives integral values
			List<Integer> spectraMasses = new ArrayList<Integer>();

			for (DataPoint p : spectrum.getDataPoints())
				spectraMasses.add((int) p.getMZ());

			// Find candidate masses by iterating over all possible mass values
			// up to m/z = 1000
			for (int i = 1; i <= 1000; i++) {
				List<AdductType> adductMatches = new ArrayList<AdductType>();

				for (AdductType a : adducts) {
					if (spectraMasses.contains(i + (int) a.getMassDifference()))
						adductMatches.add(a);
				}

				if (adductMatches.size() >= matchesThreshold) {
					massCandidates.add(new MassCandidate(dataFile, scanNumber,
							spectrum.getRetentionTime(), i, ionizationType,
							adductMatches));
					matchesFound++;
				}
			}

			processedScans++;
		}

		// If this task was canceled, stop processing
		if (!isCanceled()) {
			// Set task status to FINISHED
			setStatus(TaskStatus.FINISHED);

			LOG.info("Finished spectra mass matching on " + dataFile
					+ ", found " + matchesFound);
		}
	}
}
