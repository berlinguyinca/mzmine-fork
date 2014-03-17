package net.sf.mzmine.modules.deconvolutedanalysis.famealignment.table;

import net.sf.mzmine.data.PeakIdentity;
import net.sf.mzmine.data.PeakList;
import net.sf.mzmine.data.PeakListRow;
import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.modules.deconvolutedanalysis.massdetection.MassCandidate;
import net.sf.mzmine.modules.visualization.spectra.SpectraVisualizerModule;
import net.sf.mzmine.util.components.GroupableTableHeader;
import net.sf.mzmine.util.dialogs.PeakIdentitySetupDialog;

import javax.swing.*;
import javax.swing.event.RowSorterEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MassListTable extends JTable {
	static final String EDIT_IDENTITY = "Edit";
	static final String REMOVE_IDENTITY = "Remove";
	static final String NEW_IDENTITY = "Add new...";

	private static final Font comboFont = new Font("SansSerif", Font.PLAIN, 10);

	private MassListTableModel pkTableModel;
	private PeakList peakList;
	private PeakListRow peakListRow;
	private TableRowSorter<MassListTableModel> sorter;
	private MassListTableColumnModel cm;
	private DefaultCellEditor currentEditor = null;

	public MassListTable(MassListTableWindow window, final PeakList peakList) {
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

		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					JTable table = (JTable) e.getSource();
					viewMassSpectrum(table.getSelectedRow(),
							table.getSelectedColumn());
				}
			}
		});
	}

	private void viewMassSpectrum(int row, int col) {
		row = convertRowIndexToModel(row);

		RawDataFile dataFile = pkTableModel.getColumnDataFile(col);
		peakListRow = peakList.getRow(row);
		MassCandidate massCandidate = (MassCandidate) peakListRow
				.getPeak(dataFile);

		if (dataFile != null && massCandidate != null)
			SpectraVisualizerModule.showNewSpectrumWindow(dataFile,
					massCandidate.getSpectrumNumber(), massCandidate);
	}

	public PeakList getPeakList() {
		return peakList;
	}

	public TableCellEditor getCellEditor(int row, int column) {

		CommonColumnType commonColumn = pkTableModel.getCommonColumn(column);
		if (commonColumn == CommonColumnType.IDENTITY) {

			row = this.convertRowIndexToModel(row);
			peakListRow = peakList.getRow(row);

			PeakIdentity identities[] = peakListRow.getPeakIdentities();
			PeakIdentity preferredIdentity = peakListRow
					.getPreferredPeakIdentity();
			JComboBox combo;

			if ((identities != null) && (identities.length > 0)) {
				combo = new JComboBox(identities);
				combo.addItem("-------------------------");
				combo.addItem(REMOVE_IDENTITY);
				combo.addItem(EDIT_IDENTITY);
			} else {
				combo = new JComboBox();
			}

			combo.setFont(comboFont);
			combo.addItem(NEW_IDENTITY);
			if (preferredIdentity != null) {
				combo.setSelectedItem(preferredIdentity);
			}

			combo.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					JComboBox combo = (JComboBox) e.getSource();
					Object item = combo.getSelectedItem();
					if (item != null) {
						if (item.toString() == NEW_IDENTITY) {
							PeakIdentitySetupDialog dialog = new PeakIdentitySetupDialog(
									peakListRow);
							dialog.setVisible(true);
						} else if (item.toString() == EDIT_IDENTITY) {
							PeakIdentitySetupDialog dialog = new PeakIdentitySetupDialog(
									peakListRow, peakListRow
											.getPreferredPeakIdentity());
							dialog.setVisible(true);
						} else if (item.toString() == REMOVE_IDENTITY) {
							PeakIdentity identity = peakListRow
									.getPreferredPeakIdentity();
							if (identity != null) {
								peakListRow.removePeakIdentity(identity);
								DefaultComboBoxModel comboModel = (DefaultComboBoxModel) combo
										.getModel();
								comboModel.removeElement(identity);
							}
						} else if (item instanceof PeakIdentity) {
							peakListRow
									.setPreferredPeakIdentity((PeakIdentity) item);
						}
					}

				}
			});

			// Keep the reference to the editor
			currentEditor = new DefaultCellEditor(combo);

			return currentEditor;
		}

		return super.getCellEditor(row, column);

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