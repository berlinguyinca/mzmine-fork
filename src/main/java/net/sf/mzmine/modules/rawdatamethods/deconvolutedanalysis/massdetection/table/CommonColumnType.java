package net.sf.mzmine.modules.rawdatamethods.deconvolutedanalysis.massdetection.table;

import net.sf.mzmine.data.PeakIdentity;

public enum CommonColumnType {
	ROWID("ID", Integer.class), MZ("Mass", Double.class), AVERAGERT(
			"Avg. Retention Time", Double.class), IDENTITY("Identity",
			PeakIdentity.class), COMMENT("Comment", String.class);

	private final String columnName;
	private final Class columnClass;

	CommonColumnType(String columnName, Class columnClass) {
		this.columnName = columnName;
		this.columnClass = columnClass;
	}

	public String getColumnName() {
		return columnName;
	}
	public Class getColumnClass() {
		return columnClass;
	}
	public String toString() {
		return columnName;
	}
}
