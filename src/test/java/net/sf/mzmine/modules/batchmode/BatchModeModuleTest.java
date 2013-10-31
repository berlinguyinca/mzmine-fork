package net.sf.mzmine.modules.batchmode;

import junit.framework.Assert;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.util.ExitCode;
import org.junit.Test;

import java.io.File;
import static org.junit.Assert.assertTrue;


/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 10/30/13
 * Time: 4:15 PM
 */
public class BatchModeModuleTest {

    private BatchModeModule modeModule;

    @Test
    public void testRunBatchJustLoadFiles() throws Exception {

        MZmineCore.initializeHeadless();

        File batchFile = new File("src/test/resources/readFiles.xml");
        Assert.assertTrue(batchFile.exists());
        ExitCode code = BatchModeModule.runBatch(batchFile);

        assertTrue(code == ExitCode.OK);

    }

}
