package net.sf.mzmine.modules.deconvolutedanalysis.famealignment;

import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import edu.ucdavis.genomics.metabolomics.util.math.CombinedRegression;
import net.sf.mzmine.data.DataPoint;
import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.data.Scan;
import net.sf.mzmine.data.impl.SimpleScan;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.modules.deconvolutedanalysis.CorrectedSpectrum;
import net.sf.mzmine.modules.deconvolutedanalysis.SpectrumType;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.project.MZmineProject;
import net.sf.mzmine.project.impl.RawDataFileImpl;
import net.sf.mzmine.taskcontrol.AbstractTask;
import net.sf.mzmine.taskcontrol.TaskStatus;
import net.sf.mzmine.util.Range;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FameAlignmentProcessingTask extends AbstractTask {
	/** Logger */
	private final Logger logger = Logger.getLogger(getClass().getName());

	/** Double value tolerance */
	public static final double EPSILON = 1.0e-14;

	/*
	 * Task variables
	 */

	/** Data file to be processed */
	private final RawDataFile dataFile;

	/** Corrected data file */
	private RawDataFile correctedDataFile = null;

	/** Number of processed scans */
	private int processedScans = 0;

	/** Number of scans to process */
	private int totalScans;

	/*
	 * User parameters
	 */

	/** Ionization method used for this data file */
	SpectrumType ionizationType;

	/** Time window in which to search for FAME peaks */
	private double timeWindow;

	/** Filename suffix */
	private String suffix;

	/** Remove original data file */
	private final boolean removeOriginal;

	/*
	 * Generated data
	 */

	/** Collection of spectra for processing and analysis */
	List<CorrectedSpectrum> spectra;

	/** */
	Map<String, FameCorrection> results = null;

	/**
	 * 
	 * @param dataFile
	 * @param parameters
	 * @param ionizationType
	 */
	public FameAlignmentProcessingTask(final RawDataFile dataFile,
			final ParameterSet parameters, SpectrumType ionizationType) {

		// Set original data file
		this.dataFile = dataFile;

		// Set ionization type
		this.ionizationType = ionizationType;

		// Get user parameters
		suffix = parameters.getParameter(FameAlignmentParameters.SUFFIX)
				.getValue();
		removeOriginal = parameters.getParameter(
				FameAlignmentParameters.REMOVE_ORIGINAL).getValue();
		timeWindow = parameters.getParameter(
				FameAlignmentParameters.MATCH_TIME_WINDOW).getValue() / 60;

		// Spectra stored for processing and analysis
		spectra = new ArrayList<CorrectedSpectrum>();
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

	@Override
	public Object[] getCreatedObjects() {
		return new Object[]{correctedDataFile};
	}

	/**
	 * 
	 * @return
	 */
	public RawDataFile getDataFile() {
		return dataFile;
	}

	/**
	 * 
	 * @return
	 */
	public RawDataFile getCorrectedDataFile() {
		return correctedDataFile;
	}

	/**
	 * 
	 * @return
	 */
	public Map<String, FameCorrection> getResults() {
		return results;
	}

	public void run() {
		// Update the status of this task
		setStatus(TaskStatus.PROCESSING);
		logger.info("Started retention index correction on " + dataFile);

		// Set total number of scans to process
		totalScans = 2 * dataFile.getNumOfScans();

		try {
			// Create a new file
			final RawDataFileImpl rawDataFileWriter = (RawDataFileImpl) MZmineCore
					.createNewFile(dataFile.getName() + ' ' + suffix);

			// Process each spectrum
			for (int scanNumber : dataFile.getScanNumbers(1)) {
				// Canceled?
				if (isCanceled())
					return;

				Scan spectrum = dataFile.getScan(scanNumber);

				// Add scan to new data file
				int storageID = rawDataFileWriter.storeDataPoints(spectrum
						.getDataPoints());
				CorrectedSpectrum newSpectrum = new CorrectedSpectrum(spectrum,
						rawDataFileWriter, spectrum.getNumberOfDataPoints(),
						storageID);

				rawDataFileWriter.addScan(newSpectrum);

				// Store spectrum locally as a `SimpleScan` for processing and
				// analysis
				spectra.add(newSpectrum);

				processedScans++;
			}

			// Finalize writing and add the newly created file to the project
			correctedDataFile = rawDataFileWriter.finishWriting();

			final MZmineProject project = MZmineCore.getCurrentProject();
			project.addFile(correctedDataFile);

			// Remove the original data file if requested
			if (removeOriginal)
				project.removeFile(dataFile);
		} catch (IOException e) {
			logger.log(Level.SEVERE,
					"Retention correction initialization error", e);
			setStatus(TaskStatus.ERROR);
			errorMessage = e.getMessage();
			return;
		}

		// Process spectrum depending on ionization type
		switch (ionizationType) {
			case PCI :
			case PCI_METHANE :
			case PCI_ISOBUTANE :
				processPCI();
				break;

			case EI :
				processEI();
				break;
		}

		// If this task was canceled, stop processing
		if (!isCanceled()) {
			// Set task status to FINISHED
			setStatus(TaskStatus.FINISHED);
			logger.info("Finished retention index correction on " + dataFile);
		}
	}

	/**
	 * Perform processing of EI spectra
	 */
	private void processEI() {
		// List of all FAME peak candidates
		List<SimpleScan> allCandidates = new ArrayList<SimpleScan>();

		// Filter out spectra that do not a base peak in a list of known EI
		// ions
		for (CorrectedSpectrum s : spectra) {
			// Canceled?
			if (isCanceled())
				return;

			// Check whether the base peak or secondary base peak are valid
			// fragments
			List<Integer> fameBasePeaks = Ints.asList(FameData.FAME_BASE_PEAKS);

			if (s.getBasePeak() != null) {
				DataPoint secondaryBasePeak = s.getSecondaryBasePeak();

				if (fameBasePeaks.contains((int) s.getBasePeak().getMZ())
						|| (secondaryBasePeak != null && (secondaryBasePeak
								.getMZ() == 74 || secondaryBasePeak.getMZ() == 87))) {
					allCandidates.add(new SimpleScan(s));
					totalScans += 2 * FameData.N_FAMES;
				}
			}

			processedScans++;
		}

		// Perform C13 Isotope filtering on candidates
		for (SimpleScan s : allCandidates)
			applyC13IsotopeFilter(s);

		// Find spectrum with the highest similarity to a library spectrum
		double maxSimilarity = 0;
		int libraryMatch = -1;
		SimpleScan highestMatch = null;

		for (SimpleScan s : allCandidates) {
			DataPoint basePeak = s.getBasePeak();

			double bestSimilarity = 0;
			int matchesCount = 0;

			for (int i = 0; i < FameData.N_FAMES; i++) {
				String name = FameData.FAME_NAMES[i];

				// Check for ion qualifier
				int qualifier = FameData.QUALIFIER_IONS[i];
				double minRatio = FameData.MIN_QUAL_RATIO[i];
				double maxRatio = FameData.MAX_QUAL_RATIO[i];

				DataPoint[] p = s.getDataPointsByMass(new Range(qualifier,
						qualifier));

				// Confirm that the qualifier ion exists
				if (p.length != 1)
					continue;

				// Check for similarity
				int minSimilarity = FameData.MIN_SIMILARITY[i];
				double similarity = FameData.computeSimilarity(name, s);

				if (similarity > bestSimilarity) {
					bestSimilarity = similarity;
					matchesCount++;
				}

				if (bestSimilarity > maxSimilarity) {
					logger.info("Best Match: " + dataFile + " " + name + " "
							+ similarity + " " + s.getScanNumber() + " "
							+ s.getRetentionTime() + " " + matchesCount);

					maxSimilarity = bestSimilarity;
					highestMatch = s;
					libraryMatch = i;
				}

				processedScans++;
			}
		}

		// Return an error if no initial match is found
		if (highestMatch == null) {
			MZmineCore.getDesktop().displayErrorMessage(
					"Unable to find initial standard match in "
							+ dataFile.getName());
			setStatus(TaskStatus.ERROR);
			return;
		}

		// Product a list of candidates for each individual FAME peak
		List<List<SimpleScan>> candidates = new ArrayList<List<SimpleScan>>();

		for (int i = 0; i < FameData.N_FAMES; i++) {
			List<SimpleScan> matches = new ArrayList<SimpleScan>();

			if (i == libraryMatch)
				matches.add(highestMatch);

			else {
				double expectedRT = highestMatch.getRetentionTime()
						- (FameData.FAME_RETENTION_TIMES[libraryMatch] - FameData.FAME_RETENTION_TIMES[i]);

				for (SimpleScan s : allCandidates) {
					if (s.getBasePeak() != null
							&& s.getBasePeak().getIntensity() > 0
							&& Math.abs(s.getRetentionTime() - expectedRT) < timeWindow)
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
			List<SimpleScan> matches = candidates.get(i);

			SimpleScan bestMatch = null;
			double maxBasePeakIntensity = 0;
			maxSimilarity = 0;

			for (SimpleScan s : matches) {
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
		results = new HashMap<String, FameCorrection>();

		for (int i = 0; i < fameTimes.size(); i++)
			results.put(fameNames.get(i), new FameCorrection(correctedDataFile,
					fameTimes.get(i), (int) fameIndices.get(i).doubleValue()));

		// Log results
		logger.info(correctedDataFile + "");
		logger.info(fameTimes + "");
		logger.info(fameNames + "");

		// Apply linear/polynomial fit
		CombinedRegression fit = new CombinedRegression(5);
		fit.setData(Doubles.toArray(fameTimes), Doubles.toArray(fameIndices));

		// Add calculated retention index to each mass spectrum
		for (int scanNumber : correctedDataFile.getScanNumbers(1)) {
			CorrectedSpectrum s = (CorrectedSpectrum) correctedDataFile
					.getScan(scanNumber);
			s.setRetentionIndex((int) fit.getY(s.getRetentionTime()));
			s.setRetentionCorrection(fit);
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
		results = new HashMap<String, FameCorrection>();

		for (int i = 0; i < fameTimes.size(); i++)
			results.put(fameNames.get(i), new FameCorrection(correctedDataFile,
					fameTimes.get(i), (int) fameIndices.get(i).doubleValue()));

		logger.info(correctedDataFile + " " + fameTimes);
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

	/**
	 * 
	 * @param s
	 */
	private void applyC13IsotopeFilter(SimpleScan s) {
		// Get the data points from the spectrum and sort by m/z
		List<DataPoint> dataPoints = Lists.newArrayList(s.getDataPoints());
		Collections.sort(dataPoints, new Comparator<DataPoint>() {
			@Override
			public int compare(DataPoint a, DataPoint b) {
				return a.getMZ() < b.getMZ() ? -1 : a.getMZ() > b.getMZ()
						? 1
						: 0;
			}
		});

		// Create a list for the filtered points
		List<DataPoint> filteredDataPoints = new ArrayList<DataPoint>();

		// Iterate over ion fragments and remove
		for (int i = dataPoints.size() - 1; i >= 0; i--) {
			// Step #1: Remove C13 Isotope ions
			if (i > 0
					&& dataPoints.get(i).getMZ()
							- dataPoints.get(i - 1).getMZ() < 1 + EPSILON
					&& dataPoints.get(i - 1).getIntensity() >= 1.5 * dataPoints
							.get(i).getIntensity())
				continue;

			// If the data point passes all filters, keep it.
			else
				filteredDataPoints.add(0, dataPoints.get(i));
		}

		// Commit the filtered data points to the given spectrum
		s.setDataPoints(filteredDataPoints
				.toArray(new DataPoint[filteredDataPoints.size()]));
	}
}
