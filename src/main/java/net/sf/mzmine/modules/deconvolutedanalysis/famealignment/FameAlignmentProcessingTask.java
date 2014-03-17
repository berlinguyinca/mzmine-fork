package net.sf.mzmine.modules.deconvolutedanalysis.famealignment;

import com.google.common.collect.Lists;
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
import net.sf.mzmine.taskcontrol.AbstractTask;
import net.sf.mzmine.taskcontrol.TaskStatus;

import java.util.*;
import java.util.logging.Logger;

public class FameAlignmentProcessingTask extends AbstractTask {
	/** Logger */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	/** Data file to be processed */
	private final RawDataFile dataFile;

	/** Ionization method used for this data file */
	SpectrumType ionizationType;

	/** Time window in which to search for FAME peaks */
	private double timeWindow;

	// Progress counters
	private int processedScans = 0;
	private int totalScans;

	public FameAlignmentProcessingTask(final RawDataFile dataFile,
			final ParameterSet parameters, SpectrumType ionizationType) {
		this.dataFile = dataFile;
		this.ionizationType = ionizationType;
		this.timeWindow = parameters.getParameter(
				FameAlignmentParameters.MATCH_TIME_WINDOW).getValue() / 60;
	}

	@Override
	public String getTaskDescription() {
		return "Performing retention index correction on " + dataFile;
	}

	@Override
	public double getFinishedPercentage() {
		return totalScans == 0 ? 0.0 : (double) processedScans
				/ (double) totalScans;
	}

	public void run() {
		// Update the status of this task
		setStatus(TaskStatus.PROCESSING);
		logger.info("Started FAME marker search on " + dataFile);

		// Set total number of scans to process
		totalScans = dataFile.getNumOfScans();

		// Process PCI Spectra
		if (ionizationType == SpectrumType.METHANE
				|| ionizationType == SpectrumType.ISOBUTANE)
			processPCI();

		// Process EI Spectra
		else if (ionizationType == SpectrumType.EI)
			processEI();

		// If this task was canceled, stop processing
		if (!isCanceled()) {
			// Set task status to FINISHED
			setStatus(TaskStatus.FINISHED);

			logger.info("Finished performing retention index correction on "
					+ dataFile);
		}
	}

