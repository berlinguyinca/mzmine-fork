package net.sf.mzmine.modules.rawdatamethods.deconvolutedanalysis;

import net.sf.mzmine.data.DataPoint;
import net.sf.mzmine.data.Scan;
import net.sf.mzmine.project.impl.RawDataFileImpl;
import net.sf.mzmine.project.impl.StorableScan;
import net.sf.mzmine.util.ScanUtils;

public class DeconvolutedSpectrum extends StorableScan {
	/** Retention index obtained from FAME correction */
	private int retentionIndex;

	public DeconvolutedSpectrum(Scan sc, RawDataFileImpl dataFile, int storageID) {
		super(sc, dataFile, sc.getNumberOfDataPoints(), storageID);
		this.retentionIndex = (sc instanceof DeconvolutedSpectrum)
				? ((DeconvolutedSpectrum) sc).getRetentionIndex()
				: -1;
	}

	public DeconvolutedSpectrum(RawDataFileImpl dataFile, int storageID, int spectrumNumber, double retentionTime, DataPoint[] dataPoints) {
		super(dataFile, storageID, dataPoints.length, spectrumNumber, 1, retentionTime, -1, 0.0, 1, null, ScanUtils.isCentroided(dataPoints));
		this.retentionIndex = -1;
	}

	public DeconvolutedSpectrum(RawDataFileImpl dataFile, int storageID, int spectrumNumber, double retentionTime, int retentionIndex, DataPoint[] dataPoints) {
		super(dataFile, storageID, dataPoints.length, spectrumNumber, 1, retentionTime, -1, 0.0, 1, null, ScanUtils.isCentroided(dataPoints));
		this.retentionIndex = retentionIndex;
	}

	/**
	 * Get the retention index of this spectrum.
	 * @return retention index
	 */
	public int getRetentionIndex() { return retentionIndex; }

	/**
	 * Sets the retention index of this spectrum.
	 * @param retentionIndex retention index
	 */
	public void setRetentionIndex(int retentionIndex) {
		this.retentionIndex = retentionIndex;
	}

	/**
	 * Identifies whether this spectrum has had a retention index applied.
	 * @return whether a retention index exists
	 */
	public boolean isRetentionCorrected() { return retentionIndex >= 0; }
}
