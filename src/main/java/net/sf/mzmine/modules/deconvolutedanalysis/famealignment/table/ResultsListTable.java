package net.sf.mzmine.modules.deconvolutedanalysis.famealignment.table;

import net.sf.mzmine.data.ChromatographicPeak;
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
import java.util.HashMap;

public class ResultsListTable extends JTable {
	private ResultsListTableModel pkTableModel;
	private TableRowSorter<ResultsListTableModel> sorter;
	private ResultsListTableColumnModel cm;
	private PeakList resultsList;

	public ResultsListTable(ResultsListTableWindow window,
			final PeakList resultsList) {
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
					viewChromatagram(table.getSelectedRow());
				}
			}
		});
	}

	private void viewChromatagram(int row) {
		double rt = FameData.FAME_RETENTION_TIMES[row];
		double window = 15 / 60.0;

		TICVisualizerModule.showNewTICVisualizerWindow(
				resultsList.getRawDataFiles(), new ChromatographicPeak[0],
				new HashMap<ChromatographicPeak, String>(), 1,
				PlotType.BASEPEAK, new Range(rt - window, rt + window),
				new Range(86.5, 87.5));
	}
}