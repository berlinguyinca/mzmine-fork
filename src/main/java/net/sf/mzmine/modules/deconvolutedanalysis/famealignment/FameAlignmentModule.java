package net.sf.mzmine.modules.deconvolutedanalysis.famealignment;

import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.desktop.impl.MainWindow;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.modules.MZmineModuleCategory;
import net.sf.mzmine.modules.MZmineProcessingModule;
import net.sf.mzmine.modules.deconvolutedanalysis.SpectrumType;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.taskcontrol.Task;
import net.sf.mzmine.util.ExitCode;

import javax.annotation.Nonnull;
import java.util.*;

public class FameAlignmentModule implements MZmineProcessingModule {
	private static final String MODULE_NAME = "Retention correction (FAME)";
	private static final String MODULE_DESCRIPTION = "This module aligns spectra from multiple ionization sources by the detection and matching of FAME markers.";

	@Override
	public @Nonnull
	String getName() {
		return MODULE_NAME;
	}

	@Override
	public @Nonnull
	String getDescription() {
		return MODULE_DESCRIPTION;
	}

	@Override
	public @Nonnull
	ExitCode runModule(@Nonnull ParameterSet parameters,
			@Nonnull Collection<Task> tasks) {

		// Keep a track of local processing tasks so we know when they finish
		List<FameAlignmentProcessingTask> processingTasks = new ArrayList<FameAlignmentProcessingTask>();

		// Search for FAME markers in each spectra file
		for (SpectrumType type : FameAlignmentParameters.SPECTRA_DATA.keySet()) {
			RawDataFile[] dataFiles = parameters.getParameter(
					FameAlignmentParameters.SPECTRA_DATA.get(type)).getValue();

			for (RawDataFile dataFile : dataFiles) {
				FameAlignmentProcessingTask task = new FameAlignmentProcessingTask(
						dataFile, parameters, type);
				processingTasks.add(task);
				tasks.add(task);
			}
		}

		// Display results if requested
		if (parameters.getParameter(FameAlignmentParameters.SHOW_RESULTS)
				.getValue() && MZmineCore.getDesktop() instanceof MainWindow) {
			// Start visualization task
			tasks.add(new FameAlignmentVisualizationTask(processingTasks));
		}

		return ExitCode.OK;
	}

	@Override
	public @Nonnull
	MZmineModuleCategory getModuleCategory() {
		return MZmineModuleCategory.DECONVOLUTEDANALYSIS;
	}

	@Override
	public @Nonnull
	Class<? extends ParameterSet> getParameterSetClass() {
		return FameAlignmentParameters.class;
	}
}
