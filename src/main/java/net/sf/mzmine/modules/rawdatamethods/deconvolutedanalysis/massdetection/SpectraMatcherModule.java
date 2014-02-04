package net.sf.mzmine.modules.rawdatamethods.deconvolutedanalysis.massdetection;

import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.modules.MZmineModuleCategory;
import net.sf.mzmine.modules.MZmineProcessingModule;
import net.sf.mzmine.modules.rawdatamethods.deconvolutedanalysis.massdetection.table.MassListTableWindow;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.taskcontrol.Task;
import net.sf.mzmine.util.ExitCode;

import javax.annotation.Nonnull;
import java.util.*;

public class SpectraMatcherModule implements MZmineProcessingModule {

    private static final String MODULE_NAME = "Multi-ionization mass detector";
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

        // Keep a track of local processing tasks so we can determine when they all finish
        List<SpectraMatcherProcessingTask> processingTasks = new ArrayList<SpectraMatcherProcessingTask>();

        // Store mass candidates by filename
        Map<RawDataFile, List<MassCandidate>> massCandidatesByFile = new HashMap<RawDataFile, List<MassCandidate>>();

        for (SpectrumType i : SpectrumType.values()) {
            RawDataFile[] dataFiles = SpectraMatcherParameters.SPECTRA_DATA[i.ordinal()].getValue();

            for (RawDataFile dataFile : dataFiles) {
                List<MassCandidate> spectralMasses = new ArrayList<MassCandidate>();

                SpectraMatcherProcessingTask newTask = new SpectraMatcherProcessingTask(dataFile, i, spectralMasses);
                tasks.add(newTask);
                processingTasks.add(newTask);

                massCandidatesByFile.put(dataFile, spectralMasses);
            }
        }

        // Start the comparison task to filter and sort the candidate masses
        Map<Double, List<MassCandidate>> matchedCandidates = new TreeMap<Double, List<MassCandidate>>();
        SpectraMatcherComparisonTask comparisonTask = new SpectraMatcherComparisonTask(processingTasks, massCandidatesByFile, matchedCandidates);
        tasks.add(comparisonTask);

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
