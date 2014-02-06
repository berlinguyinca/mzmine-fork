package net.sf.mzmine.modules.rawdatamethods.deconvolutedanalysis.massdetection;

import net.sf.mzmine.data.*;
import net.sf.mzmine.data.impl.SimpleDataPoint;
import net.sf.mzmine.modules.peaklistmethods.identification.adductsearch.AdductType;
import net.sf.mzmine.modules.rawdatamethods.deconvolutedanalysis.massdetection.MassCandidate;
import net.sf.mzmine.util.Range;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.TreeMap;

public class MassCandidatePeak implements ChromatographicPeak {
	private RawDataFile dataFile;

	// Raw M/Z, RT, Height and Area
	private double mz, rt;

	// Scan number and data point
	private int scanNumber;
	private DataPoint dataPoint;

	// Isotope pattern. Null by default but can be set later by deisotoping
	// method.
	private IsotopePattern isotopePattern;
	private int charge = 0;

	// Adduct matches
	private AdductType[] adductMatches;
	private String adductsString;

	public MassCandidatePeak(MassCandidate massCandidate) {
		dataFile = massCandidate.getDataFile();
		mz = massCandidate.getIonMass();
		rt = massCandidate.getRetentionTime();
		scanNumber = massCandidate.getSpectrumNumber();
		dataPoint = new SimpleDataPoint(mz, rt);
		adductMatches = massCandidate.getAdductMatches();

		// Generate string representation of adduct matches
		StringBuilder sb = new StringBuilder();

		for (AdductType a : adductMatches)
			sb.append(a.getName() + " ");

		if (adductMatches.length > 0)
			sb.deleteCharAt(sb.length() - 1);
		adductsString = sb.toString();
	}

	@Nonnull
	@Override
	public PeakStatus getPeakStatus() {
		return null;
	}

	@Override
	public double getMZ() {
		return mz;
	}

	@Override
	public double getRT() {
		return rt;
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
		return new int[]{scanNumber};
	}

	@Override
	public int getRepresentativeScanNumber() {
		return scanNumber;
	}

	@Nullable
	@Override
	public DataPoint getDataPoint(int scanNumber) {
		return (scanNumber == this.scanNumber) ? dataPoint : null;
	}

	@Nonnull
	@Override
	public Range getRawDataPointsRTRange() {
		return new Range(rt, rt);
	}

	@Nonnull
	@Override
	public Range getRawDataPointsMZRange() {
		return new Range(mz, mz);
	}

	@Nonnull
	@Override
	public Range getRawDataPointsIntensityRange() {
		return new Range(0, 0);
	}

	@Override
	public int getMostIntenseFragmentScanNumber() {
		return scanNumber;
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
		return 0;
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
