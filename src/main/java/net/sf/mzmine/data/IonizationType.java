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
 * MZmine 2; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */

package net.sf.mzmine.data;

public enum IonizationType {

	NO_IONIZATION("No ionization", "", 0, Polarity.Neutral), //
	POSITIVE_HYDROGEN("[M+H]+", "H", 1.00728, Polarity.Positive), //
	NEGATIVE_HYDROGEN("[M-H]-", "H-1", -1.00728, Polarity.Negative), //
	POTASSIUM("[M+K]+", "K", 38.96316, Polarity.Positive), //
	SODIUM("[M+Na]+", "Na", 22.98922, Polarity.Positive), //
	AMMONIUM("[M+NH4]+", "NH4", 18.03383, Polarity.Positive), //
	CARBONATE("[M+CO3]-", "CO3", 59.98529, Polarity.Negative), //
	PHOSPHATE("[M+H2PO4]-", "H2PO4", 96.96962, Polarity.Negative);

	private final String name, adductFormula;
	private final Polarity polarity;
	private final double addedMass;

	IonizationType(String name, String adductFormula, double addedMass,
			Polarity polarity) {

		this.name = name;
		this.adductFormula = adductFormula;
		this.addedMass = addedMass;
		this.polarity = polarity;
	}

	public String getAdduct() {
		return adductFormula;
	}

	public double getAddedMass() {
		return addedMass;
	}

	public Polarity getPolarity() {
		return polarity;
	}

	public String toString() {
		return name;
	}

}
