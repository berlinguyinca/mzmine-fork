package net.sf.mzmine.modules.rawdatamethods.deconvolutedanalysis.massdetection;

import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.modules.peaklistmethods.identification.adductsearch.AdductType;
import net.sf.mzmine.modules.rawdatamethods.deconvolutedanalysis.SpectrumType;
import net.sf.mzmine.parameters.parametertypes.RawDataFilesMultiChoiceParameter;
import net.sf.mzmine.parameters.Parameter;
import net.sf.mzmine.parameters.impl.SimpleParameterSet;
import net.sf.mzmine.parameters.parametertypes.IntegerParameter;
import net.sf.mzmine.parameters.parametertypes.RawDataFilesParameter;

import java.util.Collection;

public class SpectraMatcherParameters extends SimpleParameterSet {

	public static final AdductType[] EI_ADDUCTS = new AdductType[]{
			new AdductType("[M]+", 0), new AdductType("[M-CH3]+", -15),
			new AdductType("[M-H20]+", -18), new AdductType("[M-OTMS]+", -89),
			new AdductType("[M-OTMS_2]+", -89),};

	public static final AdductType[] PCI_METHANE_ADDUCTS = new AdductType[]{
			new AdductType("[M+H]+", 1), new AdductType("[M+C2H5]+", 29),
			new AdductType("[M+C3H5]+", 41), new AdductType("[M-H]+", -1),
			new AdductType("[M-CH4+H]+", -15),
			new AdductType("[M-H2O+H]+", -17),
			new AdductType("[M-TMSOH+H]+", -89)};

	public static final AdductType[] PCI_ISOBUTANE_ADDUCTS = new AdductType[]{
			new AdductType("[M+H]+", 1), new AdductType("[M+C3H3]+", 39),
			new AdductType("[M+C4H9]+", 57), new AdductType("[M+C3H5]+", 41),
			new AdductType("[M+C3H7]+", 43), new AdductType("[M-H2O+H]+", -17),
			new AdductType("[M-CH4+H]+", -15),
			new AdductType("[M-TMSOH+H]+", -89)};

	public static final AdductType[][] ADDUCT_PARAMS = new AdductType[][]{
			EI_ADDUCTS, PCI_METHANE_ADDUCTS, PCI_ISOBUTANE_ADDUCTS};

	public static final RawDataFilesParameter DATA_FILES = new RawDataFilesParameter();

	public static final RawDataFilesMultiChoiceParameter[] SPECTRA_DATA = new RawDataFilesMultiChoiceParameter[]{
			new RawDataFilesMultiChoiceParameter("EI Files",
					"Select the EI files for analysis.", DATA_FILES,
					SpectrumType.EI),
			new RawDataFilesMultiChoiceParameter("PCI-Methane Files",
					"Select the PCI-Methane files for analysis.", DATA_FILES,
					SpectrumType.METHANE),
			new RawDataFilesMultiChoiceParameter("PCI-Isobutane Files",
					"Select the PCI-Isobutane files for analysis.", DATA_FILES,
					SpectrumType.ISOBUTANE)};

	public static final IntegerParameter[] ADDUCT_MATCHES = new IntegerParameter[]{
			new IntegerParameter(
					"Required EI Adducts",
					"Number of PCI-Methane adduct/loss matches required for a value to be considered a mass candidate",
					5),
			new IntegerParameter(
					"Required PCI-Methane Adducts",
					"Number of PCI-Methane adduct/loss matches required for a value to be considered a mass candidate",
					5),
			new IntegerParameter(
					"Required PCI-Isobutane Adducts",
					"Number of PCI-Isobutane adduct/loss matches required for a value to be considered a mass candidate",
					3)};

	public static final IntegerParameter[] FILE_MATCHES = new IntegerParameter[]{
			new IntegerParameter(
					"Required EI Files",
					"Number of PCI-Methane files in which a mass must exist to be considered a mass candidate",
					1),
			new IntegerParameter(
					"Required PCI-Methane Files",
					"Number of PCI-Methane files in which a mass must exist to be considered a mass candidate",
					1),
			new IntegerParameter(
					"Required PCI-Isobutane Files",
					"Number of PCI-Isobutane files in which a mass must exist to be considered a mass candidate",
					1)};

	public static final IntegerParameter MATCH_TIME_WINDOW = new IntegerParameter(
			"Retention Time Search Window (s)",
			"Time window, in seconds, in which masses should be considered as the same molecule.",
			5);

	public SpectraMatcherParameters() {
		super(new Parameter[]{DATA_FILES, SPECTRA_DATA[0], ADDUCT_MATCHES[0],
				FILE_MATCHES[0], SPECTRA_DATA[1], ADDUCT_MATCHES[1],
				FILE_MATCHES[1], SPECTRA_DATA[2], ADDUCT_MATCHES[2],
				FILE_MATCHES[2], MATCH_TIME_WINDOW});
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

		// Compare each unique pair of files to check for multiple file
		// selection
		for (int i = 0; i < SPECTRA_DATA.length - 1; i++) {
			for (int j = i + 1; j < SPECTRA_DATA.length; j++) {

				// Compare each selected file for multiple-selected files
				for (RawDataFile a : SPECTRA_DATA[i].getValue()) {
					for (RawDataFile b : SPECTRA_DATA[j].getValue()) {

						// If two RawDataFile objects have the same reference,
						// add an error message
						if (a == b) {
							errorMessages.add(a.getName() + " is in both '"
									+ SPECTRA_DATA[i].getName() + "' and '"
									+ SPECTRA_DATA[j].getName() + "' lists.");
							count++;
						}
					}
				}
			}
		}

		// If files have each only been selected once, check that the
		// "file matches"
		// parameters do not exceed the number of selected files.
		if (count == 0) {
			for (int i = 0; i < SPECTRA_DATA.length; i++) {
				if (FILE_MATCHES[i].getValue() > SPECTRA_DATA[i].getValue().length) {
					errorMessages.add("'" + FILE_MATCHES[i].getName()
							+ "' exceeds number of selected files.");
					count++;
				}
			}
		}

		// Return true if no matches are found, else false
		return (count == 0);
	}
}