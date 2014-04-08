package net.sf.mzmine.modules.deconvolutedanalysis.famealignment.table;

import net.sf.mzmine.data.PeakList;
import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.util.components.ColumnGroup;
import net.sf.mzmine.util.components.GroupableTableHeader;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Enumeration;

public class ResultsListTableColumnModel extends DefaultTableColumnModel
		implements
			MouseListener {

	private static final Font editFont = new Font("SansSerif", Font.PLAIN, 10);
	private final Color alternateBackground = new Color(237, 247, 255);

	private final FormattedCellRenderer rtRenderer;
	private final DefaultTableCellRenderer defaultRenderer;

	private final PeakList peakList;
	final ResultsListTableModel tableModel;
	private final GroupableTableHeader header;

	private TableColumn columnBeingResized;
	private final int[] columnWidths;

	ResultsListTableColumnModel(GroupableTableHeader header,
			ResultsListTableModel tableModel, PeakList peakList) {

		this.peakList = peakList;
		this.tableModel = tableModel;
		this.header = header;

		header.addMouseListener(this);

		// prepare formatters
		NumberFormat rtFormat = MZmineCore.getConfiguration().getRTFormat();

		// prepare cell renderers
		rtRenderer = new FormattedCellRenderer(rtFormat, alternateBackground);

		defaultRenderer = new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value,
						isSelected, hasFocus, row, column);

				c.setBackground(isSelected
						? table.getSelectionBackground()
						: (row % 2 == 0)
								? table.getBackground()
								: alternateBackground);

				setBorder(BorderFactory.createCompoundBorder(getBorder(),
						FormattedCellRenderer.padding));
				c.setForeground(Color.BLACK);
				return c;
			}
		};
		defaultRenderer.setHorizontalAlignment(SwingConstants.LEFT);

		// Define column widths
		columnWidths = new int[tableModel.getColumnCount()];
		Arrays.fill(columnWidths, 110);
		columnWidths[0] = 50;
		columnWidths[1] = 80;
	}

	public void createColumns() {
		// clear column groups
		ColumnGroup groups[] = header.getColumnGroups();
		if (groups != null) {
			for (ColumnGroup group : groups)
				header.removeColumnGroup(group);
		}

		// clear the column model
		while (getColumnCount() > 0)
			removeColumn(getColumn(0));

		// create the "average" group
		ColumnGroup averageGroup = new ColumnGroup("Average");
		header.addColumnGroup(averageGroup);

		JTextField editorField = new JTextField();
		editorField.setFont(editFont);

		for (int i = 0; i < CommonColumnType.values().length; i++) {
			CommonColumnType commonColumn = CommonColumnType.values()[i];

			TableColumn newColumn = new TableColumn(i);
			newColumn.setHeaderValue(commonColumn.getColumnName());
			newColumn.setIdentifier(commonColumn);

			switch (commonColumn) {
				case COMMENT :
					newColumn.setCellRenderer(defaultRenderer);
					break;
				case RI :
					newColumn.setCellRenderer(defaultRenderer);
					break;
				default :
					newColumn.setCellRenderer(defaultRenderer);
					break;
			}

			this.addColumn(newColumn);
			newColumn.setPreferredWidth(columnWidths[i]);
		}

		for (int i = 0; i < peakList.getNumberOfRawDataFiles(); i++) {
			RawDataFile dataFile = peakList.getRawDataFile(i);
			ColumnGroup fileGroup = new ColumnGroup(dataFile.getName());
			header.addColumnGroup(fileGroup);

			for (int j = 0; j < DataFileColumnType.values().length; j++) {

				DataFileColumnType dataFileColumn = DataFileColumnType.values()[j];
				int modelIndex = CommonColumnType.values().length
						+ (i * DataFileColumnType.values().length) + j;

				TableColumn newColumn = new TableColumn(modelIndex);
				newColumn.setHeaderValue(dataFileColumn.getColumnName());
				newColumn.setIdentifier(dataFileColumn);

				switch (dataFileColumn) {
					case RT :
						newColumn.setCellRenderer(rtRenderer);
						break;
					default :
						newColumn.setCellRenderer(defaultRenderer);
						break;
				}

				this.addColumn(newColumn);
				newColumn.setPreferredWidth(columnWidths[modelIndex]);
				fileGroup.add(newColumn);
			}

		}

	}

	/**
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
	}

	/**
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {
	}

	/**
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
		columnBeingResized = header.getResizingColumn();
	}

	/**
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {

		if (columnBeingResized == null)
			return;

		final int modelIndex = columnBeingResized.getModelIndex();
		final int newWidth = columnBeingResized.getPreferredWidth();

		final int numOfCommonColumns = CommonColumnType.values().length;
		final int numOfDataFileColumns = DataFileColumnType.values().length;

		if (modelIndex < numOfCommonColumns) {
			columnWidths[modelIndex] = newWidth;
		} else {
			// set same width to other data file columns of this type
			int dataFileColumnIndex = (modelIndex - numOfCommonColumns)
					% numOfDataFileColumns;
			int index = numOfCommonColumns + dataFileColumnIndex;

			for (int i = 0; i < peakList.getNumberOfRawDataFiles(); i++, index += numOfDataFileColumns)
				columnWidths[index] = newWidth;
		}
	}

	public TableColumn getColumnByModelIndex(int modelIndex) {
		Enumeration<TableColumn> allColumns = this.getColumns();
		while (allColumns.hasMoreElements()) {
			TableColumn col = allColumns.nextElement();
			if (col.getModelIndex() == modelIndex)
				return col;
		}
		return null;
	}
}