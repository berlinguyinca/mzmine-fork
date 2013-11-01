package net.sf.mzmine.modules.rawdatamethods.rawdataimport;

import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.taskcontrol.Task;
import net.sf.mzmine.util.ExitCode;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA. User: wohlgemuth Date: 10/30/13 Time: 3:56 PM
 * <p/>
 * imports all the data from a specified directory into mzMine
 */
public class RawDataDirectoryImportModule extends RawDataImportModule {

    private Logger logger = Logger.getLogger(getClass().getName());

    private static final String MODULE_DESCRIPTION = "This module imports raw data into the project and allows you to specify a whole directory at once.";

    private static final String MODULE_NAME = "Raw data import - Directory mode";

    @Override
    public
    @Nonnull
    String getName() {
        return MODULE_NAME;
    }

    @Override
    public
    @Nonnull
    String getDescription() {
        return MODULE_DESCRIPTION;
    }

    @Override
    @Nonnull
    public ExitCode runModule(@Nonnull ParameterSet parameters,
                              @Nonnull Collection<Task> tasks) {

        File fileNames[] = parameters.getParameter(
                RawdataImportDirectoryParameters.fileNames).getValue();

        for (File d : fileNames) {

            if (d.isDirectory()) {
                logger.info("working on directory: " + d);
                File[] files = d.listFiles(RawdataImportDirectoryParameters
                        .getFileFilter());
                for (File f : files) {
                    if (readFile(tasks, f))
                        return ExitCode.ERROR;
                }
            } else {
                if (readFile(tasks, d))
                    return ExitCode.ERROR;
            }
        }

        return ExitCode.OK;
    }

    @Override
    public
    @Nonnull
    Class<? extends ParameterSet> getParameterSetClass() {
        return RawdataImportDirectoryParameters.class;
    }

}