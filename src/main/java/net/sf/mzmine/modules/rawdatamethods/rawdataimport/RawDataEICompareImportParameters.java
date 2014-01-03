/*
 * Copyright 2006-2012 The MZmine 2 Development Team
 * 
 * This file is part of MZmine 2.
 * 
 * MZmine 2 is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * MZmine 2 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * MZmine 2; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */

package net.sf.mzmine.modules.rawdatamethods.rawdataimport;

import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.parameters.Parameter;
import net.sf.mzmine.parameters.impl.SimpleParameterSet;
import net.sf.mzmine.util.ExitCode;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

public class RawDataEICompareImportParameters extends SimpleParameterSet {

	protected static final FileFilter filters[] = new FileFilter[]{
			new FileNameExtensionFilter("All raw data files", "cdf", "nc",
					"mzData", "mzML", "mzXML", "xml", "raw", "csv"),
			new FileNameExtensionFilter("All XML files", "xml"),
			new FileNameExtensionFilter("NetCDF files", "cdf", "nc"),
			new FileNameExtensionFilter("mzData files", "mzData"),
			new FileNameExtensionFilter("mzML files", "mzML"),
			new FileNameExtensionFilter("XCalibur RAW files", "raw"),
			new FileNameExtensionFilter("mzXML files", "mzXML"),
			new FileNameExtensionFilter("Agilent CSV files", "csv")};

	public static final FileNamesParameter fileNames = new FileNamesParameter();

	public RawDataEICompareImportParameters() {
		super(new Parameter[]{fileNames});
	}

	public ExitCode showSetupDialog() {
		File eiFile = showOpenDialog("Open EI spectrum file"), isobutanePCIFile, methanePCIFile;
		
		if(eiFile == null) return ExitCode.CANCEL;
		else isobutanePCIFile = showOpenDialog("Open isobutane PCI spectrum file");
		
		if(isobutanePCIFile == null) return ExitCode.CANCEL;
		else methanePCIFile = showOpenDialog("Open methane PCI spectrum file");
		
		getParameter(fileNames).setValue(new File[] {eiFile, isobutanePCIFile, methanePCIFile});
		return methanePCIFile == null ? ExitCode.CANCEL : ExitCode.OK;
	}
	
	private File showOpenDialog(String message) {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(message);
		chooser.setMultiSelectionEnabled(false);

		for (FileFilter filter : filters)
			chooser.setFileFilter(filter);
		chooser.setFileFilter(filters[0]);
		
		File lastFiles[] = getParameter(fileNames).getValue();
		if ((lastFiles != null) && (lastFiles.length > 0)) {
			File currentDir = lastFiles[0].getParentFile();
			if ((currentDir != null) && (currentDir.exists()))
				chooser.setCurrentDirectory(currentDir);
		}
		
		
		int returnVal = chooser.showOpenDialog(MZmineCore.getDesktop().getMainFrame());
		
		if(returnVal != JFileChooser.APPROVE_OPTION)
			return null;
		else
			return chooser.getSelectedFile();
	}

}
