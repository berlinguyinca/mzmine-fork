package net.sf.mzmine.modules.deconvolutedanalysis.massdetection;

import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.modules.peaklistmethods.identification.adductsearch.AdductType;
import net.sf.mzmine.modules.deconvolutedanalysis.SpectrumType;
import net.sf.mzmine.parameters.parametertypes.AdductsParameter;
import net.sf.mzmine.modules.deconvolutedanalysis.RawDataFilesMultiChoiceParameter;
import net.sf.mzmine.parameters.Parameter;
import net.sf.mzmine.parameters.impl.SimpleParameterSet;
import net.sf.mzmine.parameters.parametertypes.DoubleParameter;
import net.sf.mzmine.parameters.parametertypes.IntegerParameter;
import net.sf.mzmine.parameters.parametertypes.RawDataFilesParameter;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

public class SpectraMatcherParameters extends SimpleParameterSet {
	/**
	 *
	 */
	public static final Map<SpectrumType, AdductType[]> ADDUCTS;

	/**
	 *
	 */
	public static final Map<SpectrumType, AdductsParameter> ADDUCT_PARAMS;

	/**
	 * Open files that are selected within MZmine.
	 */
	public static final RawDataFilesParameter DATA_FILES = new RawDataFilesParameter();

	/**
	 * Store selected files for each individual spectrum type to be processed.
	 */
	public static final Map<SpectrumType, RawDataFilesMultiChoiceParameter> SPECTRA_DATA;

	/**
	 *
	 */
	public static final Map<SpectrumType, IntegerParameter> ADDUCT_MATCHES;

	/**
	 *
	 */
	public static final Map<SpectrumType, IntegerParameter> FILE_MATCHES;

	/**
	 *
	 */
	public static final DoubleParameter MATCH_TIME_WINDOW = new DoubleParameter(
			"Retention Time Search Window (s)",
			"Time window, in seconds, in which masses should be considered as the same molecule.",
			NumberFormat.getNumberInstance(), 2.5, 0.01, 60.0);

	static {
		//
		ADDUCTS = new EnumMap<SpectrumType, AdductType[]>(SpectrumType.class);
		ADDUCTS.put(SpectrumType.EI, new AdductType[]{
				new AdductType("[M]+", 0), new AdductType("[M-CH3]+", -15),
				new AdductType("[M-H20]+", -18),
				new AdductType("[M-OTMS]+", -89),
				new AdductType("[M-OTMS_2]+", -178)});
		ADDUCTS.put(SpectrumType.PCI_METHANE, new AdductType[]{
				new AdductType("[M+H]+", 1), new AdductType("[M+C2H5]+", 29),
				new AdductType("[M+C3H5]+", 41), new AdductType("[M-H]+", -1),
				new AdductType("[M-CH4+H]+", -15),
				new AdductType("[M-H2O+H]+", -17),
				new AdductType("[M-TMSOH+H]+", -89)});
		ADDUCTS.put(SpectrumType.PCI_ISOBUTANE, new AdductType[]{
				new AdductType("[M+H]+", 1), new AdductType("[M+C3H3]+", 39),
				new AdductType("[M+C4H9]+", 57),
				new AdductType("[M+C3H5]+", 41),
				new AdductType("[M+C3H7]+", 43),
				new AdductType("[M-H2O+H]+", -17),
				new AdductType("[M-CH4+H]+", -15),
				new AdductType("[M-TMSOH+H]+", -89)});

		//
		ADDUCT_PARAMS = new EnumMap<SpectrumType, AdductsParameter>(
				SpectrumType.class);
		ADDUCT_PARAMS.put(SpectrumType.EI, new AdductsParameter("EI",
				"EI Neutral Losses", ADDUCTS.get(SpectrumType.EI)));
		ADDUCT_PARAMS.put(
				SpectrumType.PCI_METHANE,
				new AdductsParameter("PCI-Methane",
						"PCI-Methane Adducts/Losses", ADDUCTS
								.get(SpectrumType.PCI_METHANE)));
		ADDUCT_PARAMS.put(
				SpectrumType.PCI_ISOBUTANE,
				new AdductsParameter("PCI-Isobutane",
						"PCI-Isobutane Adducts/Losses", ADDUCTS
								.get(SpectrumType.PCI_ISOBUTANE)));

		// Define the data file MultiChoiceParameters for each spectrum type
		SPECTRA_DATA = new EnumMap<SpectrumType, RawDataFilesMultiChoiceParameter>(
				SpectrumType.class);
		SPECTRA_DATA.put(SpectrumType.EI, new RawDataFilesMultiChoiceParameter(
				"EI Files", "Select the EI files for analysis.", DATA_FILES,
				SpectrumType.EI));
		SPECTRA_DATA.put(SpectrumType.PCI_METHANE,
				new RawDataFilesMultiChoiceParameter("PCI-Methane Files",
						"Select the PCI-Methane files for analysis.",
						DATA_FILES, SpectrumType.PCI_METHANE));
		SPECTRA_DATA.put(SpectrumType.PCI_ISOBUTANE,
				new RawDataFilesMultiChoiceParameter("PCI-Isobutane Files",
						"Select the PCI-Isobutane files for analysis.",
						DATA_FILES, SpectrumType.PCI_ISOBUTANE));

		//
		ADDUCT_MATCHES = new EnumMap<SpectrumType, IntegerParameter>(
				SpectrumType.class);
		ADDUCT_MATCHES
				.put(SpectrumType.EI,
						new IntegerParameter(
								"Required EI Neutral Losses",
								"Number of EI neutral losses required for a value to be considered a mass candidate",
								1));
		ADDUCT_MATCHES
				.put(SpectrumType.PCI_METHANE,
						new IntegerParameter(
								"Required PCI-Methane Adducts/Losses",
								"Number of PCI-Methane Adducts/Losses required for a value to be considered a mass candidate",
								1));
		ADDUCT_MATCHES
				.put(SpectrumType.PCI_ISOBUTANE,
						new IntegerParameter(
								"Required PCI-Isobutane Adducts/Losses",
								"Number of PCI-Isobutane Adducts/Losses required for a value to be considered a mass candidate",
								1));

		//
		FILE_MATCHES = new EnumMap<SpectrumType, IntegerParameter>(
				SpectrumType.class);
		FILE_MATCHES
				.put(SpectrumType.EI,
						new IntegerParameter(
								"Required EI Files",
								"Number of EI files in which a mass must exist to be considered a mass candidate",
								1));
		FILE_MATCHES
				.put(SpectrumType.PCI_METHANE,
						new IntegerParameter(
								"Required PCI-Methane Files",
								"Number of PCI-Methane files in which a mass must exist to be considered a mass candidate",
								1));
		FILE_MATCHES
				.put(SpectrumType.PCI_ISOBUTANE,
						new IntegerParameter(
								"Required PCI-Isobutane Files",
								"Number of PCI-Isobutane files in which a mass must exist to be considered a mass candidate",
								1));
	}

