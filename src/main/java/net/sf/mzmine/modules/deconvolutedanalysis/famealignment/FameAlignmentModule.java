package net.sf.mzmine.modules.deconvolutedanalysis.famealignment;

import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.modules.MZmineModuleCategory;
import net.sf.mzmine.modules.MZmineProcessingModule;
import net.sf.mzmine.modules.deconvolutedanalysis.SpectrumType;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.taskcontrol.Task;
import net.sf.mzmine.util.ExitCode;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.logging.Logger;

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
		List<FameAlignmentProcessingTask> processingTasks = new ArrayList<FameAlignmentProcessingTask>();

		Logger logger = Logger.getLogger(this.getClass().getName());
		logger.info(String.valueOf(FameAlignmentParameters.SPECTRA_DATA[0].getValue().length));
		logger.info(String.valueOf(FameAlignmentParameters.SPECTRA_DATA[1].getValue().length));
		logger.info(String.valueOf(FameAlignmentParameters.SPECTRA_DATA[2].getValue().length));

		// Search for FAME markers in each spectra file
		for (SpectrumType i : SpectrumType.values()) {
			RawDataFile[] dataFiles = FameAlignmentParameters.SPECTRA_DATA[i
					.ordinal()].getValue();

			for (RawDataFile dataFile : dataFiles) {
				FameAlignmentProcessingTask task = new FameAlignmentProcessingTask(
						dataFile, i);
				processingTasks.add(task);
				tasks.add(task);
			}
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
