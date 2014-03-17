package net.sf.mzmine.modules.deconvolutedanalysis;

import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.parameters.parametertypes.MultiChoiceComponent;
import net.sf.mzmine.parameters.parametertypes.MultiChoiceParameter;
import net.sf.mzmine.parameters.parametertypes.RawDataFilesParameter;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * Extension of MultiChoiceParameter that enables users to make a selection from
 * the currently selected files in the desktop version.
 */
public class RawDataFilesMultiChoiceParameter
		extends
			MultiChoiceParameter<RawDataFile> {

	/**
	 * RawDataFilesParameter from which to obtain the file choices.
	 */
	private RawDataFilesParameter dataFiles;

	/**
	 * Indicates the type of spectrum of the selected files.
	 */
	private SpectrumType spectrumType;

	/**
	 * The minimum number of required files to select (default: 1)
	 */
	private int minNumber;

	/**
	 * Temporary storage for selected values until choices are loaded.
	 */
	private List<String> valueNames;

	public RawDataFilesMultiChoiceParameter(String name, String description,
			RawDataFilesParameter dataFiles, SpectrumType spectrumType) {
		this(name, description, dataFiles, spectrumType, 1);
	}

	public RawDataFilesMultiChoiceParameter(String name, String description,
			RawDataFilesParameter dataFiles, SpectrumType spectrumType,
			int minNumber) {
		this(name, description, new RawDataFile[0], new RawDataFile[0],
				dataFiles, spectrumType, minNumber);
	}

	public RawDataFilesMultiChoiceParameter(String name, String description,
			RawDataFile[] choices, RawDataFile[] values,
			RawDataFilesParameter dataFiles, SpectrumType spectrumType,
			int minNumber) {
		super(name, description, choices, values, minNumber);
		this.dataFiles = dataFiles;
		this.spectrumType = spectrumType;
		this.minNumber = minNumber;

		valueNames = new ArrayList<String>();
	}

	/**
	 * Set all selected raw data files as the potential choices before updating
	 * selected values
	 */
	private void updateChoices() {
		setChoices((dataFiles.getValue() == null)
				? new RawDataFile[0]
				: dataFiles.getValue());
	}

	/**
	 * Create a new MultiChoiceComponent filled with the user-selected
	 * RawDataFile choices
	 * 
	 * @return custom MultiChoiceComponent
	 */
	@Override
	public MultiChoiceComponent createEditingComponent() {
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

	/**
	 * Update the choices available for selection
	 * 
	 * @param choices
	 *            available to select
	 */
	@Override
	public void setChoices(RawDataFile[] choices) {
		super.setChoices(choices);

		// Update selected values if any have been loaded
		if (valueNames.size() > 0) {
			// Create a new list of loaded values that have matching choices
			List<RawDataFile> values = new ArrayList<RawDataFile>();

			for (String fileName : valueNames) {
				for (RawDataFile f : getChoices())
					if (f.toString().equals(fileName))
						values.add(f);
			}

			// Update values
			super.setValue(values.toArray(new RawDataFile[values.size()]));

			// Clear the queue of values to add
			valueNames.clear();
		}
	}

	/**
	 * Parse the xml element corresponding to this module and load the
	 * user-defined values.
	 * 
	 * @param xmlElement
	 *            element in xml configuration file
	 */
	@Override
	public void loadValueFromXML(Element xmlElement) {
		updateChoices();

		if (getChoices() == null || getChoices().length == 0) {
			NodeList items = xmlElement.getElementsByTagName("item");

			for (int i = 0; i < items.getLength(); i++)
				valueNames.add(items.item(i).getTextContent());
		} else
			super.loadValueFromXML(xmlElement);
	}

	/**
	 * Create a new object with this parameter's configuration
	 * 
	 * @return a clone of this parameter object
	 */
	@Override
	public RawDataFilesMultiChoiceParameter cloneParameter() {
		RawDataFilesMultiChoiceParameter copy = new RawDataFilesMultiChoiceParameter(
				getName(), getDescription(), getChoices(), getValue(),
				dataFiles, spectrumType, minNumber);
		copy.setValue(this.getValue());
		return copy;
	}
}