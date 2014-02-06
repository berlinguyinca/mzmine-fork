package net.sf.mzmine.modules.rawdatamethods.deconvolutedanalysis.massdetection;

import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.parameters.parametertypes.MultiChoiceComponent;
import net.sf.mzmine.parameters.parametertypes.MultiChoiceParameter;

import java.util.ArrayList;
import java.util.List;

public class RawDataFilesMultiChoiceParameter
		extends
			MultiChoiceParameter<RawDataFile> {

	private SpectrumType spectrumType;

	public RawDataFilesMultiChoiceParameter(String name, String description,
			SpectrumType spectrumType) {
		super(name, description, null);
		this.spectrumType = spectrumType;
	}

	@Override
	public MultiChoiceComponent createEditingComponent() {
		// Set all selected raw data files as the potential choices
		setChoices(SpectraMatcherParameters.DATA_FILES.getValue());

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