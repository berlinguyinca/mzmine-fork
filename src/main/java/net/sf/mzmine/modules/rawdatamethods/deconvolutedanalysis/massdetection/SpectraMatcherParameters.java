package net.sf.mzmine.modules.rawdatamethods.deconvolutedanalysis.massdetection;

import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.modules.peaklistmethods.identification.adductsearch.AdductType;
import net.sf.mzmine.parameters.Parameter;
import net.sf.mzmine.parameters.impl.SimpleParameterSet;
import net.sf.mzmine.parameters.parametertypes.IntegerParameter;
import net.sf.mzmine.parameters.parametertypes.RawDataFilesParameter;

import java.util.Collection;

public class SpectraMatcherParameters extends SimpleParameterSet {
	public static final AdductType[] METHANE_ADDUCTS = new AdductType[]{
			new AdductType("[M-TMSOH+H]+", -89),
			new AdductType("[M-H2O+H]+", -17),
			new AdductType("[M-CH4+H]+", -15), new AdductType("[M-H]+", -1),
			new AdductType("[M+H]+", 1), new AdductType("[M+CH5]+", 17),
			new AdductType("[M+C2H5]+", 29), new AdductType("[M+C3H5]+", 41)};

	public static final AdductType[] ISOBUTANE_ADDUCTS = new AdductType[]{
			new AdductType("[M-TMSOH+H]+", -89),
			new AdductType("[M-H2O+H]+", -17), new AdductType("[M+H]+", 1),
			new AdductType("[M+C3H3]+", 39), new AdductType("[M+C4H9]+", 57)};

	public static final AdductType[][] ADDUCT_PARAMS = new AdductType[][]{
			METHANE_ADDUCTS, ISOBUTANE_ADDUCTS};

	public static final RawDataFilesParameter DATA_FILES = new RawDataFilesParameter();

	public static final RawDataFilesMultiChoiceParameter[] SPECTRA_DATA = new RawDataFilesMultiChoiceParameter[]{
			new RawDataFilesMultiChoiceParameter("Methane Files",
					"Select the PCI-Methane files for analysis.",
					SpectrumType.METHANE),
			new RawDataFilesMultiChoiceParameter("Isobutane Files",
					"Select the PCI-Isobutane files for analysis.",
					SpectrumType.ISOBUTANE)};

	public static final IntegerParameter[] ADDUCT_MATCHES = new IntegerParameter[]{
			new IntegerParameter(
					"Required Methane Adducts",
					"Number of PCI-Methane adduct/loss matches required for a value to be considered a mass candidate",
					5),
			new IntegerParameter(
					"Required Isobutane Adducts",
					"Number of PCI-Isobutane adduct/loss matches required for a value to be considered a mass candidate",
					3)};

	public static final IntegerParameter[] FILE_MATCHES = new IntegerParameter[]{
			new IntegerParameter(
					"Required Methane Files",
					"Number of PCI-Methane files in which a mass must exist to be considered a mass candidate",
					1),
			new IntegerParameter(
					"Required Isobutane Files",
					"Number of PCI-Isobutane files in which a mass must exist to be considered a mass candidate",
					1)};

	public static final IntegerParameter MATCH_TIME_WINDOW = new IntegerParameter(
			"Retention Time Search Window (s)",
			"Time window, in seconds, in which masses should be considered as the same molecule.",
			5);

	public SpectraMatcherParameters() {
		super(new Parameter[]{DATA_FILES, SPECTRA_DATA[0], ADDUCT_MATCHES[0],
				SPECTRA_DATA[1], ADDUCT_MATCHES[1], FILE_MATCHES[0],
				FILE_MATCHES[1], MATCH_TIME_WINDOW});
	}

	@Override
	public boolean checkUserParameterValues(Collection<String> errorMessages) {
		// Run checkMultiChoiceParameters only if all other parameters are valid
		return super.checkUserParameterValues(errorMessages)
				&& checkMultiChoiceParameters(errorMessages);
	}

	@Override
	public boolean checkAllParameterValues(Collection<String> errorMessages) {
		// Run checkMultiChoiceParameters only if all other parameters are valid
		return super.checkAllParameterValues(errorMessages)
				&& checkMultiChoiceParameters(errorMessages);
	}

	private boolean checkMultiChoiceParameters(Collection<String> errorMessages) {
		// Number of matched files
		int count = 0;

		// Compare each unique pair of RawDataFile objects
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

		// Return true if no matches are found, else false
		return (count == 0);
	}
}