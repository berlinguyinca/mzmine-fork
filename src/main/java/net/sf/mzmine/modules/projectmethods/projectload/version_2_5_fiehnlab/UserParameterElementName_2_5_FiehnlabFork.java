package net.sf.mzmine.modules.projectmethods.projectload.version_2_5_fiehnlab;

enum UserParameterElementName_2_5_FiehnlabFork {
	PARAMETERS("parameters"), COUNT("count"), PARAMETER("parameter"), NAME(
			"name"), TYPE("type"), OPTION("option"), VALUE("value"), DATA_FILE(
			"data_file");

	private String elementName;

	private UserParameterElementName_2_5_FiehnlabFork(String itemName) {
		this.elementName = itemName;
	}

	public String getElementName() {
		return elementName;
	}
}