	private void processEI() {
		// List of all FAME peak candidates
		List<Scan> allCandidates = new ArrayList<Scan>();
		double maxBasePeakIntensity = 0;

		// Filter data to spectra that have a base peak in a list of known EI
		// ions
		for (int scanNumber : dataFile.getScanNumbers(1)) {
			// Canceled?
			if (isCanceled())
				return;

			// Get the current spectrum
			Scan spectrum = dataFile.getScan(scanNumber);

			if (spectrum.getBasePeak() == null)
				continue;

			// Check base peak and then secondary base peak
			double intensity = testEISpectrum(spectrum);

			if (intensity > 0)
				allCandidates.add(spectrum);

			// Compute maximum base peak intensity out of our list of candidates
			if (intensity > maxBasePeakIntensity)
				maxBasePeakIntensity = intensity;

			processedScans++;
		}

		// Filter those peaks with low base peak intensities
		for (Iterator<Scan> it = allCandidates.iterator(); it.hasNext();) {
			if (it.next().getBasePeak().getIntensity() < maxBasePeakIntensity / 1000)
				it.remove();
		}

		// Find spectrum with the highest similarity to a library spectrum
		double maxSimilarity = 0;
		Scan highestMatch = null;
		int libraryMatch = -1;

		for (Scan s : allCandidates) {
			double bestSimilarity = 0;
			int matchesCount = 0;

			for (int i = 0; i < FameData.N_FAMES; i++) {
				String name = FameData.FAME_NAMES[i];

				double similarity = FameData.computeSimilarity(name,
						(CorrectedSpectrum) s);

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
			}
		}

		// Return an error if no initial match is found
		if (highestMatch == null) {
			MZmineCore.getDesktop().displayErrorMessage(
					"Unable to find initial standard match in "
							+ dataFile.getName());
			setStatus(TaskStatus.ERROR);
			cancel();
			return;
		}

		// Product a list of candidates for each individual FAME peak
		List<List<Scan>> candidates = new ArrayList<List<Scan>>();

		for (int i = 0; i < FameData.N_FAMES; i++) {
			List<Scan> matches = new ArrayList<Scan>();

			if (i == libraryMatch) {
				for (Scan s : allCandidates) {
					double intensity = testEISpectrum(s);

					if (intensity > 0
							&& Math.abs(s.getRetentionTime()
									- highestMatch.getRetentionTime()) < timeWindow)
						matches.add(s);
				}
			}

			else {
				double shift = FameData.FAME_RETENTION_TIMES[libraryMatch]
						- FameData.FAME_RETENTION_TIMES[i];
				double expectedRt = highestMatch.getRetentionTime() - shift;

				for (Scan s : allCandidates) {
					double intensity = testEISpectrum(s);

					if (intensity > 0
							&& Math.abs(s.getRetentionTime() - expectedRt) < timeWindow)
						matches.add(s);
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
			List<Scan> matches = candidates.get(i);

			Scan bestMatch = null;
			maxBasePeakIntensity = 0;
			maxSimilarity = 0;

			for (Scan s : matches) {
				if (bestMatch == null) {
					bestMatch = s;
					maxBasePeakIntensity = s.getBasePeak().getIntensity();
				} else {
					double similarity = FameData.computeSimilarity(libraryName,
							(CorrectedSpectrum) s);

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

		logger.info(dataFile + " " + fameTimes);
		logger.info(fameNames + "");

		// Apply linear/polynomial fit
		CombinedRegression fit = new CombinedRegression(5);
		fit.setData(Doubles.toArray(fameTimes), Doubles.toArray(fameIndices));

		// Add calculated retention index to each mass spectrum
		for (int scanNumber : dataFile.getScanNumbers(1)) {
			if (dataFile.getScan(scanNumber) instanceof CorrectedSpectrum) {
				CorrectedSpectrum s = (CorrectedSpectrum) dataFile
						.getScan(scanNumber);
				s.setRetentionIndex((int) fit.getY(s.getRetentionTime()));
				s.setRetentionCorrection(fit);
			}
		}
	}

	private double testEISpectrum(Scan s) {
		if (Ints.asList(FameData.FAME_BASE_PEAKS).contains(
				(int) s.getBasePeak().getMZ()))
			return s.getBasePeak().getIntensity();
		else if (s instanceof CorrectedSpectrum) {
			CorrectedSpectrum spectrum = (CorrectedSpectrum) s;
			DataPoint secondaryBasePeak = spectrum.getSecondaryBasePeak();

			if (secondaryBasePeak.getMZ() == 74
					|| secondaryBasePeak.getMZ() == 87)
				return secondaryBasePeak.getIntensity();
		}

		return -1;
	}

	private void processPCI() {
		// Product a list of candidates for each individual FAME peak
		List<List<Scan>> candidates = new ArrayList<List<Scan>>();

		Scan bestMatch = null, secondaryBestMatch = null;
		int libraryMatch = -1;

		for (int i = 0; i < FameData.N_FAMES; i++) {
			// Search for [M + H]+ ion for each FAME marker
			int mass = FameData.FAME_MASSES[i] + 1;
			String name = FameData.FAME_NAMES[i];

			List<Scan> matches = new ArrayList<Scan>();
			double maxBasePeakIntensity = 0;

			for (int scanNumber : dataFile.getScanNumbers(1)) {
				// Canceled?
				if (isCanceled())
					return;

				// Get the current spectrum
				Scan spectrum = dataFile.getScan(scanNumber);
				DataPoint basePeak = spectrum.getBasePeak();

				if (basePeak == null)
					continue;

				if ((int) basePeak.getMZ() == mass) {
					matches.add(spectrum);

					// Compute maximum base peak intensity of these FAME markers
					if (basePeak.getIntensity() > maxBasePeakIntensity)
						maxBasePeakIntensity = basePeak.getIntensity();
				}
			}

			// Filter candidates
			for (Iterator<Scan> it = matches.iterator(); it.hasNext();) {
				Scan s = it.next();

				// Filter those peaks with low base peak intensities
				if (s.getBasePeak().getIntensity() < maxBasePeakIntensity / 100)
					it.remove();
			}

			// Check if there is only a single best match
			Scan highestMatch = null;
			int count = 0;

			for (Scan s : matches) {
				if (s.getBasePeak().getIntensity() > 0.5 * maxBasePeakIntensity) {
					if (highestMatch == null)
						highestMatch = s;
					count++;
				}
			}

			if (count == 1) {
				matches = new ArrayList<Scan>();
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
							+ dataFile.getName());
			setStatus(TaskStatus.ERROR);
			cancel();
			return;
		}

		List<Double> fameTimes = new ArrayList<Double>();
		List<Double> fameIndices = new ArrayList<Double>();
		List<String> fameNames = new ArrayList<String>();

		// Search for each FAME marker individually
		for (int i = 0; i < FameData.N_FAMES; i++) {
			List<Scan> matches = candidates.get(i);

			if (matches.size() > 0) {
				double shift = FameData.FAME_RETENTION_TIMES[libraryMatch]
						- FameData.FAME_RETENTION_TIMES[i];
				double expectedRt = bestMatch.getRetentionTime() - shift;

				for (Iterator<Scan> it = matches.iterator(); it.hasNext();) {
					Scan s = it.next();

					// Filter those peaks outside of expected range
					if (Math.abs(s.getRetentionTime() - expectedRt) > timeWindow)
						it.remove();
				}

				double maxBasePeakIntensity = 0;
				Scan highestMatch = null;

				for (Scan s : matches) {
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

		logger.info(dataFile + " " + fameTimes);
		logger.info(fameNames.toString());

		// Apply linear/polynomial fit
		CombinedRegression fit = new CombinedRegression(5);
		fit.setData(Doubles.toArray(fameTimes), Doubles.toArray(fameIndices));

		// Add calculated retention index to each mass spectrum
		for (int scanNumber : dataFile.getScanNumbers(1)) {
			CorrectedSpectrum s = (CorrectedSpectrum) dataFile
					.getScan(scanNumber);
			s.setRetentionIndex((int) fit.getY(s.getRetentionTime()));
			s.setRetentionCorrection(fit);
		}
	}
}
