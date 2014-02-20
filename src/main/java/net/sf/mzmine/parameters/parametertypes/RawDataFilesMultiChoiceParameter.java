package net.sf.mzmine.parameters.parametertypes;

import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.modules.rawdatamethods.deconvolutedanalysis.SpectrumType;

import java.util.ArrayList;
import java.util.List;

public class RawDataFilesMultiChoiceParameter
		extends
			MultiChoiceParameter<RawDataFile> {

	private RawDataFilesParameter dataFiles;
	private SpectrumType spectrumType;

	public RawDataFilesMultiChoiceParameter(String name, String description,
			RawDataFilesParameter dataFiles, SpectrumType spectrumType) {
		this(name, description, dataFiles, spectrumType, 1);
	}

	public RawDataFilesMultiChoiceParameter(String name, String description,
			RawDataFilesParameter dataFiles, SpectrumType spectrumType,
			int minNumber) {
		super(name, description, null, null, minNumber);
		this.dataFiles = dataFiles;
		this.spectrumType = spectrumType;
	}

	@Override
	public MultiChoiceComponent createEditingComponent() {
		// Set all selected raw data files as the potential choices
		setChoices(dataFiles.getValue());

		// Use this selector's spectrum type to choose potentially correct files
		List<RawDataFile> values = new ArrayList<RawDataFile>();

		for (RawDataFile f : getChoices())
			if (f.getName().toLowerCase()
					.contains(spectrumType.name().toLowerCase()))
				values.add(f);

		setValue(values.toArray(new RawDataFile[values.size()]));

		return new MultiChoiceComponent(getChoices());
	}
}