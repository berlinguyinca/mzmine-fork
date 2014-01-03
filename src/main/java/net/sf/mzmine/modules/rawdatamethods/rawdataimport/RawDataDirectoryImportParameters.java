package net.sf.mzmine.modules.rawdatamethods.rawdataimport;

import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.util.ExitCode;

import javax.swing.*;
import java.io.File;
import java.io.FileFilter;

/**
 * Created with IntelliJ IDEA. User: wohlgemuth Date: 10/30/13 Time: 6:07 PM
 */
public class RawDataDirectoryImportParameters extends RawDataImportParameters {

	/**
	 * shows a dialog, which only accept's directories and verifies that all the
	 * files in this directory are acceptable
	 * 
	 * @return
	 */
	public ExitCode showSetupDialog() {

		JFileChooser chooser = new JFileChooser();

		File lastFiles[] = getParameter(fileNames).getValue();
		if ((lastFiles != null) && (lastFiles.length > 0)) {
			File currentDir = lastFiles[0].getParentFile();
			if ((currentDir != null) && (currentDir.exists()))
				chooser.setCurrentDirectory(currentDir);
		}

		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		int returnVal = chooser.showOpenDialog(MZmineCore.getDesktop()
				.getMainFrame());

		if (returnVal != JFileChooser.APPROVE_OPTION)
			return ExitCode.CANCEL;

		File file = chooser.getSelectedFile();

		if (file.isDirectory()) {

			getParameter(fileNames).setValue(file.listFiles(getFileFilter()));
		}
		return ExitCode.OK;

	}

	/**
	 * file filter to use for our method
	 * 
	 * @return
	 */
	public static FileFilter getFileFilter() {
		return new FileFilter() {
			@Override
			public boolean accept(File file) {
				for (javax.swing.filechooser.FileFilter filter : filters) {
					if (filter.accept(file)) {
						return true;
					}
				}
				return false;
			}
		};
	}

}
