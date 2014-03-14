package net.sf.mzmine.modules.deconvolutedanalysis;

/**
 * Enumeration of spectrum ionization types supported by the deconvoluted analysis modules
 */
public enum SpectrumType {
	/**
	 * Electron Ionization
	 */
	EI("EI"),

	/**
	 * Positive Chemical Ionization using Methane
	 */
	METHANE("PCI-Methane"),

	/**
	 * Positive Chemical Ionization using Isobutane
	 */
	ISOBUTANE("PCI-Isobutane");


	/** Name of the ionization type */
	private final String name;


	/**
	 * Sets the name of this spectrum ionization type
	 * @param name name of this spectrum ionization type
	 */
	SpectrumType(String name) {
		this.name = name;
	}

	/**
	 * Returns the name of this spectrum ionization type
	 * @return name of the spectrum ionization type
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the index of this spectrum ionization type by its enumeration ordinal
	 * @return enumeration index
	 */
	public int getIndex() {
		return ordinal();
	}
}