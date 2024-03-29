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

package net.sf.mzmine.modules.peaklistmethods.filtering.rowsfilter;

import java.util.Collection;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import net.sf.mzmine.data.PeakList;
import net.sf.mzmine.modules.MZmineModuleCategory;
import net.sf.mzmine.modules.MZmineProcessingModule;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.taskcontrol.Task;
import net.sf.mzmine.util.ExitCode;

/**
 * Implements a filter for alignment results. The filter removes rows that have
 * fewer than a defined number of peaks detected and other conditions.
 */
public class RowsFilterModule implements MZmineProcessingModule {

	private Logger logger = Logger.getLogger(getName());

	private static final String MODULE_NAME = "Peak list rows filter";
	private static final String MODULE_DESCRIPTION = "This method removes certain entries for a peak list based on given restrictions.";

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

		final PeakList[] peakLists = parameters.getParameter(
				RowsFilterParameters.PEAK_LISTS).getValue();
		logger.info("registered peakLists: " + peakLists);

		for (PeakList peakList : peakLists) {
			logger.info("working on peakList: " + peakList);

			Task newTask = new RowsFilterTask(peakList, parameters);
			tasks.add(newTask);

		}
		logger.info("finished...");

		return ExitCode.OK;
	}

	@Override
	public @Nonnull
	MZmineModuleCategory getModuleCategory() {
		return MZmineModuleCategory.PEAKLISTFILTERING;
	}

	@Override
	public @Nonnull
	Class<? extends ParameterSet> getParameterSetClass() {
		return RowsFilterParameters.class;
	}
}