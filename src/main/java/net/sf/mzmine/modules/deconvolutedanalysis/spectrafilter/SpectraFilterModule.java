package net.sf.mzmine.modules.deconvolutedanalysis.spectrafilter;

import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.modules.MZmineModuleCategory;
import net.sf.mzmine.modules.MZmineProcessingModule;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.taskcontrol.Task;
import net.sf.mzmine.util.ExitCode;

import javax.annotation.Nonnull;
import java.util.Collection;

public class SpectraFilterModule implements MZmineProcessingModule {
	private static final String MODULE_NAME = "Spectra filter";
	private static final String MODULE_DESCRIPTION = "This module applies a C13 isotope filter, and base peak/unique mass threshold, noise threshold and base peak percentage cuts.";

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

		// Get all selected files
		RawDataFile[] dataFiles = parameters.getParameter(
				SpectraFilterParameters.DATA_FILES).getValue();

		// Create a new task for each file to be filtered
		for (final RawDataFile dataFile : dataFiles) {
			Task newTask = new SpectraFilterTask(dataFile, parameters);
			tasks.add(newTask);
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
		return SpectraFilterParameters.class;
	}
}
