package net.sf.mzmine.modules.rawdatamethods.deconvolutedanalysis.massdetection.table;

public enum DataFileColumnType {
	SPECNUMBER("Spectrum Number", Integer.class), RT("Retention time",
			Double.class), ADDUCTS("Matched Adducts/Losses", String.class);

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