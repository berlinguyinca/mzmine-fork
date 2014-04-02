package net.sf.mzmine.modules.deconvolutedanalysis.famealignment.table;

import net.sf.mzmine.data.*;
import net.sf.mzmine.modules.deconvolutedanalysis.famealignment.FameCorrection;
import net.sf.mzmine.modules.deconvolutedanalysis.famealignment.FameData;

import javax.swing.table.AbstractTableModel;
import java.util.Arrays;

public class ResultsListTableModel extends AbstractTableModel {
	private PeakList peakList;

	public ResultsListTableModel(PeakList peakList) {
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
				case COMMENT :
					return peakListRow.getComment();
				case RI :
					String name = peakListRow.getComment();
					int idx = Arrays.asList(FameData.FAME_NAMES).indexOf(name);
					return FameData.FAME_RETENTION_INDICES[idx];
			}
		} else {
			RawDataFile file = getColumnDataFile(col);
			FameCorrection correction = (FameCorrection) peakListRow
					.getPeak(file);

			if (correction == null)
				return null;

			switch (getDataFileColumn(col)) {
				case RT :
					return correction.getRetentionTime();
			}
		}

		return null;
	}

	public boolean isCellEditable(int row, int col) {
		return false;
	}

	public void setValueAt(Object value, int row, int col) {
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
