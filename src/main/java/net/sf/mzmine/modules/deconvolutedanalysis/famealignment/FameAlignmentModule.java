package net.sf.mzmine.modules.deconvolutedanalysis.famealignment;

import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.modules.MZmineModuleCategory;
import net.sf.mzmine.modules.MZmineProcessingModule;
import net.sf.mzmine.modules.deconvolutedanalysis.SpectrumType;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.taskcontrol.AbstractTask;
import net.sf.mzmine.taskcontrol.Task;
import net.sf.mzmine.util.ExitCode;

import javax.annotation.Nonnull;
import java.util.*;

public class FameAlignmentModule implements MZmineProcessingModule {
	private static final String MODULE_NAME = "Automated FAME alignment";
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
	@Nonnull
	public ExitCode runModule(@Nonnull ParameterSet parameters,
			@Nonnull Collection<Task> tasks) {

		// Keep a track of local processing tasks so we can determine when they
		// all finish
		List<AbstractTask> processingTasks = new ArrayList<AbstractTask>();

		// Keep a track of correction results
		List<Map<String, Correction>> correctionTable = new ArrayList<Map<String, Correction>>();

		// Search for FAME markers in each spectra file
		for (SpectrumType type : FameAlignmentParameters.SPECTRA_DATA.keySet()) {
			RawDataFile[] dataFiles = parameters.getParameter(
					FameAlignmentParameters.SPECTRA_DATA.get(type)).getValue();

			for (RawDataFile dataFile : dataFiles) {
				FameAlignmentProcessingTask task = new FameAlignmentProcessingTask(
						dataFile, parameters, type, correctionTable);
				processingTasks.add(task);
				tasks.add(task);
			}
		}

		// Display results if requested
		if (parameters.getParameter(FameAlignmentParameters.SHOW_RESULTS)
				.getValue()) {
			// Create PeakList with ordered files
			List<RawDataFile> dataFiles = new ArrayList<RawDataFile>();
			for (SpectrumType type : FameAlignmentParameters.SPECTRA_DATA
					.keySet()) {
				RawDataFile[] files = parameters.getParameter(
						FameAlignmentParameters.SPECTRA_DATA.get(type))
						.getValue();

				Arrays.sort(files, new Comparator<RawDataFile>() {
					@Override
					public int compare(RawDataFile a, RawDataFile b) {
						return a.getName().compareTo(b.getName());
					}
				});
				dataFiles.addAll(Arrays.asList(files));
			}

			// Start visualization task
			FameAlignmentVisualizationTask visualizationTask = new FameAlignmentVisualizationTask(
					dataFiles, correctionTable, processingTasks);
			tasks.add(visualizationTask);
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
