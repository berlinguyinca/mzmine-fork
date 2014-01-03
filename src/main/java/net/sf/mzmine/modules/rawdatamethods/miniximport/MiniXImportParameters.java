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

import net.sf.mzmine.parameters.Parameter;
import net.sf.mzmine.parameters.impl.SimpleParameterSet;
import net.sf.mzmine.parameters.parametertypes.StringParameter;

import java.util.logging.Logger;

public class MiniXImportParameters extends SimpleParameterSet {

	private Logger logger = Logger.getLogger(this.getClass().getName());

	public static final StringParameter MINIX_ID_PARAMETER = new StringParameter(
			"Study Id", "this is your MiniX study id");

	public static final StringParameter MINIX_URL_PARAMETER = new StringParameter(
			"MiniX url", "this is your MiniX url",
			"http://minix.fiehnlab.ucdavis.edu");

	public static final StringParameter MINIX_RAWDATA_URL = new StringParameter(
			"Raw Data Provider", "this is your rawdata provider url",
			"http://data.fiehnlab.ucdavis.edu");

	public MiniXImportParameters() {
		super(new Parameter[]{MINIX_ID_PARAMETER, MINIX_URL_PARAMETER,
				MINIX_RAWDATA_URL});
	}

}
