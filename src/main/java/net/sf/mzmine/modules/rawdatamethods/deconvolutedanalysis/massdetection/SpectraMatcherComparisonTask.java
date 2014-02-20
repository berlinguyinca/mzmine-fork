package net.sf.mzmine.modules.rawdatamethods.deconvolutedanalysis.massdetection;

import net.sf.mzmine.data.PeakList;
import net.sf.mzmine.data.PeakListRow;
import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.data.impl.SimplePeakListRow;
import net.sf.mzmine.modules.rawdatamethods.deconvolutedanalysis.SpectrumType;
import net.sf.mzmine.taskcontrol.AbstractTask;
import net.sf.mzmine.taskcontrol.TaskStatus;
import net.sf.mzmine.util.Range;

import java.util.*;
import java.util.logging.Logger;

public class SpectraMatcherComparisonTask extends AbstractTask {
	// Logger
	private Logger LOG = Logger.getLogger(this.getClass().getName());

	// Collection of spectra processing tasks
	private List<SpectraMatcherProcessingTask> processingTasks;

	// Collection of mass candidates sorted by data file
	private Map<RawDataFile, List<MassCandidate>> massCandidates;

	// Final peak list
	private PeakList peakList;

	// Progress counters
	private int processedScans = 0;
	private int totalScans;

	// Time window in which to search for mass candidates
	private double timeWindow;

	// Requried number of matches for each ionization type
	int[] requiredMatches;

	public SpectraMatcherComparisonTask(
			List<SpectraMatcherProcessingTask> processingTasks,
			Map<RawDataFile, List<MassCandidate>> massCandidates,
			PeakList peakList) {
		this.processingTasks = processingTasks;
		this.massCandidates = massCandidates;
		this.peakList = peakList;

		// Get the match time window parameter
		// Converted to minutes
		timeWindow = SpectraMatcherParameters.MATCH_TIME_WINDOW.getValue() / 60.0;

		// Get the required matches parameters
		requiredMatches = new int[SpectrumType.values().length];
		for (int i = 0; i < requiredMatches.length; i++)
			requiredMatches[i] = SpectraMatcherParameters.FILE_MATCHES[i]
					.getValue();
	}

	@Override
	public String getTaskDescription() {
		return "Comparing detected masses from multiple ionization sources.";
	}

	@Override
	public double getFinishedPercentage() {
		return totalScans == 0 ? 0.0 : (double) processedScans
				/ (double) totalScans;
	}

