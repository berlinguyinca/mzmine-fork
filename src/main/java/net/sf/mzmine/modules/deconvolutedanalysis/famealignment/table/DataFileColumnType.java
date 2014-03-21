package net.sf.mzmine.modules.deconvolutedanalysis.famealignment.table;

public enum DataFileColumnType {
	RT("Retention Time", Double.class);

	private final String columnName;
	private final Class columnClass;

	DataFileColumnType(String columnName, Class columnClass) {
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