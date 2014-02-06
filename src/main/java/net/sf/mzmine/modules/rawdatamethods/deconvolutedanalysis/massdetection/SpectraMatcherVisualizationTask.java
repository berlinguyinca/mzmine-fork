package net.sf.mzmine.modules.rawdatamethods.deconvolutedanalysis.massdetection;

import net.sf.mzmine.data.DataPoint;
import net.sf.mzmine.data.PeakList;
import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.data.Scan;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.modules.peaklistmethods.identification.adductsearch.AdductType;
import net.sf.mzmine.modules.rawdatamethods.deconvolutedanalysis.massdetection.table.MassListTableWindow;
import net.sf.mzmine.taskcontrol.AbstractTask;
import net.sf.mzmine.taskcontrol.Task;
import net.sf.mzmine.taskcontrol.TaskStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class SpectraMatcherVisualizationTask extends AbstractTask {
	// Logger
	private Logger LOG = Logger.getLogger(this.getClass().getName());

	// Final peak list
	private PeakList peakList;

	// Comparison task to wait for
	AbstractTask comparisonTask;

	public SpectraMatcherVisualizationTask(PeakList peakList,
			AbstractTask comparisonTask) {
		this.peakList = peakList;
		this.comparisonTask = comparisonTask;
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

		while (!comparisonTask.isFinished())
			try {
				Thread.sleep(250);
			} catch (Exception e) {
			}

		// Add table to the GUI
		MassListTableWindow window = new MassListTableWindow(peakList);
		MZmineCore.getDesktop().addInternalFrame(window);

		// If this task was canceled, stop processing
		if (!isCanceled()) {
			// Set task status to FINISHED
			setStatus(TaskStatus.FINISHED);

			LOG.info("Finished building table of detected masses.");
		}
	}
}
