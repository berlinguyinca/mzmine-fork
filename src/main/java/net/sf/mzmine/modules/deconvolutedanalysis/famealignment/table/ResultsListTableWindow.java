package net.sf.mzmine.modules.deconvolutedanalysis.famealignment.table;

import net.sf.mzmine.data.PeakList;

import javax.swing.*;
import java.awt.*;

public class ResultsListTableWindow extends JInternalFrame {
	public ResultsListTableWindow(PeakList resultsList) {
		super("Retention Index Correction Results", true, true, true, true);

		setResizable(true);
		setIconifiable(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setBackground(Color.white);

		// Build table
		ResultsListTable table = new ResultsListTable(this, resultsList);

		add(new JScrollPane(table), BorderLayout.CENTER);
		pack();
	}
}