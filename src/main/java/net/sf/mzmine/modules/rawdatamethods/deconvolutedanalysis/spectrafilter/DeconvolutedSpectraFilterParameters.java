package net.sf.mzmine.modules.rawdatamethods.deconvolutedanalysis.spectrafilter;

import net.sf.mzmine.parameters.Parameter;
import net.sf.mzmine.parameters.impl.SimpleParameterSet;
import net.sf.mzmine.parameters.parametertypes.*;

import java.text.NumberFormat;

public class DeconvolutedSpectraFilterParameters extends SimpleParameterSet {
	public static final RawDataFilesParameter DATA_FILES = new RawDataFilesParameter();

	public static final DoubleParameter C13_ISOTOPE_CUT = new DoubleParameter(
			"C13 Isotope Cut",
			"Remove all C13 isotope ions whose previous ion (one less m/z value) is the given percentage larger (default 50% larger).",
			NumberFormat.getNumberInstance(), 0.5, 0.0, 1.0);

	public static final IntegerParameter NOISE_THRESHOLD = new IntegerParameter(
			"Intensity Threshold",
			"Remove all ions less than the given noise threshold level.", 100);

	public static final DoubleParameter BASE_PEAK_CUT = new DoubleParameter(
			"Base Peak Threshold (Percentage)",
			"Remove all ions less than the given percentage of the current spectra's base peak (default 1%).",
			NumberFormat.getNumberInstance(), 0.01, 0.0, 1.0);

	public static final StringParameter SUFFIX = new StringParameter("Suffix",
			"This string is added to filename as suffix", "filtered");

	public static final BooleanParameter REMOVE_ORIGINAL = new BooleanParameter(
			"Remove source file after filtering",
			"If checked, original file will be replaced by the corrected version",
			true);

	public DeconvolutedSpectraFilterParameters() {
		super(new Parameter[]{DATA_FILES, C13_ISOTOPE_CUT, NOISE_THRESHOLD,
				BASE_PEAK_CUT, SUFFIX, REMOVE_ORIGINAL});
	}
}