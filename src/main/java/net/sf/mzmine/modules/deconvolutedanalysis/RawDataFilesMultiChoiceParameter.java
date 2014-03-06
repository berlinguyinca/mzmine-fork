package net.sf.mzmine.modules.deconvolutedanalysis;

import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.parameters.parametertypes.MultiChoiceComponent;
import net.sf.mzmine.parameters.parametertypes.MultiChoiceParameter;
import net.sf.mzmine.parameters.parametertypes.RawDataFilesParameter;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

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
		super(name, description, new RawDataFile[0], new RawDataFile[0], minNumber);
		this.dataFiles = dataFiles;
		this.spectrumType = spectrumType;
	}

	@Override
	public MultiChoiceComponent createEditingComponent() {
		// Set all selected raw data files as the potential choices
		updateChoices();

		// Use this selector's spectrum type to choose potentially correct files
		List<RawDataFile> values = new ArrayList<RawDataFile>();

		for (RawDataFile f : getChoices())
			if (f.getName().toLowerCase()
					.contains(spectrumType.name().toLowerCase()))
				values.add(f);

		setValue(values.toArray(new RawDataFile[values.size()]));

		return new MultiChoiceComponent(getChoices());
	}

	@Override
	public void setValue(RawDataFile[] values) {
		updateChoices();
		super.setValue(values);
	}

	@Override
	public void loadValueFromXML(Element xmlElement) {
		Logger logger = Logger.getLogger(getClass().getName());
		logger.info("loadValueFromXML updateChoices");
		logger.info(dataFiles.getValue().toString());
		updateChoices();
		super.loadValueFromXML(xmlElement);
	}

	public void updateChoices() {
		setChoices(dataFiles.getValue());
	}
}