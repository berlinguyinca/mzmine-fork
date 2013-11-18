/*
 * Copyright 2006-2013 The MZmine 2 Development Team
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

package net.sf.mzmine.modules.rawdatamethods.miniximport;

import java.io.File;
import java.util.Collection;

import net.sf.mzmine.parameters.Parameter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class MiniXIDParameter implements Parameter<String> {

	private String id;
	
	@Override
	public String getName() {
		return "MiniX Study ID";
	}

	public String getValue() {
		return this.id;
	}

	public void setValue(String id) {
		this.id = id;
	}

	@Override
	public MiniXIDParameter cloneParameter() {
		MiniXIDParameter copy = new MiniXIDParameter();
		copy.setValue(this.getValue());
		return copy;
	}

	@Override
	public void loadValueFromXML(Element xmlElement) {
		Element idElem = (Element)xmlElement.getElementsByTagName("studyID").item(0);
		String id = idElem.getTextContent();
		this.id = id;
	}

	@Override
	public void saveValueToXML(Element xmlElement) {
		if (this.id == null)
			return;
		Document parentDocument = xmlElement.getOwnerDocument();
        Element newElement = parentDocument.createElement("studyID");
        newElement.setTextContent (this.id);
        xmlElement.appendChild(newElement);
	}
	
	@Override
	public boolean checkValue(Collection<String> errorMessages) {
		if (this.id == null) {
			errorMessages.add("MiniX study id is not set");
			return false;
		}
		return true;
	}

}
