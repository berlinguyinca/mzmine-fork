package net.sf.mzmine.modules.deconvolutedanalysis.spectrafilter;

import net.sf.mzmine.parameters.Parameter;
import net.sf.mzmine.parameters.impl.SimpleParameterSet;
import net.sf.mzmine.parameters.parametertypes.*;

import java.text.NumberFormat;

public class DeconvolutedSpectraFilterParameters extends SimpleParameterSet {
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
	 * Intensity threshold below which all ions are cut.
	 */
	public static final IntegerParameter INTENSITY_THRESHOLD = new IntegerParameter(
			"Intensity Threshold",
			"Remove all ions less than the given noise threshold level.", 100);

	/**
	 * In each individual mass spectrum, remove all ions with intensity less
	 * than the given percentage of its base peak intensity.
	 */
	public static final DoubleParameter BASE_PEAK_CUT = new DoubleParameter(
			"Base Peak Threshold (Percentage)",
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
			"If checked, original file will be replaced by the corrected version",
			true);

	public DeconvolutedSpectraFilterParameters() {
		super(new Parameter[]{DATA_FILES, C13_ISOTOPE_CUT, INTENSITY_THRESHOLD,
				BASE_PEAK_CUT, SUFFIX, REMOVE_ORIGINAL});
	}
}