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

package net.sf.mzmine.desktop.impl;

import net.sf.mzmine.data.PeakList;
import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.desktop.Desktop;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.parameters.impl.SimpleParameterSet;
import net.sf.mzmine.util.ExitCode;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.event.TreeModelListener;
import java.awt.*;
import java.util.logging.Logger;

public class HeadLessDesktop implements Desktop {

	private static final String MODULE_NAME = "Desktop";

	private Logger logger = Logger.getLogger(this.getClass().getName());

	@Override
	public JFrame getMainFrame() {
		return null;
	}

	@Override
	public void addInternalFrame(JInternalFrame frame) {
		throw new UnsupportedOperationException();
	}

	@Override
	public JInternalFrame[] getInternalFrames() {
		throw new UnsupportedOperationException();
	}

	@Override
	public JInternalFrame getSelectedFrame() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setStatusBarText(String text) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setStatusBarText(String text, Color textColor) {
	}

	@Override
	public void displayMessage(String msg) {
		logger.info(msg);
	}

	@Override
	public void displayMessage(String title, String msg) {
		logger.info(msg);
	}

	@Override
	public void displayErrorMessage(String msg) {
		logger.severe(msg);
	}

	@Override
	public void displayErrorMessage(String title, String msg) {
		logger.severe(msg);
	}

	@Override
	public void displayException(Exception e) {
		e.printStackTrace();
	}

	@Override
	public RawDataFile[] getSelectedDataFiles() {
		throw new UnsupportedOperationException();
	}

	@Override
	public PeakList[] getSelectedPeakLists() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addProjectTreeListener(TreeModelListener listener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeProjectTreeListener(TreeModelListener listener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public @Nonnull
	Class<? extends ParameterSet> getParameterSetClass() {
		return SimpleParameterSet.class;
	}

	@Override
	public @Nonnull
	String getName() {
		return MODULE_NAME;
	}

	@Override
	public @Nonnull
	ExitCode exitMZmine() {
		System.exit(0);
		return ExitCode.OK;
	}

}
