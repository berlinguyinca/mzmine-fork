package net.sf.mzmine.modules.deconvolutedanalysis.famealignment;

import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.modules.deconvolutedanalysis.RawDataFilesMultiChoiceParameter;
import net.sf.mzmine.modules.deconvolutedanalysis.SpectrumType;
import net.sf.mzmine.parameters.Parameter;
import net.sf.mzmine.parameters.impl.SimpleParameterSet;
import net.sf.mzmine.parameters.parametertypes.DoubleParameter;
import net.sf.mzmine.parameters.parametertypes.RawDataFilesParameter;

import java.text.NumberFormat;
import java.util.Collection;

public class FameAlignmentParameters extends SimpleParameterSet {
	public static final RawDataFilesParameter DATA_FILES = new RawDataFilesParameter();

	public static final RawDataFilesMultiChoiceParameter[] SPECTRA_DATA = new RawDataFilesMultiChoiceParameter[]{
			new RawDataFilesMultiChoiceParameter("EI Files",
					"Select the EI files for analysis.", DATA_FILES,
					SpectrumType.EI, 0),
			new RawDataFilesMultiChoiceParameter("Methane Files",
					"Select the PCI-Methane files for alignment.", DATA_FILES,
					SpectrumType.METHANE, 0),
			new RawDataFilesMultiChoiceParameter("Isobutane Files",
					"Select the PCI-Isobutane files for alignment.",
					DATA_FILES, SpectrumType.ISOBUTANE, 0)};

	public static final DoubleParameter MATCH_TIME_WINDOW = new DoubleParameter(
			"Retention Time Search Window (s)",
			"Time window, in seconds, in which a peak can be matched.",
			NumberFormat.getNumberInstance(), 2.5, 0.01, 60.0);

	public FameAlignmentParameters() {
		super(new Parameter[]{DATA_FILES, SPECTRA_DATA[0], SPECTRA_DATA[1],
				SPECTRA_DATA[2]});
	}

	@Override
	public boolean checkUserParameterValues(Collection<String> errorMessages) {
		// Run checkMultiChoiceParameters only if all other parameters are valid
		return super.checkUserParameterValues(errorMessages)
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