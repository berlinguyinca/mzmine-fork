package net.sf.mzmine.modules.deconvolutedanalysis.famealignment.table;

import net.sf.mzmine.data.PeakList;
import net.sf.mzmine.modules.deconvolutedanalysis.famealignment.FameData;
import net.sf.mzmine.modules.visualization.tic.PlotType;
import net.sf.mzmine.modules.visualization.tic.TICVisualizerModule;
import net.sf.mzmine.util.Range;
import net.sf.mzmine.util.components.GroupableTableHeader;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ResultsListTable extends JTable {
	private final ResultsListTableModel pkTableModel;
	private final TableRowSorter<ResultsListTableModel> sorter;
	private final ResultsListTableColumnModel cm;
	private final PeakList resultsList;

	public ResultsListTable(final PeakList resultsList) {
		this.pkTableModel = new ResultsListTableModel(resultsList);
		this.resultsList = resultsList;

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

		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					JTable table = (JTable) e.getSource();
					viewChromatogram(table.getSelectedRow());
				}
			}
		});
	}

	private void viewChromatogram(int row) {
		// Library retention time for this FAME marker
		double rt = FameData.FAME_RETENTION_TIMES[row];

		// Tine window in minutes
		double window = 0.5;

		TICVisualizerModule.showNewTICVisualizerWindow(
				resultsList.getRawDataFiles(), null, null, 1,
				PlotType.BASEPEAK, new Range(rt - window, rt + window),
				new Range(0, 600));
	}
}