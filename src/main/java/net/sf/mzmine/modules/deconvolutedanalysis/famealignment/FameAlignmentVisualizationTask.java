package net.sf.mzmine.modules.deconvolutedanalysis.famealignment;

import net.sf.mzmine.data.PeakList;
import net.sf.mzmine.data.PeakListRow;
import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.data.impl.SimplePeakList;
import net.sf.mzmine.data.impl.SimplePeakListRow;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.modules.deconvolutedanalysis.famealignment.table.ResultsListTableWindow;
import net.sf.mzmine.taskcontrol.AbstractTask;
import net.sf.mzmine.taskcontrol.TaskStatus;

import java.util.*;
import java.util.logging.Logger;

public class FameAlignmentVisualizationTask extends AbstractTask {
	// Logger
	private Logger LOG = Logger.getLogger(this.getClass().getName());

	// Comparison task to wait for
	List<FameAlignmentProcessingTask> processingTasks;

	public FameAlignmentVisualizationTask(
			List<FameAlignmentProcessingTask> processingTasks) {

		this.processingTasks = processingTasks;
	}

	@Override
	public String getTaskDescription() {
		return "Building table of detected masses.";
	}

	@Override
	public double getFinishedPercentage() {
		return 0.0;
	}

	public void run() {
		// Update the status of this task
		setStatus(TaskStatus.PROCESSING);
		LOG.info("Building table of detected masses.");

		// Wait until the comparison task is finished, then open the mass table

		while (isBusy()) {
			try {
				Thread.sleep(250);
			} catch (Exception e) {
			}
		}

		// Canceled?
		if (isCanceled())
			return;

		// Retrieve corrected data files
		List<RawDataFile> dataFiles = new ArrayList<RawDataFile>();

		for (FameAlignmentProcessingTask task : processingTasks)
			dataFiles.add(task.getCorrectedDataFile());

		Collections.sort(dataFiles, new Comparator<RawDataFile>() {
			@Override
			public int compare(RawDataFile a, RawDataFile b) {
				return a.getName().compareTo(b.getName());
			}
		});

		// Produce new PeakList
		PeakList peakList = new SimplePeakList(
				"Retention Index Correction Results",
				dataFiles.toArray(new RawDataFile[dataFiles.size()]));

		// Create PeakListRows
		int id = 0;

		for (String name : FameData.FAME_NAMES) {
			PeakListRow row = new SimplePeakListRow(++id);
			row.setComment(name);

			for (FameAlignmentProcessingTask task : processingTasks) {
				if (task.getResults().containsKey(name))
					row.addPeak(task.getResults().get(name).getDataFile(), task
							.getResults().get(name));
			}

			peakList.addRow(row);
		}

		// Add table to the GUI
		ResultsListTableWindow window = new ResultsListTableWindow(peakList);
		MZmineCore.getDesktop().addInternalFrame(window);

		// If this task was canceled, stop processing
		if (!isCanceled()) {
			// Set task status to FINISHED
			setStatus(TaskStatus.FINISHED);

			LOG.info("Finished building table of detected masses.");
		}
	}

	/**
	 * Checks whether any of the processing tasks are still running.
	 * 
	 * @return whether the processing tasks are still busy
	 */
	private boolean isBusy() {
		// If the current task is cancelled, we are not busy
		if (isCanceled())
			return false;

		for (FameAlignmentProcessingTask task : processingTasks) {
			// If a task is still working, we are still busy
			if (!task.isFinished())
				return true;
		}

		// If all tasks are finished, we are not busy
		return false;
	}
}
