package net.sf.mzmine.modules.deconvolutedanalysis.famealignment.table;

public enum CommonColumnType {
	ROWID("ID", Integer.class), COMMENT("FAME ID", String.class), RI(
			"Retention Index", Integer.class);

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
	public String toString() { return columnName; }
}
