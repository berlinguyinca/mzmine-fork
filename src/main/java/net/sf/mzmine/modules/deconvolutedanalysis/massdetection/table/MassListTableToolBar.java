package net.sf.mzmine.modules.deconvolutedanalysis.massdetection.table;

import net.sf.mzmine.util.GUIUtils;

import javax.swing.*;
import java.awt.*;

class MassListTableToolBar extends JToolBar {
	private static final Icon propertiesIcon = new ImageIcon(
			"icons/propertiesicon.png");
	private static final Icon printIcon = new ImageIcon("icons/printicon.png");

	MassListTableToolBar(MassListTableWindow masterFrame) {
		super(JToolBar.VERTICAL);

		setFloatable(false);
		setMargin(new Insets(5, 5, 5, 5));
		setBackground(Color.white);

		GUIUtils.addButton(this, null, propertiesIcon, masterFrame,
				"Export as CSV", "Export as CSV");

		addSeparator();

		GUIUtils.addButton(this, null, printIcon, masterFrame, "Print", "Print");
	}
}
