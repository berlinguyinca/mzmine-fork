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

/* Code created was by or on behalf of Syngenta and is released under the open source license in use for the
 * pre-existing code or project. Syngenta does not assert ownership or copyright any over pre-existing work.
 */

package net.sf.mzmine.modules.rawdatamethods.filtering.baselinecorrection;

import java.util.Collection;

import javax.annotation.Nonnull;

import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.modules.MZmineModuleCategory;
import net.sf.mzmine.modules.MZmineProcessingModule;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.taskcontrol.Task;
import net.sf.mzmine.util.ExitCode;

/**
 * Baseline correction module.
 * 
 * @version $Revision: 3218 $
 */
public class BaselineCorrectionModule implements MZmineProcessingModule {

	private static final String MODULE_NAME = "Baseline correction";
	private static final String MODULE_DESCRIPTION = "This module performs a baseline correction on raw data files.";

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

		RawDataFile dataFiles[] = parameters.getParameter(
				BaselineCorrectionParameters.dataFiles).getValue();

		for (final RawDataFile dataFile : dataFiles) {

			Task newTask = new BaselineCorrectionTask(dataFile, parameters);
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
		return BaselineCorrectionParameters.class;
	}

}
