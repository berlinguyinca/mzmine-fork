package net.sf.mzmine.modules.deconvolutedanalysis.famealignment;

import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.modules.deconvolutedanalysis.RawDataFilesMultiChoiceParameter;
import net.sf.mzmine.modules.deconvolutedanalysis.SpectrumType;
import net.sf.mzmine.parameters.Parameter;
import net.sf.mzmine.parameters.impl.SimpleParameterSet;
import net.sf.mzmine.parameters.parametertypes.BooleanParameter;
import net.sf.mzmine.parameters.parametertypes.DoubleParameter;
import net.sf.mzmine.parameters.parametertypes.RawDataFilesParameter;
import net.sf.mzmine.parameters.parametertypes.StringParameter;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

public class FameAlignmentParameters extends SimpleParameterSet {
	/**
	 * Open files that are selected within MZmine.
	 */
	public static final RawDataFilesParameter DATA_FILES = new RawDataFilesParameter();

	/**
	 * Store selected files for each individual spectrum type to be processed.
	 */
	public static final Map<SpectrumType, RawDataFilesMultiChoiceParameter> SPECTRA_DATA;

	/**
	 * Time window (in seconds) in which to search for spectra corresponding to
	 * a FAME marker.
	 */
	public static final DoubleParameter MATCH_TIME_WINDOW = new DoubleParameter(
			"Retention Time Search Window (s)",
			"Time window, in seconds, in which a peak can be matched.",
			NumberFormat.getNumberInstance(), 10.0, 0.01, 60.0);

	/**
	 * File name suffix with which to append the new filtered files.
	 */
	public static final StringParameter SUFFIX = new StringParameter("Suffix",
			"This string is added to filename as suffix", "ri_corrected");

	/**
	 * Option to remove the original loaded files and replace them with the
	 * filtered versions.
	 */
	public static final BooleanParameter REMOVE_ORIGINAL = new BooleanParameter(
			"Remove source file after filtering",
			"If checked, original file will be replaced by the corrected version",
			true);

	/**
	 * Option to show or hide the alignment results
	 */
	public static final BooleanParameter SHOW_RESULTS = new BooleanParameter(
			"Show Results", "Shows a table with the results of FAME detection",
			true);

	/*
	 * Static initializer
	 */
	static {
		// Define the data file MultiChoiceParameters for each spectrum type
		SPECTRA_DATA = new EnumMap<SpectrumType, RawDataFilesMultiChoiceParameter>(
				SpectrumType.class);

		SPECTRA_DATA.put(SpectrumType.EI, new RawDataFilesMultiChoiceParameter(
				"EI Files", "Select the EI files for analysis.", DATA_FILES,
				SpectrumType.EI, 0));

		SPECTRA_DATA.put(SpectrumType.PCI,
				new RawDataFilesMultiChoiceParameter("PCI Files",
						"Select the PCI files for analysis.", DATA_FILES,
						SpectrumType.PCI, 0));
	}

	/**
	 * Default constructor, creating a new parameter set.
	 */
	public FameAlignmentParameters() {
		super(new Parameter[]{DATA_FILES, SPECTRA_DATA.get(SpectrumType.EI),
				SPECTRA_DATA.get(SpectrumType.PCI), MATCH_TIME_WINDOW, SUFFIX,
				REMOVE_ORIGINAL, SHOW_RESULTS});
	}

	/**
	 * Run the default check of user parameters followed by this module's check
	 * of duplicate selections ONLY if all other parameters are valid.
	 * 
	 * @param errorMessages
	 *            error messages to display
	 * @return result of the user parameter check
	 */
	@Override
	public boolean checkUserParameterValues(Collection<String> errorMessages) {
		return super.checkUserParameterValues(errorMessages)
				&& checkMultiChoiceParameters(errorMessages);
	}

	/**
	 * Check that each spectrum type has a distinct set of selected files.
	 * 
	 * @param errorMessages
	 *            error messages to display
	 * @return result of the user parameter check
	 */
	private boolean checkMultiChoiceParameters(Collection<String> errorMessages) {
		// Number of matched files
		int count = 0;

		// Compare each unique pair of RawDataFile objects
		for (Map.Entry<SpectrumType, RawDataFilesMultiChoiceParameter> x : SPECTRA_DATA
				.entrySet()) {
			for (Map.Entry<SpectrumType, RawDataFilesMultiChoiceParameter> y : SPECTRA_DATA
					.entrySet()) {
				if (x.getKey().getIndex() > y.getKey().getIndex()) {
					for (RawDataFile a : getParameter(x.getValue()).getValue()) {
						for (RawDataFile b : getParameter(y.getValue())
								.getValue()) {
							// If two RawDataFile objects have the same
							// reference,
							// add an error message
							if (a == b) {
								errorMessages.add(a.getName() + " is in both '"
										+ x.getValue().getName() + "' and '"
										+ y.getValue().getName() + "' lists.");
								count++;
							}
						}
					}
				}
			}
		}

		// Return true if no matches are found, else false
		return (count == 0);
	}
}