package net.sf.mzmine.modules.rawdatamethods.deconvolutedspectrafilter;

import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.modules.MZmineModuleCategory;
import net.sf.mzmine.modules.MZmineProcessingModule;
import net.sf.mzmine.modules.masslistmethods.spectramatcher.SpectraMatcherParameters;
import net.sf.mzmine.modules.rawdatamethods.rawdataimport.fileformats.DeconvolutedCsvReadTask;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.taskcontrol.Task;
import net.sf.mzmine.util.ExitCode;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Created by sajjan on 1/27/14.
 */
public class DeconvolutedSpectraFilterModule implements MZmineProcessingModule {
    private static final String MODULE_NAME = "Deconvoluted Spectra Filter";
    private static final String MODULE_DESCRIPTION = "This module filters deconvoluted spectra through C13 isotope, noise threshold and base peak percentage cuts.";

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

        RawDataFile[] dataFiles = parameters.getParameter(
                DeconvolutedSpectraFilterParameters.dataFiles).getValue();

        for (final RawDataFile dataFile : dataFiles) {
            Task newTask = new DeconvolutedSpectraFilterTask(dataFile, parameters);
            tasks.add(newTask);
        }

        return ExitCode.OK;
    }

    @Override
    public @Nonnull
    MZmineModuleCategory getModuleCategory() {
        return MZmineModuleCategory.RAWDATAFILTERING;
    }

    @Override
    public @Nonnull
    Class<? extends ParameterSet> getParameterSetClass() {
        return DeconvolutedSpectraFilterParameters.class;
    }
}
