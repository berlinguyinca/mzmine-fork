package net.sf.mzmine.modules.rawdatamethods.deconvolutedanalysis.massdetection.table;

import net.sf.mzmine.data.*;
import net.sf.mzmine.modules.rawdatamethods.deconvolutedanalysis.massdetection.MassCandidate;

import javax.swing.table.AbstractTableModel;

public class MassListTableModel extends AbstractTableModel {
	private PeakList peakList;

	public MassListTableModel(PeakList peakList) {
		this.peakList = peakList;
	}

	public int getColumnCount() {
		return CommonColumnType.values().length
				+ peakList.getNumberOfRawDataFiles()
				* DataFileColumnType.values().length;
	}

	public int getRowCount() {
		return peakList.getNumberOfRows();
	}

	public String getColumnName(int col) {
		return "column" + col;
	}

	public Class<?> getColumnClass(int col) {
		if (isCommonColumn(col)) {
			CommonColumnType commonColumn = getCommonColumn(col);
			return commonColumn.getColumnClass();
		} else {
			DataFileColumnType dataFileColumn = getDataFileColumn(col);
			return dataFileColumn.getColumnClass();
		}
	}

	/**
	 * This method returns the value at given coordinates of the dataset or null
	 * if it is a missing value
	 */
	public Object getValueAt(int row, int col) {
		PeakListRow peakListRow = peakList.getRow(row);

		if (isCommonColumn(col)) {
			switch (getCommonColumn(col)) {
				case ROWID :
					return peakListRow.getID();
				case MZ :
					return peakListRow.getAverageMZ();
				case AVERAGERT :
					return peakListRow.getAverageRT();
				case IDENTITY :
					return peakListRow.getPreferredPeakIdentity();
				case COMMENT :
					return peakListRow.getComment();
			}
		} else {
			RawDataFile file = getColumnDataFile(col);
			ChromatographicPeak peak = peakListRow.getPeak(file);

			if (peak == null)
				return null;

			switch (getDataFileColumn(col)) {
				case SPECNUMBER :
					return peak.getScanNumbers()[0];
				case RT :
					return peak.getRT();
				case ADDUCTS :
					return ((MassCandidate) peak).getAdductsString();
			}
		}

		return null;
	}

	public boolean isCellEditable(int row, int col) {
		CommonColumnType columnType = getCommonColumn(col);
		return ((columnType == CommonColumnType.COMMENT) || (columnType == CommonColumnType.IDENTITY));
	}

	public void setValueAt(Object value, int row, int col) {
		CommonColumnType columnType = getCommonColumn(col);
		PeakListRow peakListRow = peakList.getRow(row);

		if (columnType == CommonColumnType.COMMENT) {
			peakListRow.setComment((String) value);
		}

		if (columnType == CommonColumnType.IDENTITY) {
			if (value instanceof PeakIdentity)
				peakListRow.setPreferredPeakIdentity((PeakIdentity) value);
		}
	}

	boolean isCommonColumn(int col) {
		return (col < CommonColumnType.values().length);
	}

	CommonColumnType getCommonColumn(int col) {
		return isCommonColumn(col) ? CommonColumnType.values()[col] : null;
	}

	DataFileColumnType getDataFileColumn(int col) {
		if (isCommonColumn(col))
			return null;
		else {
			col = (col - CommonColumnType.values().length)
					% DataFileColumnType.values().length;
			return DataFileColumnType.values()[col];
		}
	}

	RawDataFile getColumnDataFile(int col) {
		if (isCommonColumn(col))
			return null;

		col = (col - CommonColumnType.values().length)
				/ DataFileColumnType.values().length;
		return peakList.getRawDataFile(col);
	}
}
