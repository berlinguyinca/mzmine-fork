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

package net.sf.mzmine.modules.peaklistmethods.filtering.duplicatefilter;

import net.sf.mzmine.data.ChromatographicPeak;
import net.sf.mzmine.data.PeakList;
import net.sf.mzmine.data.PeakListAppliedMethod;
import net.sf.mzmine.data.PeakListRow;
import net.sf.mzmine.data.impl.SimpleChromatographicPeak;
import net.sf.mzmine.data.impl.SimplePeakList;
import net.sf.mzmine.data.impl.SimplePeakListAppliedMethod;
import net.sf.mzmine.data.impl.SimplePeakListRow;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.parameters.parametertypes.MZTolerance;
import net.sf.mzmine.parameters.parametertypes.RTTolerance;
import net.sf.mzmine.project.MZmineProject;
import net.sf.mzmine.taskcontrol.AbstractTask;
import net.sf.mzmine.taskcontrol.TaskStatus;
import net.sf.mzmine.util.PeakListRowSorter;
import net.sf.mzmine.util.PeakUtils;
import net.sf.mzmine.util.SortingDirection;
import net.sf.mzmine.util.SortingProperty;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A task to filter out duplicate peak list rows.
 */
public class DuplicateFilterTask extends AbstractTask {

	// Logger.
	private static final Logger LOG = Logger
			.getLogger(DuplicateFilterTask.class.getName());

	// Original and resultant peak lists.
	private final PeakList peakList;
	private PeakList filteredPeakList;

	// Counters.
	private int processedRows;
	private int totalRows;

	// Parameters.
	private final ParameterSet parameters;

	public DuplicateFilterTask(final PeakList list, final ParameterSet params) {

		// Initialize.
		parameters = params;
		peakList = list;
		filteredPeakList = null;
		totalRows = 0;
		processedRows = 0;
	}

	@Override
	public String getTaskDescription() {

		return "Filtering duplicate peak list rows of " + peakList;
	}

	@Override
	public double getFinishedPercentage() {

		return totalRows == 0 ? 0.0 : (double) processedRows
				/ (double) totalRows;
	}

	@Override
	public Object[] getCreatedObjects() {

		return new Object[]{filteredPeakList};
	}

	@Override
	public void run() {

		if (!isCanceled()) {
			try {

				LOG.info("Filtering duplicate peaks list rows of " + peakList);
				setStatus(TaskStatus.PROCESSING);

				// Filter out duplicates..
				filteredPeakList = filterDuplicatePeakListRows(
						peakList,
						parameters.getParameter(
								DuplicateFilterParameters.suffix).getValue(),
						parameters.getParameter(
								DuplicateFilterParameters.mzDifferenceMax)
								.getValue(),
						parameters.getParameter(
								DuplicateFilterParameters.rtDifferenceMax)
								.getValue(),
						parameters
								.getParameter(
										DuplicateFilterParameters.requireSameIdentification)
								.getValue());

				if (!isCanceled()) {

					// Add new peakList to the project.
					final MZmineProject project = MZmineCore
							.getCurrentProject();
					project.addPeakList(filteredPeakList);

					// Remove the original peakList if requested.
					if (parameters.getParameter(
							DuplicateFilterParameters.autoRemove).getValue()) {

						project.removePeakList(peakList);
					}

					// Finished.
					LOG.info("Finished filtering duplicate peak list rows on "
							+ peakList);
					setStatus(TaskStatus.FINISHED);
				}
			} catch (Throwable t) {

				LOG.log(Level.SEVERE, "Duplicate filter error", t);
				errorMessage = t.getMessage();
				setStatus(TaskStatus.ERROR);
			}
		}
	}

	/**
	 * Filter our duplicate peak list rows.
	 * 
	 * @param origPeakList
	 *            the original peak list.
	 * @param suffix
	 *            the suffix to apply to the new peak list name.
	 * @param mzTolerance
	 *            m/z tolerance.
	 * @param rtTolerance
	 *            RT tolerance.
	 * @param requireSameId
	 *            must duplicate peaks have the same identities?
	 * @return the filtered peak list.
	 */
	private PeakList filterDuplicatePeakListRows(final PeakList origPeakList,
			final String suffix, final MZTolerance mzTolerance,
			final RTTolerance rtTolerance, final boolean requireSameId) {

		final PeakListRow[] peakListRows = origPeakList.getRows();
		final int rowCount = peakListRows.length;

		Arrays.sort(peakListRows, new PeakListRowSorter(SortingProperty.Area,
				SortingDirection.Descending));

		// Loop through all peak list rows
		processedRows = 0;
		totalRows = rowCount;
		for (int firstRowIndex = 0; !isCanceled() && firstRowIndex < rowCount; firstRowIndex++) {

			final PeakListRow firstRow = peakListRows[firstRowIndex];
			if (firstRow != null) {

				for (int secondRowIndex = firstRowIndex + 1; !isCanceled()
						&& secondRowIndex < rowCount; secondRowIndex++) {

					final PeakListRow secondRow = peakListRows[secondRowIndex];
					if (secondRow != null) {

						// Compare identifications
						final boolean sameID = !requireSameId
								|| PeakUtils.compareIdentities(firstRow,
										secondRow);

						// Compare m/z
						final boolean sameMZ = mzTolerance.getToleranceRange(
								firstRow.getAverageMZ()).contains(
								secondRow.getAverageMZ());

						// Compare rt
						final boolean sameRT = rtTolerance.getToleranceRange(
								firstRow.getAverageRT()).contains(
								secondRow.getAverageRT());

						// Duplicate peaks?
						if (sameID && sameMZ && sameRT) {

							peakListRows[secondRowIndex] = null;
						}
					}
				}
			}

			processedRows++;
		}

		// Create the new peak list.
		final PeakList newPeakList = new SimplePeakList(origPeakList + " "
				+ suffix, origPeakList.getRawDataFiles());

		// Add all remaining rows to a new peak list.
		for (int i = 0; !isCanceled() && i < rowCount; i++) {

			final PeakListRow row = peakListRows[i];

			if (row != null) {

				// Copy the peak list row.
				final PeakListRow newRow = new SimplePeakListRow(row.getID());
				PeakUtils.copyPeakListRowProperties(row, newRow);

				// Copy the peaks.
				for (final ChromatographicPeak peak : row.getPeaks()) {

					final ChromatographicPeak newPeak = new SimpleChromatographicPeak(
							peak);
					PeakUtils.copyPeakProperties(peak, newPeak);
					newRow.addPeak(peak.getDataFile(), newPeak);
				}

				newPeakList.addRow(newRow);
			}
		}

		if (!isCanceled()) {

			// Load previous applied methods.
			for (final PeakListAppliedMethod method : origPeakList
					.getAppliedMethods()) {

				newPeakList.addDescriptionOfAppliedTask(method);
			}

			// Add task description to peakList
			newPeakList
					.addDescriptionOfAppliedTask(new SimplePeakListAppliedMethod(
							"Duplicate peak list rows filter", parameters));
		}

		return newPeakList;
	}
}
