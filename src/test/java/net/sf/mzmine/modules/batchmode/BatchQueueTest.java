package net.sf.mzmine.modules.batchmode;

import net.sf.mzmine.main.MZmineCore;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 10/30/13
 * Time: 4:24 PM
 */
public class BatchQueueTest {
    @Test
    public void testLoadFromXml() throws Exception {

        MZmineCore.initializeHeadless();

        File batchFile = new File("src/test/resources/readFiles.xml");

        DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder();
        Document parsedBatchXML = docBuilder.parse(batchFile);
        BatchQueue newQueue = BatchQueue.loadFromXml(parsedBatchXML
                .getDocumentElement());


        assertTrue(newQueue.isEmpty() == false);
        assertTrue(newQueue.size() == 1);


    }
}
