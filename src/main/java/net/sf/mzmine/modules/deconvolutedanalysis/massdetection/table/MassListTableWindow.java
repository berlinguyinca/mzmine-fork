package net.sf.mzmine.modules.deconvolutedanalysis.massdetection.table;

import net.sf.mzmine.data.PeakList;
import net.sf.mzmine.main.MZmineCore;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;

public class MassListTableWindow extends JInternalFrame
		implements
			ActionListener {
	/**
	 * Table containing mass candidate information.
	 */
	private MassListTable table;

	/**
	 * Default constructor, creating an empty table.
	 * 
	 * @param massList
	 *            `PeakList` object containing mass candidates
	 */
	public MassListTableWindow(PeakList massList) {
		super("Multi-ionization Spectra Match Candidates", true, true, true,
				true);

		setResizable(true);
		setIconifiable(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setBackground(Color.white);

		// Build toolbar
		MassListTableToolBar toolBar = new MassListTableToolBar(this);
		add(toolBar, BorderLayout.EAST);

		// Build table
		table = new MassListTable(massList);

		add(new JScrollPane(table), BorderLayout.CENTER);
		pack();
	}

	/**
	 * Methods for ActionListener interface implementation
	 * 
	 * @param event
	 *            performed event
	 */
	public void actionPerformed(ActionEvent event) {
		String command = event.getActionCommand();

		if (command.equals("Export as CSV")) {

		}

		if (command.equals("Print")) {
			try {
				table.print(JTable.PrintMode.FIT_WIDTH);
			} catch (PrinterException e) {
				MZmineCore.getDesktop().displayException(e);
			}
		}
	}
}
