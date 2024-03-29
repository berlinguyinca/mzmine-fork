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

package net.sf.mzmine.modules.visualization.peaklist.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import javax.annotation.Nonnull;

import net.sf.mzmine.data.ChromatographicPeak;
import net.sf.mzmine.data.DataPoint;
import net.sf.mzmine.data.IsotopePattern;
import net.sf.mzmine.data.PeakListRow;
import net.sf.mzmine.data.impl.SimpleDataPoint;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.modules.MZmineModule;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.util.ExceptionUtils;
import net.sf.mzmine.util.ExitCode;

public class IsotopePatternExportModule implements MZmineModule {

	private static final String MODULE_NAME = "Isotope pattern export";

	@Override
	public @Nonnull
	String getName() {
		return MODULE_NAME;
	}

	public static void exportIsotopePattern(PeakListRow row) {

		ParameterSet parameters = MZmineCore.getConfiguration()
				.getModuleParameters(IsotopePatternExportModule.class);

		ExitCode exitCode = parameters.showSetupDialog();
		if (exitCode != ExitCode.OK)
			return;

		File outputFile = parameters.getParameter(
				IsotopePatternExportParameters.outputFile).getValue();
		if (outputFile == null)
			return;

		IsotopePattern pattern = row.getBestIsotopePattern();

		DataPoint isotopes[];

		if (pattern != null) {
			isotopes = pattern.getDataPoints();
		} else {
			isotopes = new DataPoint[1];
			ChromatographicPeak bestPeak = row.getBestPeak();
			isotopes[0] = new SimpleDataPoint(bestPeak.getMZ(),
					bestPeak.getHeight());
		}

		try {
			FileWriter fileWriter = new FileWriter(outputFile);
			BufferedWriter writer = new BufferedWriter(fileWriter);

			for (DataPoint isotope : isotopes) {
				writer.write(isotope.getMZ() + " " + isotope.getIntensity());
				writer.newLine();
			}

			writer.close();

		} catch (Exception e) {
			MZmineCore.getDesktop().displayErrorMessage(
					"Error writing to file " + outputFile + ": "
							+ ExceptionUtils.exceptionToString(e));
		}

	}

	@Override
	public @Nonnull
	Class<? extends ParameterSet> getParameterSetClass() {
		return IsotopePatternExportParameters.class;
	}

}