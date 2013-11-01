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
package net.sf.mzmine.main;

import net.sf.mzmine.modules.MZmineModule;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

/**
 * List of modules included in MZmine
 */
public class MZmineModulesList {

	/*
	 * public static final Class<?> MODULES[] = new Class<?>[]{ // Project
	 * methods ProjectLoadModule.class, ProjectSaveModule.class,
	 * ProjectSaveAsModule.class, ProjectCloseModule.class, // Batch mode
	 * BatchModeModule.class, // Raw data methods RawDataImportModule.class,
	 * MassDetectionModule.class, ShoulderPeaksFilterModule.class,
	 * ChromatogramBuilderModule.class, ManualPeakPickerModule.class,
	 * MsMsPeakPickerModule.class, ScanFiltersModule.class,
	 * DataSetFiltersModule.class, BaselineCorrectionModule.class, // Alignment
	 * JoinAlignerModule.class, RansacAlignerModule.class, //
	 * PathAlignerModule.class,
	 * 
	 * // I/O CSVExportModule.class, XMLExportModule.class,
	 * XMLImportModule.class, SQLExportModule.class, // Gap filling
	 * PeakFinderModule.class, SameRangeGapFillerModule.class, // Isotopes
	 * IsotopeGrouperModule.class, IsotopePatternCalculator.class, // Peak
	 * detection SmoothingModule.class, DeconvolutionModule.class,
	 * ShapeModelerModule.class, PeakExtenderModule.class,
	 * TargetedPeakDetectionModule.class, // Peak list filtering
	 * DuplicateFilterModule.class, RowsFilterModule.class, // Normalization
	 * RTNormalizerModule.class, LinearNormalizerModule.class,
	 * StandardCompoundNormalizerModule.class, // Data analysis
	 * CVPlotModule.class, LogratioPlotModule.class, PCAPlotModule.class,
	 * CDAPlotModule.class, SammonsPlotModule.class, ClusteringModule.class,
	 * HeatMapModule.class, // Identification CustomDBSearchModule.class,
	 * FormulaPredictionModule.class, FragmentSearchModule.class,
	 * AdductSearchModule.class, ComplexSearchModule.class,
	 * OnlineDBSearchModule.class, GPLipidSearchModule.class,
	 * CameraSearchModule.class, NistMsSearchModule.class,
	 * FormulaPredictionPeakListModule.class, // Visualizers
	 * TICVisualizerModule.class, SpectraVisualizerModule.class,
	 * TwoDVisualizerModule.class, ThreeDVisualizerModule.class,
	 * NeutralLossVisualizerModule.class, PeakListTableModule.class,
	 * IsotopePatternExportModule.class, MSMSExportModule.class,
	 * ScatterPlotVisualizerModule.class, HistogramVisualizerModule.class,
	 * InfoVisualizerModule.class, IntensityPlotModule.class, // Tools
	 * MzRangeCalculatorModule.class};
	 */

	/**
	 * loads all available modules in the mzmine package
	 */
	public static final Set<Class<? extends MZmineModule>> MODULES = findModulesInPackage("net.sf.mzmine");

	/**
	 * scans the local classpath and addes all implementation of our modules to
	 * this
	 * 
	 * @param packages
	 *            where would we like to search
	 * @return
	 */
	private static Set<Class<? extends MZmineModule>> findModulesInPackage(
			String packages) {

		Reflections reflections = new Reflections(packages);

		Set<Class<? extends MZmineModule>> result = new HashSet<Class<? extends MZmineModule>>();

		// builds all our implementation objects
		for (Class<? extends MZmineModule> module : reflections
				.getSubTypesOf(MZmineModule.class)) {
			if (module.isInterface() == false) {
				if (module.isAnonymousClass() == false) {
					if (module.isAnnotation() == false) {
						if (module.isLocalClass() == false) {
							if (Modifier.isAbstract(module.getModifiers()) == false) {
								if (module.isEnum() == false) {
									result.add(module);
								}
							}
						}
					}
				}
			}
		}
		return result;
	}
}