package net.sf.mzmine.modules.deconvolutedanalysis.massdetection;

import net.sf.mzmine.data.PeakList;
import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.data.impl.SimplePeakList;
import net.sf.mzmine.modules.MZmineModuleCategory;
import net.sf.mzmine.modules.MZmineProcessingModule;
import net.sf.mzmine.modules.deconvolutedanalysis.SpectrumType;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.taskcontrol.Task;
import net.sf.mzmine.util.ExitCode;

import javax.annotation.Nonnull;
import java.util.*;

public class SpectraMatcherModule implements MZmineProcessingModule {

	private static final String MODULE_NAME = "Automated mass detection";
	private static final String MODULE_DESCRIPTION = "This module compares spectra from multiple ionization sources and detects candidate masses.";

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
		List<SpectraMatcherProcessingTask> processingTasks = new ArrayList<SpectraMatcherProcessingTask>();

		// Store mass candidates by filename
		Map<RawDataFile, List<MassCandidate>> massCandidatesByFile = new HashMap<RawDataFile, List<MassCandidate>>();

		for (SpectrumType i : SpectrumType.values()) {
			RawDataFile[] dataFiles = parameters.getParameter(SpectraMatcherParameters.SPECTRA_DATA[i
					.ordinal()]).getValue();

			for (RawDataFile dataFile : dataFiles) {
				List<MassCandidate> spectralMasses = new ArrayList<MassCandidate>();

				SpectraMatcherProcessingTask newTask = new SpectraMatcherProcessingTask(
						dataFile, parameters, i, spectralMasses);
				tasks.add(newTask);
				processingTasks.add(newTask);

				massCandidatesByFile.put(dataFile, spectralMasses);
			}
		}

		// Create PeakList with ordered files
		List<RawDataFile> dataFiles = new ArrayList<RawDataFile>();
		for (SpectrumType i : SpectrumType.values()) {
			RawDataFile[] files = parameters.getParameter(SpectraMatcherParameters.SPECTRA_DATA[i.ordinal()]).getValue();
			Arrays.sort(files, new Comparator<RawDataFile>() {
				@Override
				public int compare(RawDataFile a, RawDataFile b) {
					return a.getName().compareTo(b.getName());
				}
			});
			dataFiles.addAll(Arrays.asList(files));
		}

		// Produce new PeakList
		PeakList peakList = new SimplePeakList("Mass Candidates",
				dataFiles.toArray(new RawDataFile[dataFiles.size()]));

		// Start the comparison task to filter and sort the candidate masses
		SpectraMatcherComparisonTask comparisonTask = new SpectraMatcherComparisonTask(
				processingTasks, parameters, massCandidatesByFile, peakList);
		tasks.add(comparisonTask);

		// Start visualization task
		SpectraMatcherVisualizationTask visualizationTask = new SpectraMatcherVisualizationTask(
				peakList, comparisonTask);
		tasks.add(visualizationTask);

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
		return SpectraMatcherParameters.class;
	}

}
