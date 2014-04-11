package net.sf.mzmine.modules.deconvolutedanalysis.massdetection;

import edu.ucdavis.genomics.metabolomics.util.math.CombinedRegression;
import net.sf.mzmine.data.*;
import net.sf.mzmine.data.impl.SimpleDataPoint;
import net.sf.mzmine.modules.deconvolutedanalysis.CorrectedSpectrum;
import net.sf.mzmine.modules.peaklistmethods.identification.adductsearch.AdductType;
import net.sf.mzmine.modules.deconvolutedanalysis.SpectrumType;
import net.sf.mzmine.util.Range;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MassCandidate implements ChromatographicPeak {
	/** Data file containing this mass candidate */
	private final RawDataFile dataFile;

	/** Spectrum containing this mass candidate */
	private final CorrectedSpectrum spectrum;

	/** Spectrum number continaing this mass candidate */
	private final int spectrumNumber;

	/** Corrected retention time of this mass candidate */
	private final double retentionTime;

	/** Original, uncorrected retention time of this mass candidate */
	private final double originalRetentionTime;

	/** Predicted m/z value of this mass candidate */
	private final int ionMass;

	/** Data point corresponding to this mass candidate */
	private final DataPoint dataPoint;

	/** Ionization type of this file */
	private final SpectrumType ionizationType;

	/** Retention correction for this file */
	private final CombinedRegression fit;

	/**
	 * Isotope pattern. Null by default but can be set later by deisotoping
	 * method.
	 */
	private IsotopePattern isotopePattern;

	/** Charge of ion */
	private int charge = 1;

	/** Adduct matches */
	private final AdductType[] adductMatches;

	/** Adduct matches in string format */
	private final String adductsString;

	public MassCandidate(RawDataFile dataFile, int spectrumNumber,
			Scan spectrum, int ionMass, SpectrumType ionizationType,
			List<AdductType> adductMatches) {

		this.dataFile = dataFile;
		this.spectrumNumber = spectrumNumber;
		this.spectrum = (CorrectedSpectrum) spectrum;
		this.retentionTime = this.spectrum.getRetentionTime();
		this.originalRetentionTime = this.spectrum.getOriginalRetentionTime();
		this.ionMass = ionMass;
		this.ionizationType = ionizationType;
		this.adductMatches = adductMatches.toArray(new AdductType[adductMatches
				.size()]);
		this.dataPoint = new SimpleDataPoint(ionMass, retentionTime);

		fit = this.spectrum.getRetentionCorrection();

		// Generate string representation of adduct matches
		StringBuilder sb = new StringBuilder();

		for (AdductType a : adductMatches)
			sb.append(a.getName()).append(" ");

		if (adductMatches.size() > 0)
			sb.deleteCharAt(sb.length() - 1);
		adductsString = sb.toString();
	}

	public Scan getSpectrum() {
		return spectrum;
	}

	@Nonnull
	@Override
	public PeakStatus getPeakStatus() {
		return PeakStatus.UNKNOWN;
	}

	@Override
	public double getMZ() {
		return ionMass;
	}

	@Override
	public double getRT() {
		return retentionTime;
	}

	public int getSpectrumNumber() {
		return spectrumNumber;
	}

	public double getRetentionTime() {
		return retentionTime;
	}

	public double getOriginalRetentionTime() {
		return originalRetentionTime;
	}

	public int getRetentionIndex() {
		if (spectrum instanceof CorrectedSpectrum)
			return ((CorrectedSpectrum) spectrum).getRetentionIndex();
		else
			return Integer.MIN_VALUE;
	}

	public int getIonMass() {
		return ionMass;
	}

	public SpectrumType getIonizationType() {
		return ionizationType;
	}

	public CombinedRegression getFit() {
		return fit;
	}

	@Override
	public double getHeight() {
		return 0;
	}

	@Override
	public double getArea() {
		return 0;
	}

	@Nonnull
	@Override
	public RawDataFile getDataFile() {
		return dataFile;
	}

	@Nonnull
	@Override
	public int[] getScanNumbers() {
		return new int[]{spectrumNumber};
	}

	@Override
	public int getRepresentativeScanNumber() {
		return spectrumNumber;
	}

	@Nullable
	@Override
	public DataPoint getDataPoint(int scanNumber) {
		return (scanNumber == this.spectrumNumber) ? dataPoint : null;
	}

	@Nonnull
	@Override
	public Range getRawDataPointsRTRange() {
		return new Range(retentionTime, retentionTime);
	}

	@Nonnull
	@Override
	public Range getRawDataPointsMZRange() {
		return new Range(ionMass, ionMass);
	}

	@Nonnull
	@Override
	public Range getRawDataPointsIntensityRange() {
		return new Range(0, 0);
	}

	@Override
	public int getMostIntenseFragmentScanNumber() {
		return spectrumNumber;
	}

	@Nullable
	@Override
	public IsotopePattern getIsotopePattern() {
		return isotopePattern;
	}

	@Override
	public void setIsotopePattern(@Nonnull IsotopePattern isotopePattern) {
		this.isotopePattern = isotopePattern;
	}

	@Override
	public int getCharge() {
		return charge;
	}

	@Override
	public void setCharge(int charge) {
		this.charge = charge;
	}

	public AdductType[] getAdductMatches() {
		return adductMatches;
	}

	public String getAdductsString() {
		return adductsString;
	}

	public String toString() {
		return String.valueOf(retentionTime + " "
				+ ((CorrectedSpectrum) spectrum).getRetentionIndex());
	}
}
