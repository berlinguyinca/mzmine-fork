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

package net.sf.mzmine.modules.batchmode;

import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.modules.MZmineModule;
import net.sf.mzmine.modules.MZmineProcessingModule;
import net.sf.mzmine.modules.MZmineProcessingStep;
import net.sf.mzmine.modules.impl.MZmineProcessingStepImpl;
import net.sf.mzmine.parameters.ParameterSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Collection;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * Batch steps queue
 */
public class BatchQueue
		extends
			Vector<MZmineProcessingStep<MZmineProcessingModule>> {

	// Batch step element name.
	private static final String BATCH_STEP_ELEMENT = "batchstep";

	// Method element name.
	private static final String METHOD_ELEMENT = "method";

	@Override
	public BatchQueue clone() {

		// Clone the parameters.
		final BatchQueue clonedQueue = new BatchQueue();
		for (final MZmineProcessingStep<MZmineProcessingModule> step : this) {
			final ParameterSet parameters = step.getParameterSet();
			final MZmineProcessingStepImpl<MZmineProcessingModule> stepCopy = new MZmineProcessingStepImpl<MZmineProcessingModule>(
					step.getModule(), parameters.cloneParameter());
			clonedQueue.add(stepCopy);
		}
		return clonedQueue;
	}

	/**
	 * De-serialize from XML.
	 * 
	 * @param xmlElement
	 *            the element that holds the XML.
	 * @return the de-serialized value.
	 */
	public static BatchQueue loadFromXml(final Element xmlElement) {

		Logger logger = Logger.getLogger(BatchQueue.class.getName());
		// Create an empty queue.
		final BatchQueue queue = new BatchQueue();

		// Get the loaded modules.
		final Collection<MZmineModule> allModules = MZmineCore.getAllModules();

		logger.info("loaded " + allModules.size() + " modules...");
		// Process the batch step elements.
		final NodeList nodes = xmlElement
				.getElementsByTagName(BATCH_STEP_ELEMENT);
		final int nodesLength = nodes.getLength();
		for (int i = 0; i < nodesLength; i++) {

			final Element stepElement = (Element) nodes.item(i);
			final String methodName = stepElement.getAttribute(METHOD_ELEMENT);

			logger.info("trying to load method: " + methodName);
			// Find a matching module.
			for (final MZmineModule module : allModules) {

				if (module instanceof MZmineProcessingModule
						&& module.getClass().getName().equals(methodName)) {

					logger.info("success, now loading parameters...");
					// Get parameters and add step to queue.
					final ParameterSet parameterSet = MZmineCore
							.getConfiguration().getModuleParameters(
									module.getClass());
					final ParameterSet methodParams = parameterSet
							.cloneParameter();

					logger.fine("method parameters are of type: "
							+ methodParams.getClass().getName());
					methodParams.loadValuesFromXML(stepElement);
					queue.add(new MZmineProcessingStepImpl<MZmineProcessingModule>(
							(MZmineProcessingModule) module, methodParams));
					break;
				} else {
					// logger.finest("=> was of wrong type or name didn't match...");
				}
			}
		}

		return queue;
	}

	/**
	 * Serialize to XML.
	 * 
	 * @param xmlElement
	 *            the XML element to append to.
	 */
	public void saveToXml(final Element xmlElement) {

		final Document document = xmlElement.getOwnerDocument();

		// Process each step.
		for (final MZmineProcessingStep step : this) {

			// Append a new batch step element.
			final Element stepElement = document
					.createElement(BATCH_STEP_ELEMENT);
			stepElement.setAttribute(METHOD_ELEMENT, step.getModule()
					.getClass().getName());
			xmlElement.appendChild(stepElement);

			// Save parameters.
			final ParameterSet parameters = step.getParameterSet();
			if (parameters != null) {
				parameters.saveValuesToXML(stepElement);
			}
		}
	}
}