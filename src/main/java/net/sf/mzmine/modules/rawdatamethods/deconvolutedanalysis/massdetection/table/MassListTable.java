package net.sf.mzmine.modules.rawdatamethods.deconvolutedanalysis.massdetection.table;

import net.sf.mzmine.data.PeakList;
import net.sf.mzmine.modules.visualization.peaklist.table.PeakListTableModel;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.util.components.ComponentToolTipManager;
import net.sf.mzmine.util.components.GroupableTableHeader;

import javax.swing.*;
import javax.swing.event.RowSorterEvent;
import javax.swing.table.TableRowSorter;
import java.awt.*;

public class MassListTable extends JTable {
	static final String EDIT_IDENTITY = "Edit";
	static final String REMOVE_IDENTITY = "Remove";
	static final String NEW_IDENTITY = "Add new...";

	private static final Font comboFont = new Font("SansSerif", Font.PLAIN, 10);

	private MassListTableModel pkTableModel;
	private PeakList peakList;
	private TableRowSorter<MassListTableModel> sorter;
	private MassListTableColumnModel cm;
	private DefaultCellEditor currentEditor = null;

	public MassListTable(MassListTableWindow window, PeakList peakList) {
		this.peakList = peakList;
		this.pkTableModel = new MassListTableModel(peakList);

		this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		this.setAutoCreateColumnsFromModel(false);
		setModel(pkTableModel);

		GroupableTableHeader header = new GroupableTableHeader();
		setTableHeader(header);

		cm = new MassListTableColumnModel(header, pkTableModel, peakList);
		cm.setColumnMargin(0);
		setColumnModel(cm);

		// create default columns
		cm.createColumns();

		// Initialize sorter
		sorter = new TableRowSorter<MassListTableModel>(pkTableModel);
		setRowSorter(sorter);

		setRowHeight(25);
	}

	public PeakList getPeakList() {
		return peakList;
	}

	/**
	 * When user sorts the table, we have to cancel current combobox for
	 * identity selection. Unfortunately, this doesn't happen automatically.
	 */
	public void sorterChanged(RowSorterEvent e) {
		if (currentEditor != null)
			currentEditor.stopCellEditing();

		super.sorterChanged(e);
	}
}