	public void run() {
		// Wait until all other processing tasks have finished
		try {
			while (isBusy())
				Thread.sleep(250);
		} catch (Throwable t) {
		}

		// Canceled?
		if (isCanceled())
			return;

		// Update the status of this task
		setStatus(TaskStatus.PROCESSING);
		LOG.info("Started detected mass comparison.");

		// Set initial number of "scans"
		totalScans = massCandidates.size();

		// Find all existing mass ranges
		Map<Integer, Map<Range, List<MassCandidate>>> massRanges = new HashMap<Integer, Map<Range, List<MassCandidate>>>();

		for (List<MassCandidate> masses : massCandidates.values()) {
			for (MassCandidate m : masses) {
				int mass = m.getIonMass();
				Range range = getTimeWindow(m.getRetentionTime());

				// Create a new HashMap entry with a simple Range Comparator if
				// this is a new mass
				if (!massRanges.containsKey(mass)) {
					massRanges.put(mass,
							new TreeMap<Range, List<MassCandidate>>(
									new Comparator<Range>() {
										@Override
										public int compare(Range a, Range b) {
											if (a.getMin() < b.getMin())
												return -1;
											else if (a.getMin() > b.getMin())
												return 1;
											else
												return 0;
										}
									}));

					totalScans += 2;
				}

				// Otherwise, search through all ranges
				else {
					Map<Range, List<MassCandidate>> map = massRanges.get(mass);
					Iterator<Map.Entry<Range, List<MassCandidate>>> iter = map
							.entrySet().iterator();

					boolean matched = false;

					while (iter.hasNext()) {
						Map.Entry<Range, List<MassCandidate>> e = iter.next();

						// If the range overlaps, remove the old key, extend it,
						// and re-add the list with the new key
						if (rangeOverlaps(e.getKey(), range)) {
							List<MassCandidate> tmpList = e.getValue();
							tmpList.add(m);

							Range matchedRange = e.getKey();
							map.remove(e.getKey());

							matchedRange.extendRange(range);
							map.put(matchedRange, tmpList);

							matched = true;
							break;
						}
					}

					// If no match is found, add another range entry
					if (!matched) {
						List<MassCandidate> tmpList = new ArrayList<MassCandidate>();
						tmpList.add(m);

						massRanges.get(mass).put(range, tmpList);
					}
				}
			}

			processedScans++;
		}

		// Combine any possible overlapping ranges
		for (Map<Range, List<MassCandidate>> map : massRanges.values()) {
			Range[] keys = map.keySet().toArray(new Range[0]);

			for (int i = 0; i < keys.length - 1;) {
				if (rangeOverlaps(keys[i], keys[i + 1])) {
					List<MassCandidate> tmpList = map.get(keys[i]);
					tmpList.addAll(map.get(keys[i + 1]));

					Range tmpRange = new Range(keys[i]);
					tmpRange.extendRange(keys[i + 1]);

					map.remove(keys[i]);
					map.remove(keys[i + 1]);
					map.put(tmpRange, tmpList);

					keys = map.keySet().toArray(new Range[0]);
				} else
					i++;
			}

			processedScans++;
		}

		// Filter masses with insufficient matches
		Map<Double, List<MassCandidate>> matchedCandidates = new TreeMap<Double, List<MassCandidate>>();

		for (Map<Range, List<MassCandidate>> map : massRanges.values()) {
			for (Map.Entry<Range, List<MassCandidate>> e : map.entrySet()) {
				int[] count = new int[SpectrumType.values().length];
				double averageRT = 0;

				for (MassCandidate m : e.getValue()) {
					count[m.getIonizationType().ordinal()]++;
					averageRT += m.getRetentionTime();
				}

				averageRT /= e.getValue().size();

				// Check that each ionization type has sufficient matches
				boolean omit = false;

				for (int i = 0; i < count.length; i++) {
					if (count[i] < requiredMatches[i]) {
						omit = true;
						break;
					}
				}

				if (omit)
					continue;

				// If it passes this filter, add to the final results
				matchedCandidates.put(averageRT, e.getValue());
			}

			processedScans++;
		}

		// Create PeakList
		int id = 0;

		for (Double averageRT : matchedCandidates.keySet()) {
			List<MassCandidate> masses = matchedCandidates.get(averageRT);
			PeakListRow row = new SimplePeakListRow(id++);

			for (MassCandidate m : masses)
				row.addPeak(m.getDataFile(), m);

			peakList.addRow(row);
		}

		// If this task was canceled, stop processing
		if (!isCanceled()) {
			// Set task status to FINISHED
			setStatus(TaskStatus.FINISHED);

			LOG.info("Finished detected mass comparison.");
		}
	}

	private boolean isBusy() {
		// If the current task is cancelled, we are not busy
		if (isCanceled())
			return false;

		for (SpectraMatcherProcessingTask task : processingTasks) {
			// If a task is still working, we are still busy
			if (!task.isFinished())
				return true;
		}

		// If all tasks are finished, we are not busy
		return false;
	}

	private Range getTimeWindow(double rt) {
		return new Range(rt - timeWindow, rt + timeWindow);
	}

	private boolean rangeOverlaps(Range a, Range b) {
		return (a.getMin() <= b.getMax()) && (a.getMax() >= b.getMin());
	}
}
