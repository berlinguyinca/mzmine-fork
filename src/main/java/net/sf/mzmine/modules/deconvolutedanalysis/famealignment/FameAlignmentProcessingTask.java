package net.sf.mzmine.modules.deconvolutedanalysis.famealignment;

import com.google.common.primitives.Doubles;
import edu.ucdavis.genomics.metabolomics.util.math.CombinedRegression;
import net.sf.mzmine.data.DataPoint;
import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.data.Scan;
import net.sf.mzmine.modules.deconvolutedanalysis.DeconvolutedSpectrum;
import net.sf.mzmine.modules.deconvolutedanalysis.SpectrumType;
import net.sf.mzmine.taskcontrol.AbstractTask;
import net.sf.mzmine.taskcontrol.TaskStatus;

import java.util.*;
import java.util.logging.Logger;

public class FameAlignmentProcessingTask extends AbstractTask {
	/** Logger */
	private Logger LOG = Logger.getLogger(this.getClass().getName());

	/** Data file to be processed */
	private final RawDataFile dataFile;

	/** Ionization method used for this data file */
	SpectrumType ionizationType;

	/** Time window in which to search for FAME peaks */
	private double timeWindow;

	// Progress counters
	private int processedScans = 0;
	private int totalScans;

	public FameAlignmentProcessingTask(RawDataFile dataFile,
			SpectrumType ionizationType) {
		this.dataFile = dataFile;
		this.ionizationType = ionizationType;
		this.timeWindow = FameAlignmentParameters.MATCH_TIME_WINDOW.getValue() / 60;
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
		LOG.info("Started FAME marker search on " + dataFile);

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

			LOG.info("Finished performing retention index correction on "
					+ dataFile);
		}
	}

	private void processEI() {
		// List of all FAME peak candidates
		List<Scan> allCandidates = new ArrayList<Scan>();
		double maxBasePeakIntensity = 0;

		// Filter data to spectra that have a base peak at m/z = 74 or 87
		for (int scanNumber : dataFile.getScanNumbers(1)) {
			// Canceled?
			if (isCanceled())
				return;

			// Get the current spectrum
			Scan spectrum = dataFile.getScan(scanNumber);
			DataPoint basePeak = spectrum.getBasePeak();

			if (basePeak.getMZ() == 74 || basePeak.getMZ() == 87) {
				allCandidates.add(spectrum);

				// Compute maximum base peak intensity out of our list of
				// candidates
				if (basePeak.getIntensity() > maxBasePeakIntensity)
					maxBasePeakIntensity = basePeak.getIntensity();
			}

			processedScans++;
		}

		// Filter those peaks with low base peak intensities
		for (Iterator<Scan> it = allCandidates.iterator(); it.hasNext();) {
			if (it.next().getBasePeak().getIntensity() < maxBasePeakIntensity / 100)
				it.remove();
		}

		// Product a list of candidates for each individual FAME peak
		List<List<Scan>> candidates = new ArrayList<List<Scan>>();
		List<Integer> candidateMatches = new ArrayList<Integer>();

		for (int i = 0; i < FameData.N_FAMES; i++) {
			List<Integer> matchCount = new ArrayList<Integer>();

			for (Scan s : allCandidates) {
				// Compute expected shifts in FAME peaks from the current
				// spectrum
				double[] shift = FameData.FAME_RETENTION_TIMES.clone();

				for (int j = 0; j < FameData.N_FAMES; j++)
					shift[j] += s.getRetentionTime()
							- FameData.FAME_RETENTION_TIMES[i];

				// Count the number of shifted retention times that match within
				// the given time window of FAME candidates
				int matches = 0;

				for (Double retentionShift : shift) {
					for (Scan s2 : allCandidates) {
						if (Math.abs(retentionShift - s2.getRetentionTime()) < timeWindow) {
							matches++;
							break;
						}
					}
				}

				matchCount.add(matches);
			}

			// Store spectra with maximal matches
			int maxMatches = Collections.max(matchCount);
			List<Scan> currentCandidates = new ArrayList<Scan>();

			for (int j = 0; j < matchCount.size(); j++)
				if (matchCount.get(j) == maxMatches)
					currentCandidates.add(allCandidates.get(j));

			candidates.add(currentCandidates);
			candidateMatches.add(maxMatches);
		}

		// Apply spectral similarity to choose the best match for each FAME peak
		List<Double> fameTimes = new ArrayList<Double>();
		List<Double> fameIndices = new ArrayList<Double>();

		for (int i = 0; i < FameData.N_FAMES; i++) {
			if (candidateMatches.get(i) > 1) {
				String name = FameData.FAME_NAMES[i];
				double maxSimilarity = 0;
				double retentionTime = 0;

				for (Scan s : candidates.get(i)) {
					double similarity = FameData.computeSimilarity(name,
							(DeconvolutedSpectrum) s);

					if (similarity > maxSimilarity) {
						maxSimilarity = similarity;
						retentionTime = s.getRetentionTime();
					}
				}

				// If a valid peak is found, add it
				if (fameTimes.size() == 0
						|| retentionTime > fameTimes.get(fameTimes.size() - 1)) {
					fameTimes.add(retentionTime);
					fameIndices
							.add((double) FameData.FAME_RETENTION_INDICES[i]);
				}
			}
		}

		LOG.info(dataFile + " " + fameTimes);

		// Apply linear/polynomial fit
		CombinedRegression fit = new CombinedRegression(5);
		fit.setData(Doubles.toArray(fameTimes), Doubles.toArray(fameIndices));

		// Add calculated retention index to each mass spectrum
		for (int scanNumber : dataFile.getScanNumbers(1)) {
			DeconvolutedSpectrum s = (DeconvolutedSpectrum) dataFile
					.getScan(scanNumber);
			s.setRetentionIndex((int) fit.getY(s.getRetentionTime()));
		}
	}

	private void processPCI() {
		List<Double> fameTimes = new ArrayList<Double>();
		List<Double> fameIndices = new ArrayList<Double>();

		// Search for each FAME marker individually
		for (int i = 0; i < FameData.N_FAMES; i++) {
			// Search for [M + H]+ ion for each FAME marker
			String name = FameData.FAME_NAMES[i];
			int mass = FameData.FAME_MASSES[i] + 1;

			List<Scan> candidates = new ArrayList<Scan>();
			double maxBasePeakIntensity = 0;

			// Filter data to spectra that have a base peak at the current ion
			// mass
			for (int scanNumber : dataFile.getScanNumbers(1)) {
				// Canceled?
				if (isCanceled())
					return;

				// Get the current spectrum
				Scan spectrum = dataFile.getScan(scanNumber);
				DataPoint basePeak = spectrum.getBasePeak();

				if (basePeak.getMZ() == mass) {
					candidates.add(spectrum);

					// Compute maximum base peak intensity out of our list of
					// candidates
					if (basePeak.getIntensity() > maxBasePeakIntensity)
						maxBasePeakIntensity = basePeak.getIntensity();
				}

				processedScans++;
			}

			// Filter candidates
			for (Iterator<Scan> it = candidates.iterator(); it.hasNext();) {
				Scan s = it.next();

				// Filter those peaks with low base peak intensities
				if (s.getBasePeak().getIntensity() < maxBasePeakIntensity / 100)
					it.remove();

				// Filter those peaks with retention time less than that of the
				// previous FAME peak
				else if (fameTimes.size() > 0
						&& s.getRetentionTime() < fameTimes.get(fameTimes
								.size() - 1))
					it.remove();
			}

			// Apply spectral similarity to choose the best match for each FAME
			// peak
			double maxSimilarity = 0;
			double retentionTime = 0;

			for (Scan s : candidates) {
				double similarity = FameData.computeSimilarity(name,
						(DeconvolutedSpectrum) s);

				if (similarity > maxSimilarity) {
					maxSimilarity = similarity;
					retentionTime = s.getRetentionTime();
				}
			}

			fameTimes.add(retentionTime);
			fameIndices.add((double) FameData.FAME_RETENTION_INDICES[i]);
		}
		LOG.info(dataFile + " " + fameTimes);

		// Apply linear/polynomial fit
		CombinedRegression fit = new CombinedRegression(5);
		fit.setData(Doubles.toArray(fameTimes), Doubles.toArray(fameIndices));

		// Add calculated retention index to each mass spectrum
		for (int scanNumber : dataFile.getScanNumbers(1)) {
			DeconvolutedSpectrum s = (DeconvolutedSpectrum) dataFile
					.getScan(scanNumber);
			s.setRetentionIndex((int) fit.getY(s.getRetentionTime()));
		}
	}

	private void reduceCandidates(List<Double> fameTimes,
			List<Double> fameIndices) {

	}
}
