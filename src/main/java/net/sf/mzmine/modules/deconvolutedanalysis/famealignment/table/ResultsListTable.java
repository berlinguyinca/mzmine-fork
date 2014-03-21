package net.sf.mzmine.modules.deconvolutedanalysis.famealignment.table;

import net.sf.mzmine.data.PeakList;
import net.sf.mzmine.util.components.GroupableTableHeader;

import javax.swing.*;
import javax.swing.table.TableRowSorter;

public class ResultsListTable extends JTable {
	private ResultsListTableModel pkTableModel;
	private TableRowSorter<ResultsListTableModel> sorter;
	private ResultsListTableColumnModel cm;

	public ResultsListTable(ResultsListTableWindow window,
			final PeakList resultsList) {
		this.pkTableModel = new ResultsListTableModel(resultsList);

		this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		this.setAutoCreateColumnsFromModel(false);
		setModel(pkTableModel);

		GroupableTableHeader header = new GroupableTableHeader();
		setTableHeader(header);

		cm = new ResultsListTableColumnModel(header, pkTableModel, resultsList);
		cm.setColumnMargin(0);
		setColumnModel(cm);

		// create default columns
		cm.createColumns();

		// Initialize sorter
		sorter = new TableRowSorter<ResultsListTableModel>(pkTableModel);
		setRowSorter(sorter);

		setRowHeight(25);
	}
}