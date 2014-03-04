package net.sf.mzmine.modules.deconvolutedanalysis.massdetection.table;

import net.sf.mzmine.data.PeakList;

import javax.swing.*;
import java.awt.*;

public class MassListTableWindow extends JInternalFrame {
	public MassListTableWindow(PeakList massList) {
		super("Multi-ionization Spectra Match Candidates", true, true, true,
				true);

		setResizable(true);
		setIconifiable(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setBackground(Color.white);

		// Build table
		MassListTable table = new MassListTable(this, massList);

		add(new JScrollPane(table), BorderLayout.CENTER);
		pack();
	}
}
