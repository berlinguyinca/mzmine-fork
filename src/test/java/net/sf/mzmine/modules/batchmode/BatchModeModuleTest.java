package net.sf.mzmine.modules.batchmode;

import junit.framework.Assert;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.util.ExitCode;
import org.junit.Test;

import java.io.File;
import java.util.Scanner;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA. User: wohlgemuth Date: 10/30/13 Time: 4:15 PM
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

	@Test
	public void testRunBatchLoadAllFilesFromDirectory() throws Exception {

		MZmineCore.initializeHeadless();

		File batchFile = new File("src/test/resources/readDirectory.xml");
		Assert.assertTrue(batchFile.exists());
		ExitCode code = BatchModeModule.runBatch(batchFile);

		assertTrue(code == ExitCode.OK);

	}

	@Test
	public void testRunBatchLoadAllFilesFromDirectoryWithRowFilterAndAlignment()
			throws Exception {

		MZmineCore.initializeHeadless();

		File batchFile = new File(
				"src/test/resources/readDirectoryWithRowFilterAndAlignment.xml");
		Assert.assertTrue(batchFile.exists());
		ExitCode code = BatchModeModule.runBatch(batchFile);

		System.out.println("result code: " + code);
		assertTrue(code == ExitCode.OK);

	}

	@Test
	public void testRunBatchLoadAllFilesFromDirectoryWithLibraryIdentification()
			throws Exception {

		MZmineCore.initializeHeadless();

		File batchFile = new File(
				"src/test/resources/readDirectoryWithLibraryIdentification.xml");
		Assert.assertTrue(batchFile.exists());
		ExitCode code = BatchModeModule.runBatch(batchFile);

		System.out.println("result code: " + code);
		assertTrue(code == ExitCode.OK);

	}

	@Test
	public void testRunBatchExportLibraryIdentification() throws Exception {

		MZmineCore.initializeHeadless();

		File batchFile = new File("src/test/resources/exportData.xml");
		Assert.assertTrue(batchFile.exists());
		ExitCode code = BatchModeModule.runBatch(batchFile);

		System.out.println("result code: " + code);
		assertTrue(code == ExitCode.OK);

		Scanner scanner = new Scanner(new File("target/export-result.csv"));

		assertTrue(scanner.hasNextLine());

		while (scanner.hasNextLine()) {
			String[] columns = scanner.nextLine().split(",");

			assertTrue(columns.length == 17);
		}
	}

	@Test
	public void testRunBatchExportPosLibraryIdentification() throws Exception {

		MZmineCore.initializeHeadless();

		File batchFile = new File("src/test/resources/exportPosData.xml");
		Assert.assertTrue(batchFile.exists());
		ExitCode code = BatchModeModule.runBatch(batchFile);

		System.out.println("result code: " + code);
		assertTrue(code == ExitCode.OK);

		Scanner scanner = new Scanner(new File("target/export-result-pos.csv"));

		assertTrue(scanner.hasNextLine());

		while (scanner.hasNextLine()) {
			String[] columns = scanner.nextLine().split(",");

			assertTrue(columns.length == 20);
		}
	}

}
