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
 * the currently selected files in the desktop version. For instance, this is
 * required to perform simultaneous analysis of data with multiple ionization
 * strategies.
 */
public class RawDataFilesMultiChoiceParameter
		extends
			MultiChoiceParameter<RawDataFile> {

	/**
	 * RawDataFilesParameter from which to obtain the file choices.
	 */
	private final RawDataFilesParameter dataFiles;

	/**
	 * Indicates the type of spectrum of the selected files.
	 */
	private final SpectrumType spectrumType;

	/**
	 * The minimum number of required files to select (default: 1)
	 */
	private final int minNumber;

	/**
	 * Temporary storage for selected values until choices are loaded.
	 */
	private final List<String> valueNames;

	/**
	 * Base constructor, creating a new MultiChoiceParameter of the given
	 * ionization type requiring at least one selected file.
	 * 
	 * @param name
	 *            parameter name
	 * @param description
	 *            parameter description
	 * @param dataFiles
	 *            parameter that provides the choices for selection
	 * @param spectrumType
	 *            spectrum ionization type represented by this parameter
	 */
	public RawDataFilesMultiChoiceParameter(String name, String description,
			RawDataFilesParameter dataFiles, SpectrumType spectrumType) {
		this(name, description, dataFiles, spectrumType, 1);
	}

	/**
	 * Constructor, creating a new MultiChoiceParameter of the given ionization
	 * type requiring the given number of selected files.
	 * 
	 * @param name
	 *            parameter name
	 * @param description
	 *            parameter description
	 * @param dataFiles
	 *            parameter that provides the choices for selection
	 * @param spectrumType
	 *            spectrum ionization type represented by this parameter
	 * @param minNumber
	 *            number of selected files required by this parameter
	 */
	public RawDataFilesMultiChoiceParameter(String name, String description,
			RawDataFilesParameter dataFiles, SpectrumType spectrumType,
			int minNumber) {
		this(name, description, new RawDataFile[0], new RawDataFile[0],
				dataFiles, spectrumType, minNumber);
	}

	/**
	 * Constructor, creating a new MultiChoiceParameter of the given ionization
	 * type, choices and values, and requiring the given number of selected
	 * files.
	 * 
	 * @param name
	 *            parameter name
	 * @param description
	 *            parameter description
	 * @param choices
	 *            files to be given as choices
	 * @param values
	 *            files to be selected by default
	 * @param dataFiles
	 *            parameter that provides the choices for selection
	 * @param spectrumType
	 *            spectrum ionization type represented by this parameter
	 * @param minNumber
	 *            number of selected files required by this parameter
	 */
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
	 * Set all selected raw data files as the available choices.
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
		// Update the available choices
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
	 *            files to be given as choices
	 */
	@Override
	public void setChoices(RawDataFile[] choices) {
		// Perform the default actions
		super.setChoices(choices);

		// Update selected values if any have been loaded from XML
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
	 * Create a new object with this parameter's configuration.
	 * 
	 * @return a clone of this parameter object
	 */
	@Override
	public RawDataFilesMultiChoiceParameter cloneParameter() {
		RawDataFilesMultiChoiceParameter copy = new RawDataFilesMultiChoiceParameter(
				getName(), getDescription(), getChoices(), getValue(),
				dataFiles, spectrumType, minNumber);

		copy.setValue(getValue());
		return copy;
	}
}