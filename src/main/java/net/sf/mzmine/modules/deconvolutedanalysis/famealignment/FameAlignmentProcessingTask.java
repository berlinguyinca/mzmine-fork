package net.sf.mzmine.modules.deconvolutedanalysis.famealignment;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import edu.ucdavis.genomics.metabolomics.util.math.CombinedRegression;
import net.sf.mzmine.data.DataPoint;
import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.data.Scan;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.modules.deconvolutedanalysis.CorrectedSpectrum;
import net.sf.mzmine.modules.deconvolutedanalysis.SpectrumType;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.project.MZmineProject;
import net.sf.mzmine.project.impl.RawDataFileImpl;
import net.sf.mzmine.taskcontrol.AbstractTask;
import net.sf.mzmine.taskcontrol.TaskStatus;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FameAlignmentProcessingTask extends AbstractTask {
	/** Logger */
	private final Logger logger = Logger.getLogger(this.getClass().getName());

	/** Data file to be processed */
	private final RawDataFile origDataFile;

	/** Corrected data file */
	private RawDataFile correctedDataFile = null;

	/** Ionization method used for this data file */
	SpectrumType ionizationType;

	/** Time window in which to search for FAME peaks */
	private double timeWindow;

	/** Number of processed scans */
	private int processedScans = 0;

	/** Number of scans to process */
	private int totalScans;

	/** Filename suffix */
	private String suffix;

	/** Remove original data file */
	private final boolean removeOriginal;

	/** List of clone spectra */
	List<CorrectedSpectrum> spectra;

	/** */
	List<Map<String, Correction>> correctionTable;

	public FameAlignmentProcessingTask(final RawDataFile dataFile,
			final ParameterSet parameters, SpectrumType ionizationType,
			List<Map<String, Correction>> correctionTable) {
		origDataFile = dataFile;
		this.ionizationType = ionizationType;
		this.correctionTable = correctionTable;

		// Get user parameters
		suffix = parameters.getParameter(FameAlignmentParameters.SUFFIX)
				.getValue();
		removeOriginal = parameters.getParameter(
				FameAlignmentParameters.REMOVE_ORIGINAL).getValue();
		timeWindow = parameters.getParameter(
				FameAlignmentParameters.MATCH_TIME_WINDOW).getValue() / 60;

		spectra = new ArrayList<CorrectedSpectrum>();
	}

	@Override
	public String getTaskDescription() {
		return "Performing retention index correction on " + origDataFile;
	}

	@Override
	public double getFinishedPercentage() {
		return totalScans == 0 ? 0.0 : (double) processedScans
				/ (double) totalScans;
	}

	public void run() {
		// Update the status of this task
		setStatus(TaskStatus.PROCESSING);
		logger.info("Started FAME marker search on " + origDataFile);

		// Set total number of scans to process
		totalScans = 2 * origDataFile.getNumOfScans();

		// Declare the file writer for the retention corrected file
		final RawDataFileImpl rawDataFileWriter;

		// Create new data file and convert spectra if necessary
		try {
			// Create a new file
			rawDataFileWriter = (RawDataFileImpl) MZmineCore
					.createNewFile(origDataFile.getName() + ' ' + suffix);

			// Process each deconvoluted spectrum
			for (int scanNumber : origDataFile.getScanNumbers(1)) {
				// Canceled?
				if (isCanceled())
					return;

				// Duplicate current spectrum, obtain data points and create
				// list of filtered data points
				Scan spectrum = origDataFile.getScan(scanNumber);

				// Add scan to new data file
				int storageID = rawDataFileWriter.storeDataPoints(spectrum
						.getDataPoints());
				CorrectedSpectrum newSpectrum = new CorrectedSpectrum(spectrum,
						rawDataFileWriter, spectrum.getNumberOfDataPoints(),
						storageID);

				spectra.add(newSpectrum);
				rawDataFileWriter.addScan(newSpectrum);

				processedScans++;
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE,
					"Retention correction initialization error", e);
			setStatus(TaskStatus.ERROR);
			errorMessage = e.getMessage();
			return;
		}

		// Process PCI Spectra
		if (ionizationType == SpectrumType.PCI
				|| ionizationType == SpectrumType.PCI_METHANE
				|| ionizationType == SpectrumType.PCI_ISOBUTANE)
			processPCI();

		// Process EI Spectra
		else if (ionizationType == SpectrumType.EI)
			processEI();

		// If this task was canceled, stop processing
		if (!isCanceled()) {
			try {
				// Finalize writing
				correctedDataFile = rawDataFileWriter.finishWriting();

				// Add the newly created file to the project
				final MZmineProject project = MZmineCore.getCurrentProject();
				project.addFile(correctedDataFile);

				// Remove the original data file if requested
				if (removeOriginal)
					project.removeFile(origDataFile);
			} catch (IOException e) {
				logger.log(Level.SEVERE,
						"Retention correction finalization error", e);
				setStatus(TaskStatus.ERROR);
				errorMessage = e.getMessage();
				return;
			}

			// Set task status to FINISHED
			setStatus(TaskStatus.FINISHED);
			logger.info("Finished performing retention index correction on "
					+ origDataFile);
		}
	}

	/**
	 * Perform processing of EI spectra
	 */
	private void processEI() {
		// List of all FAME peak candidates
		List<CorrectedSpectrum> allCandidates = new ArrayList<CorrectedSpectrum>();
		double maxBasePeakIntensity = 0;

		// Filter out spectra that do not a base peak in a list of known EI
		// ions
		for (CorrectedSpectrum spectrum : spectra) {
			// Canceled?
			if (isCanceled())
				return;

			// Check base peak and then secondary base peak
			double intensity = testEISpectrum(spectrum);

			if (intensity > 0) {
				allCandidates.add(spectrum);
				totalScans += 2 * FameData.N_FAMES;
			}

			// Compute maximum base peak intensity out of our list of candidates
			if (intensity > maxBasePeakIntensity)
				maxBasePeakIntensity = intensity;

			processedScans++;
		}

		// Filter those peaks with low base peak intensities
		for (Iterator<CorrectedSpectrum> it = allCandidates.iterator(); it
				.hasNext();) {
			if (it.next().getBasePeak().getIntensity() < maxBasePeakIntensity / 1000)
				it.remove();
		}

		// Find spectrum with the highest similarity to a library spectrum
		double maxSimilarity = 0;
		CorrectedSpectrum highestMatch = null;
		int libraryMatch = -1;

		for (CorrectedSpectrum s : allCandidates) {
			double bestSimilarity = 0;
			int matchesCount = 0;

			for (int i = 0; i < FameData.N_FAMES; i++) {
				String name = FameData.FAME_NAMES[i];

				double similarity = FameData.computeSimilarity(name, s);

				if (similarity > bestSimilarity) {
					bestSimilarity = similarity;
					matchesCount++;
				}

				if (bestSimilarity > maxSimilarity) {
					maxSimilarity = bestSimilarity;
					highestMatch = s;
					libraryMatch = i;
					logger.info("Best Match: " + name + " " + similarity + " "
							+ s.getScanNumber() + " " + s.getRetentionTime()
							+ " " + matchesCount);
				}

				processedScans++;
			}
		}

		// Return an error if no initial match is found
		if (highestMatch == null) {
			MZmineCore.getDesktop().displayErrorMessage(
					"Unable to find initial standard match in "
							+ origDataFile.getName());
			setStatus(TaskStatus.ERROR);
			cancel();
			return;
		}

		// Product a list of candidates for each individual FAME peak
		List<List<CorrectedSpectrum>> candidates = new ArrayList<List<CorrectedSpectrum>>();

		for (int i = 0; i < FameData.N_FAMES; i++) {
			List<CorrectedSpectrum> matches = new ArrayList<CorrectedSpectrum>();

			if (i == libraryMatch) {
				for (CorrectedSpectrum s : allCandidates) {
					double intensity = testEISpectrum(s);

					if (intensity > 0
							&& Math.abs(s.getRetentionTime()
									- highestMatch.getRetentionTime()) < timeWindow)
						matches.add(s);

					processedScans++;
				}
			}

			else {
				double shift = FameData.FAME_RETENTION_TIMES[libraryMatch]
						- FameData.FAME_RETENTION_TIMES[i];
				double expectedRt = highestMatch.getRetentionTime() - shift;

				for (CorrectedSpectrum s : allCandidates) {
					double intensity = testEISpectrum(s);

					if (intensity > 0
							&& Math.abs(s.getRetentionTime() - expectedRt) < timeWindow)
						matches.add(s);

					processedScans++;
				}
			}

			candidates.add(matches);
		}

		// Apply spectral similarity to choose the best match for each FAME peak
		List<Double> fameTimes = new ArrayList<Double>();
		List<Double> fameIndices = new ArrayList<Double>();
		List<String> fameNames = new ArrayList<String>();

		for (int i = 0; i < FameData.N_FAMES; i++) {
			String libraryName = FameData.FAME_NAMES[i];
			List<CorrectedSpectrum> matches = candidates.get(i);

			CorrectedSpectrum bestMatch = null;
			maxBasePeakIntensity = 0;
			maxSimilarity = 0;

			for (CorrectedSpectrum s : matches) {
				if (bestMatch == null) {
					bestMatch = s;
					maxBasePeakIntensity = s.getBasePeak().getIntensity();
				} else {
					double similarity = FameData.computeSimilarity(libraryName,
							s);

					// || (similarity < maxSimilarity &&
					// s.getBasePeak().getIntensity() / maxBasePeakIntensity >
					// similarity / maxSimilarity)
					if ((similarity > maxSimilarity && s.getBasePeak()
							.getIntensity() > maxBasePeakIntensity)) {
						bestMatch = s;
						maxBasePeakIntensity = s.getBasePeak().getIntensity();
						maxSimilarity = similarity;
					}
				}
			}

			if (bestMatch != null) {
				fameTimes.add(bestMatch.getRetentionTime());
				fameIndices.add((double) FameData.FAME_RETENTION_INDICES[i]);
				fameNames.add(FameData.FAME_NAMES[i]);
			}
		}

		// Store retention correction results
		Map<String, Correction> results = new HashMap<String, Correction>();

		for (int i = 0; i < fameTimes.size(); i++)
			results.put(fameNames.get(i), new Correction(origDataFile,
					fameTimes.get(i), (int) fameIndices.get(i).doubleValue()));

		correctionTable.add(results);

		logger.info(origDataFile + " " + fameTimes);
		logger.info(fameNames + "");

		// Apply linear/polynomial fit
		CombinedRegression fit = new CombinedRegression(5);
		fit.setData(Doubles.toArray(fameTimes), Doubles.toArray(fameIndices));

		// Add calculated retention index to each mass spectrum
		for (CorrectedSpectrum s : spectra) {
			s.setRetentionIndex((int) fit.getY(s.getRetentionTime()));
			s.setRetentionCorrection(fit);
		}
	}

	/**
	 * Check if spectrum passes the initial base peak check for processing.
	 * 
	 * @return intensity of the base peak if this spectrum passes, otherwise -1
	 */
	private double testEISpectrum(CorrectedSpectrum s) {
		List<Integer> fameBasePeaks = Ints.asList(FameData.FAME_BASE_PEAKS);

		if (s.getBasePeak() == null)
			return -1;

		else if (fameBasePeaks.contains((int) s.getBasePeak().getMZ()))
			return s.getBasePeak().getIntensity();

		else {
			DataPoint secondaryBasePeak = s.getSecondaryBasePeak();

			if (secondaryBasePeak != null
					&& fameBasePeaks.contains((int) secondaryBasePeak.getMZ()))
				return secondaryBasePeak.getIntensity();
			else
				return -1;
		}

	}

	/**
	 * Perform processing of PCI spectra
	 */
	private void processPCI() {
		// Product a list of candidates for each individual FAME peak
		List<List<CorrectedSpectrum>> candidates = new ArrayList<List<CorrectedSpectrum>>();

		CorrectedSpectrum bestMatch = null, secondaryBestMatch = null;
		int libraryMatch = -1;

		for (int i = 0; i < FameData.N_FAMES; i++) {
			// Search for [M + H]+ ion for each FAME marker
			int mass = FameData.FAME_MASSES[i] + 1;
			String name = FameData.FAME_NAMES[i];

			List<CorrectedSpectrum> matches = new ArrayList<CorrectedSpectrum>();
			double maxBasePeakIntensity = 0;

			for (CorrectedSpectrum spectrum : spectra) {
				// Canceled?
				if (isCanceled())
					return;

				DataPoint basePeak = spectrum.getBasePeak();

				if (basePeak != null && (int) basePeak.getMZ() == mass) {
					matches.add(spectrum);

					// Compute maximum base peak intensity of these FAME markers
					if (basePeak.getIntensity() > maxBasePeakIntensity)
						maxBasePeakIntensity = basePeak.getIntensity();
				}
			}

			// Filter candidates
			for (Iterator<CorrectedSpectrum> it = matches.iterator(); it
					.hasNext();) {
				CorrectedSpectrum s = it.next();

				// Filter those peaks with low base peak intensities
				if (s.getBasePeak().getIntensity() < maxBasePeakIntensity / 100)
					it.remove();
			}

			// Check if there is only a single best match
			CorrectedSpectrum highestMatch = null;
			int count = 0;

			for (CorrectedSpectrum s : matches) {
				if (s.getBasePeak().getIntensity() > 0.5 * maxBasePeakIntensity) {
					if (highestMatch == null)
						highestMatch = s;
					count++;
				}
			}

			if (count == 1) {
				matches = new ArrayList<CorrectedSpectrum>();
				matches.add(highestMatch);

				if (bestMatch == null && i > 1 && i < FameData.N_FAMES - 2) {
					bestMatch = highestMatch;
					libraryMatch = i;
					logger.info("Best Match: " + name + " "
							+ bestMatch.getScanNumber() + " "
							+ bestMatch.getRetentionTime());
				}

				secondaryBestMatch = highestMatch;
			}

			candidates.add(matches);
		}

		if (bestMatch == null)
			bestMatch = secondaryBestMatch;

		// Return an error if no initial match is found
		if (bestMatch == null) {
			MZmineCore.getDesktop().displayErrorMessage(
					"Unable to find initial standard match in "
							+ origDataFile.getName());
			setStatus(TaskStatus.ERROR);
			cancel();
			return;
		}

		List<Double> fameTimes = new ArrayList<Double>();
		List<Double> fameIndices = new ArrayList<Double>();
		List<String> fameNames = new ArrayList<String>();

		// Search for each FAME marker individually
		for (int i = 0; i < FameData.N_FAMES; i++) {
			List<CorrectedSpectrum> matches = candidates.get(i);

			if (matches.size() > 0) {
				double shift = FameData.FAME_RETENTION_TIMES[libraryMatch]
						- FameData.FAME_RETENTION_TIMES[i];
				double expectedRt = bestMatch.getRetentionTime() - shift;

				for (Iterator<CorrectedSpectrum> it = matches.iterator(); it
						.hasNext();) {
					CorrectedSpectrum s = it.next();

					// Filter those peaks outside of expected range
					if (Math.abs(s.getRetentionTime() - expectedRt) > timeWindow)
						it.remove();
				}

				double maxBasePeakIntensity = 0;
				CorrectedSpectrum highestMatch = null;

				for (CorrectedSpectrum s : matches) {
					if (s.getBasePeak().getIntensity() > maxBasePeakIntensity) {
						highestMatch = s;
						maxBasePeakIntensity = s.getBasePeak().getIntensity();
					}
				}

				if (highestMatch != null) {
					fameTimes.add(highestMatch.getRetentionTime());
					fameIndices
							.add((double) FameData.FAME_RETENTION_INDICES[i]);
					fameNames.add(FameData.FAME_NAMES[i]);
				}
			}
		}

		// Store retention correction results
		Map<String, Correction> results = new HashMap<String, Correction>();

		for (int i = 0; i < fameTimes.size(); i++)
			results.put(fameNames.get(i), new Correction(origDataFile,
					fameTimes.get(i), (int) fameIndices.get(i).doubleValue()));

		correctionTable.add(results);

		logger.info(origDataFile + " " + fameTimes);
		logger.info(fameNames.toString());

		// Apply linear/polynomial fit
		CombinedRegression fit = new CombinedRegression(5);
		fit.setData(Doubles.toArray(fameTimes), Doubles.toArray(fameIndices));

		// Add calculated retention index to each mass spectrum
		for (CorrectedSpectrum s : spectra) {
			s.setRetentionIndex((int) fit.getY(s.getRetentionTime()));
			s.setRetentionCorrection(fit);
		}
	}
}
