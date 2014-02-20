package net.sf.mzmine.modules.rawdatamethods.deconvolutedanalysis.massdetection;

import net.sf.mzmine.data.*;
import net.sf.mzmine.data.impl.SimpleDataPoint;
import net.sf.mzmine.modules.peaklistmethods.identification.adductsearch.AdductType;
import net.sf.mzmine.modules.rawdatamethods.deconvolutedanalysis.SpectrumType;
import net.sf.mzmine.util.Range;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MassCandidate implements ChromatographicPeak {
	private RawDataFile dataFile;
	private int spectrumNumber;
	private double retentionTime;
	private int ionMass;
	private DataPoint dataPoint;
	private SpectrumType ionizationType;

	// Isotope pattern. Null by default but can be set later by deisotoping
	// method.
	private IsotopePattern isotopePattern;
	private int charge = 0;

	// Adduct matches
	private AdductType[] adductMatches;
	private String adductsString;

	public MassCandidate(RawDataFile dataFile, int spectrumNumber,
			double retentionTime, int ionMass, SpectrumType ionizationType,
			List<AdductType> adductMatches) {

		this.dataFile = dataFile;
		this.spectrumNumber = spectrumNumber;
		this.retentionTime = retentionTime;
		this.ionMass = ionMass;
		this.ionizationType = ionizationType;
		this.adductMatches = adductMatches.toArray(new AdductType[0]);
		this.dataPoint = new SimpleDataPoint(ionMass, retentionTime);

		// Generate string representation of adduct matches
		StringBuilder sb = new StringBuilder();

		for (AdductType a : adductMatches)
			sb.append(a.getName()).append(" ");

		if (adductMatches.size() > 0)
			sb.deleteCharAt(sb.length() - 1);
		adductsString = sb.toString();
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

	public int getIonMass() {
		return ionMass;
	}

	public SpectrumType getIonizationType() {
		return ionizationType;
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
}
