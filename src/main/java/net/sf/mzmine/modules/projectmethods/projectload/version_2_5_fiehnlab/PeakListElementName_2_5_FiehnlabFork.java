package net.sf.mzmine.modules.projectmethods.projectload.version_2_5_fiehnlab;

public enum PeakListElementName_2_5_FiehnlabFork {

	PEAKLIST("peaklist"), PEAKLIST_DATE("created"), QUANTITY("quantity"), RAWFILE(
			"raw_file"), PEAKLIST_NAME("pl_name"), ID("id"), RT("rt"), MZ("mz"), HEIGHT(
			"height"), RTRANGE("rt_range"), MZRANGE("mz_range"), AREA("area"), STATUS(
			"status"), COLUMN("column_id"), SCAN_ID("scan_id"), ROW("row"), PEAK_IDENTITY(
			"identity"), PREFERRED("preferred"), IDPROPERTY("identity_property"), NAME(
			"name"), COMMENT("comment"), PEAK("peak"), ISOTOPE_PATTERN(
			"isotope_pattern"), DESCRIPTION("description"), CHARGE("charge"), ISOTOPE(
			"isotope"), MZPEAKS("mzpeaks"), METHOD("applied_method"), METHOD_NAME(
			"method_name"), METHOD_PARAMETERS("method_parameters"), REPRESENTATIVE_SCAN(
			"best_scan"), FRAGMENT_SCAN("fragment_scan");

	private String elementName;

	private PeakListElementName_2_5_FiehnlabFork(String itemName) {
		this.elementName = itemName;
	}

	public String getElementName() {
		return elementName;
	}
}
