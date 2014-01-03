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

import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.data.RawDataFileWriter;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.modules.MZmineModuleCategory;
import net.sf.mzmine.modules.MZmineProcessingModule;
import net.sf.mzmine.modules.rawdatamethods.rawdataimport.fileformats.*;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.taskcontrol.Task;
import net.sf.mzmine.taskcontrol.TaskEvent;
import net.sf.mzmine.taskcontrol.TaskListener;
import net.sf.mzmine.taskcontrol.TaskStatus;
import net.sf.mzmine.util.ExitCode;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Raw data import module
 */
public class RawDataEICompareImportModule
		implements
			MZmineProcessingModule,
			TaskListener {

	private Logger logger = Logger.getLogger(this.getClass().getName());

	private static final String MODULE_NAME = "Raw data import - EI/PCI mode";
	private static final String MODULE_DESCRIPTION = "This module imports raw EI and isobutane/methane PCI data into the project.";

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

	/**
	 * reads a single file
	 * 
	 * @param tasks
	 * @param fileName
	 * @return
	 */
	protected boolean readFile(Collection<Task> tasks, File fileName) {
		if ((!fileName.exists()) || (!fileName.canRead())) {
			MZmineCore.getDesktop().displayErrorMessage(
					"Cannot read file " + fileName);
			logger.warning("Cannot read file " + fileName);
			return true;
		}

		RawDataFileWriter newMZmineFile;
		try {
			newMZmineFile = MZmineCore.createNewFile(fileName.getName());
		} catch (IOException e) {
			MZmineCore.getDesktop().displayErrorMessage(
					"Could not create a new temporary file " + e);
			logger.log(Level.SEVERE, "Could not create a new temporary file ",
					e);
			return true;
		}

		String extension = fileName.getName()
				.substring(fileName.getName().lastIndexOf(".") + 1)
				.toLowerCase();
		Task newTask = null;

		if (extension.endsWith("mzdata")) {
			newTask = new MzDataReadTask(fileName, newMZmineFile);
		}
		if (extension.endsWith("mzxml")) {
			newTask = new MzXMLReadTask(fileName, newMZmineFile);
		}
		if (extension.endsWith("mzml")) {
			newTask = new MzMLReadTask(fileName, newMZmineFile);
		}
		if (extension.endsWith("cdf")) {
			newTask = new NetCDFReadTask(fileName, newMZmineFile);
		}
		if (extension.endsWith("raw")) {
			newTask = new XcaliburRawFileReadTask(fileName, newMZmineFile);
		}
		if (extension.endsWith("xml")) {

			try {
				// Check the first 512 bytes of the file, to determine the
				// file type
				FileReader reader = new FileReader(fileName);
				char buffer[] = new char[512];
				reader.read(buffer);
				reader.close();
				String fileHeader = new String(buffer);
				if (fileHeader.contains("mzXML")) {
					newTask = new MzXMLReadTask(fileName, newMZmineFile);
				}
				if (fileHeader.contains("mzData")) {
					newTask = new MzDataReadTask(fileName, newMZmineFile);
				}
				if (fileHeader.contains("mzML")) {
					newTask = new MzMLReadTask(fileName, newMZmineFile);
				}
			} catch (Exception e) {
				logger.warning("Cannot read file " + fileName + ": " + e);
				return true;
			}
		}

		if (extension.endsWith("csv")) {
			newTask = new AgilentCsvReadTask(fileName, newMZmineFile);
		}

		if (newTask == null) {
			logger.warning("Cannot determine file type of file " + fileName);
			return true;
		}

		newTask.addTaskListener(this);
		tasks.add(newTask);
		return false;
	}

	@Override
	@Nonnull
	public ExitCode runModule(@Nonnull ParameterSet parameters,
			@Nonnull Collection<Task> tasks) {

		File fileNames[] = parameters.getParameter(
				RawDataImportParameters.fileNames).getValue();

		for (File f : fileNames) {
			if (readFile(tasks, f))
				return ExitCode.ERROR;
		}

		return ExitCode.OK;
	}

	/**
	 * The statusChanged method of the TaskEvent interface
	 * 
	 * @param e
	 *            The TaskEvent which triggered this action
	 */
	@Override
	public void statusChanged(TaskEvent e) {
		if (e.getStatus() == TaskStatus.FINISHED) {
			MZmineCore.getCurrentProject().addFile(
					(RawDataFile) e.getSource().getCreatedObjects()[0]);
		}

	}

	@Override
	public @Nonnull
	MZmineModuleCategory getModuleCategory() {
		return MZmineModuleCategory.RAWDATA;
	}

	@Override
	public @Nonnull
	Class<? extends ParameterSet> getParameterSetClass() {
		return RawDataImportParameters.class;
	}

}
