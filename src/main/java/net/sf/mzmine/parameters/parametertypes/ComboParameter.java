/*
 * Copyright 2006-2012 The MZmine 2 Development Team
 * 
 * This file is part of MZmine 2.
 * 
 * MZmine 2 is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * MZmine 2 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * MZmine 2; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * St, Fifth Floor, Boston, MA 02110-1301 USA
 */

package net.sf.mzmine.parameters.parametertypes;

import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Logger;

import javax.swing.JComboBox;

import net.sf.mzmine.parameters.UserParameter;

import org.w3c.dom.Element;

/**
 * Combo Parameter implementation
 * 
 */
public class ComboParameter<ValueType>
		implements
			UserParameter<ValueType, JComboBox> {

	private Logger logger = Logger.getLogger(getClass().getName());

	private String name, description;
	private ValueType choices[], value;

	public ComboParameter(String name, String description, ValueType choices[]) {
		this(name, description, choices, null);
	}

	public ComboParameter(String name, String description, ValueType choices[],
			ValueType defaultValue) {
		this.name = name;
		this.description = description;
		this.choices = choices;
		this.value = defaultValue;
	}

	/**
	 * @see net.sf.mzmine.data.Parameter#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public JComboBox createEditingComponent() {
		return new JComboBox(choices);
	}

	@Override
	public ValueType getValue() {
		return value;
	}

	public ValueType[] getChoices() {
		return choices;
	}

	public void setChoices(ValueType newChoices[]) {
		this.choices = newChoices;
	}

	@Override
	public void setValue(ValueType value) {
		this.value = value;
	}

	@Override
	public ComboParameter<ValueType> cloneParameter() {
		ComboParameter<ValueType> copy = new ComboParameter<ValueType>(name,
				description, choices);
		copy.value = this.value;
		return copy;
	}

	@Override
	public void setValueFromComponent(JComboBox component) {
		Object selectedItem = component.getSelectedItem();
		if (selectedItem == null) {
			value = null;
			return;
		}
		if (!Arrays.asList(choices).contains(selectedItem)) {
			throw new IllegalArgumentException("Invalid value for parameter "
					+ name + ": " + selectedItem);
		}
		int index = component.getSelectedIndex();
		if (index < 0)
			return;

		value = choices[index];
	}

	@Override
	public void setValueToComponent(JComboBox component, ValueType newValue) {
		component.setSelectedItem(newValue);
	}

	@Override
	public void loadValueFromXML(Element xmlElement) {
		logger.fine("loading configuration...");
		String elementString = xmlElement.getTextContent();
		logger.fine("element was: " + elementString);
		if (elementString.length() == 0)
			return;

		for (ValueType option : choices) {
			logger.finest("option: " + option.toString());
			if (option.toString().equals(elementString)) {
				logger.finest("accepted...");
				value = option;
				break;
			}
		}
	}

	@Override
	public void saveValueToXML(Element xmlElement) {
		if (value == null)
			return;
		xmlElement.setTextContent(value.toString());
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean checkValue(Collection<String> errorMessages) {
		if (value == null) {
			errorMessages.add(name + " is not set properly");
			return false;
		}
		return true;
	}

}
