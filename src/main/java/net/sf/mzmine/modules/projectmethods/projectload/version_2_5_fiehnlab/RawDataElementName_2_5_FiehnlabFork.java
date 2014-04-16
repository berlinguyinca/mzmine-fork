package net.sf.mzmine.modules.projectmethods.projectload.version_2_5_fiehnlab;

enum RawDataElementName_2_5_FiehnlabFork {

	RAWDATA("rawdata"), NAME("name"), QUANTITY_SCAN("num_scans"), ID("id"), SCAN(
			"scan"), SCAN_ID("id"), MS_LEVEL("mslevel"), QUANTITY_FRAGMENT_SCAN(
			"fragmentscans"), FRAGMENT_SCAN("fragmentscan"), QUANTITY(
			"quantity"), PARENT_SCAN("parent"), PRECURSOR_MZ("precursor_mz"), PRECURSOR_CHARGE(
			"precursor_charge"), RETENTION_TIME("rt"), CENTROIDED("centroid"), QUANTITY_DATAPOINTS(
			"num_dp"), MASS_LIST("mass_list"), STORED_DATAPOINTS(
			"stored_datapoints"), STORED_DATA("stored_data"), STORAGE_ID(
			"storage_id"),

	CORRECTED_SPECTRUM("corrected_spectrum"), RETENTION_INDEX("retention_index"), ORIGINAL_RETENTION_TIME(
			"original_retention_time"), UNIQUE_MASS("unique_mass"), CORRECTION_FIT(
			"correction_fit"), CORRECTION_FIT_DATA("correction_fit_data"), XDATA(
			"xdata"), YDATA("ydata"), CORRECTION_RESULTS("correction_results"), CORRECTION_DATA(
			"correction_data");

	private String elementName;

	private RawDataElementName_2_5_FiehnlabFork(String itemName) {
		this.elementName = itemName;
	}

	public String getElementName() {
		return elementName;
	}
}
