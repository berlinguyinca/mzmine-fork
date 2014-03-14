package net.sf.mzmine.modules.deconvolutedanalysis.famealignment;

import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.modules.deconvolutedanalysis.RawDataFilesMultiChoiceParameter;
import net.sf.mzmine.modules.deconvolutedanalysis.SpectrumType;
import net.sf.mzmine.parameters.Parameter;
import net.sf.mzmine.parameters.impl.SimpleParameterSet;
import net.sf.mzmine.parameters.parametertypes.BooleanParameter;
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
			new RawDataFilesMultiChoiceParameter("PCI-Methane Files",
					"Select the PCI-Methane files for alignment.", DATA_FILES,
					SpectrumType.METHANE, 0),
			new RawDataFilesMultiChoiceParameter("PCI-Isobutane Files",
					"Select the PCI-Isobutane files for alignment.",
					DATA_FILES, SpectrumType.ISOBUTANE, 0)};

	public static final DoubleParameter MATCH_TIME_WINDOW = new DoubleParameter(
			"Retention Time Search Window (s)",
			"Time window, in seconds, in which a peak can be matched.",
			NumberFormat.getNumberInstance(), 10.0, 0.01, 60.0);

	public static final BooleanParameter SHOW_RESULTS = new BooleanParameter("Show Results", "Shows a table with the results of FAME detection.", false);


	public FameAlignmentParameters() {
		super(new Parameter[]{DATA_FILES, SPECTRA_DATA[0], SPECTRA_DATA[1],
				SPECTRA_DATA[2], MATCH_TIME_WINDOW, SHOW_RESULTS});
	}

	@Override
	public boolean checkUserParameterValues(Collection<String> errorMessages) {
		// Run checkMultiChoiceParameters only if all other parameters are valid
		return super.checkUserParameterValues(errorMessages)
				&& checkMultiChoiceParameters(errorMessages);
	}

	private boolean checkMultiChoiceParameters(Collection<String> errorMessages) {
		// Get spectra data
		RawDataFilesMultiChoiceParameter[] spectraData = new RawDataFilesMultiChoiceParameter[SPECTRA_DATA.length];
		for(int i = 0; i < SPECTRA_DATA.length; i++)
			spectraData[i] = getParameter(SPECTRA_DATA[i]);

		// Number of matched files
		int count = 0;

		// Compare each unique pair of RawDataFile objects
		for (int i = 0; i < spectraData.length - 1; i++) {
			for (int j = i + 1; j < spectraData.length; j++) {

				// Compare each selected file for multiple-selected files
				for (RawDataFile a : spectraData[i].getValue()) {
					for (RawDataFile b : spectraData[j].getValue()) {

						// If two RawDataFile objects have the same reference,
						// add an error message
						if (a == b) {
							errorMessages.add(a.getName() + " is in both '"
									+ spectraData[i].getName() + "' and '"
									+ spectraData[j].getName() + "' lists.");
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