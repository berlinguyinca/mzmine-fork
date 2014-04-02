package net.sf.mzmine.modules.deconvolutedanalysis.spectrafilter;

import net.sf.mzmine.parameters.Parameter;
import net.sf.mzmine.parameters.impl.SimpleParameterSet;
import net.sf.mzmine.parameters.parametertypes.*;

import java.text.NumberFormat;

public class SpectraFilterParameters extends SimpleParameterSet {
	/**
	 * Open files that are selected within MZmine
	 */
	public static final RawDataFilesParameter DATA_FILES = new RawDataFilesParameter();

	/**
	 * Parameter for removing C13 isotope ions. Iterating over ions from high to
	 * low m/z, remove an ion if its intensity is less than the given percentage
	 * of the previous peak.
	 */
	public static final DoubleParameter C13_ISOTOPE_CUT = new DoubleParameter(
			"C13 Isotope Cut",
			"Remove all C13 isotope ions whose previous ion (one less m/z value) is the given percentage larger (default 50% larger).",
			NumberFormat.getNumberInstance(), 0.5, 0.0, 1.0);

	/**
	 * Parameter for removing full spectra that have a low base peak intensity.
	 */
	public static final IntegerParameter BASE_PEAK_THRESHOLD = new IntegerParameter(
			"Base Peak Threshold",
			"Remove all spectra with base peak intensity less than the given threshold level",
			5000);

	/**
	 * Parameter for removing full spectra that have a low unique mass
	 * intensity.
	 */
	public static final IntegerParameter UNIQUE_MASS_THRESHOLD = new IntegerParameter(
			"Unique Mass Threshold",
			"Remove all spectra with unique mass intensity less than the given threshold level.",
			1000);

	/**
	 * Intensity threshold below which all ions are cut.
	 */
	public static final IntegerParameter INTENSITY_THRESHOLD = new IntegerParameter(
			"Intensity Threshold",
			"Remove all ions less than the given noise threshold level.", 100);

	/**
	 * In each individual mass spectrum, remove all ions with intensity less
	 * than the given percentage of its base peak intensity.
	 */
	public static final DoubleParameter INTENSITY_PERCENTAGE_THRESHOLD = new DoubleParameter(
			"Intensity Percentage Threshold",
			"Remove all ions less than the given percentage of the current spectra's base peak (default 1%).",
			NumberFormat.getNumberInstance(), 0.01, 0.0, 1.0);

	/**
	 * File name suffix with which to append the new filtered files.
	 */
	public static final StringParameter SUFFIX = new StringParameter("Suffix",
			"This string is added to filename as suffix", "filtered");

	/**
	 * Option to remove the original loaded files and replace them with the
	 * filtered versions.
	 */
	public static final BooleanParameter REMOVE_ORIGINAL = new BooleanParameter(
			"Remove source file after filtering",
			"If checked, original file will be replaced by the filtered version",
			true);

	/**
	 * Default constructor, creating a new parameter set.
	 */
	public SpectraFilterParameters() {
		super(new Parameter[]{DATA_FILES, C13_ISOTOPE_CUT, BASE_PEAK_THRESHOLD,
				UNIQUE_MASS_THRESHOLD, INTENSITY_THRESHOLD,
				INTENSITY_PERCENTAGE_THRESHOLD, SUFFIX, REMOVE_ORIGINAL});
	}
}