	public SpectraMatcherParameters() {
		super(new Parameter[]{DATA_FILES, SPECTRA_DATA.get(SpectrumType.EI),
				FILE_MATCHES.get(SpectrumType.EI),
				ADDUCT_PARAMS.get(SpectrumType.EI),
				ADDUCT_MATCHES.get(SpectrumType.EI),
				SPECTRA_DATA.get(SpectrumType.PCI_METHANE),
				FILE_MATCHES.get(SpectrumType.PCI_METHANE),
				ADDUCT_PARAMS.get(SpectrumType.PCI_METHANE),
				ADDUCT_MATCHES.get(SpectrumType.PCI_METHANE),
				SPECTRA_DATA.get(SpectrumType.PCI_ISOBUTANE),
				FILE_MATCHES.get(SpectrumType.PCI_ISOBUTANE),
				ADDUCT_PARAMS.get(SpectrumType.PCI_ISOBUTANE),
				ADDUCT_MATCHES.get(SpectrumType.PCI_ISOBUTANE),
				MATCH_TIME_WINDOW});
	}

	/**
	 * Checks that our custom user parameters are valid ONLY when the default
	 * checks are validated.
	 * 
	 * @param errorMessages
	 *            collection of error messages to add to, if necessary
	 * @return whether all user parameters are valid
	 */
	@Override
	public boolean checkUserParameterValues(Collection<String> errorMessages) {
		// Run checkMultiChoiceParameters only if all other parameters are valid
		return super.checkUserParameterValues(errorMessages)
				&& checkMultiChoiceParameters(errorMessages);
	}

	/**
	 * Compares each data file input parameter to check for multiple selections
	 * of a single file. Additionally verifies that
	 * 
	 * @param errorMessages
	 *            collection of error messages to add to, if necessary
	 * @return whether this check passes without generating any errors
	 */
	private boolean checkMultiChoiceParameters(Collection<String> errorMessages) {
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

		// If files have each only been selected once, check that the
		// "file matches" parameters do not exceed the number of selected files
		if (count == 0) {
			for (SpectrumType type : SPECTRA_DATA.keySet()) {
				if (getParameter(FILE_MATCHES.get(type)).getValue() > getParameter(
						SPECTRA_DATA.get(type)).getValue().length) {
					errorMessages.add("'" + FILE_MATCHES.get(type).getName()
							+ "' exceeds number of selected files.");
					count++;
				}
			}
		}

		// Return true if no matches are found, else false
		return (count == 0);
	}
}