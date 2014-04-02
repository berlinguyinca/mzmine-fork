package net.sf.mzmine.modules.deconvolutedanalysis.famealignment;

import net.sf.mzmine.data.*;
import net.sf.mzmine.util.Range;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Keeps a store of discovered FAME markers. The implementation of
 * `ChromatagraphicPeak` is a hack to allow easy presentation of retention
 * correction results in the MZmine table structure.
 */
public class FameCorrection implements ChromatographicPeak {
	/** Data file for analysis */
	private RawDataFile dataFile;

	/** Library retention index of detected FAME marker */
	private int retentionIndex;

	/** Experimental retention time of detected FAME marker */
	private double retentionTime;

	/**
	 * 
	 * @param dataFile
	 * @param retentionTime
	 * @param retentionIndex
	 */
	public FameCorrection(RawDataFile dataFile, double retentionTime,
			int retentionIndex) {
		this.dataFile = dataFile;
		this.retentionTime = retentionTime;
		this.retentionIndex = retentionIndex;
	}

	@Override
	public @Nonnull
	PeakStatus getPeakStatus() {
		return PeakStatus.UNKNOWN;
	}

	@Override
	public double getMZ() {
		return -1;
	}

	@Override
	public double getRT() {
		return -1;
	}

	public int getSpectrumNumber() {
		return -1;
	}

	public double getRetentionIndex() {
		return retentionIndex;
	}

	public double getRetentionTime() {
		return retentionTime;
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
		return new int[0];
	}

	@Override
	public int getRepresentativeScanNumber() {
		return -1;
	}

	@Nullable
	@Override
	public DataPoint getDataPoint(int scanNumber) {
		return null;
	}

	@Nonnull
	@Override
	public Range getRawDataPointsRTRange() {
		return new Range(0, 0);
	}

	@Nonnull
	@Override
	public Range getRawDataPointsMZRange() {
		return new Range(0, 0);
	}

	@Nonnull
	@Override
	public Range getRawDataPointsIntensityRange() {
		return new Range(0, 0);
	}

	@Override
	public int getMostIntenseFragmentScanNumber() {
		return -1;
	}

	@Nullable
	@Override
	public IsotopePattern getIsotopePattern() {
		return null;
	}

	@Override
	public void setIsotopePattern(@Nonnull IsotopePattern isotopePattern) {
	}

	@Override
	public int getCharge() {
		return -1;
	}

	@Override
	public void setCharge(int charge) {
	}
}
