package net.sf.mzmine.modules.deconvolutedanalysis;

import edu.ucdavis.genomics.metabolomics.util.math.CombinedRegression;
import net.sf.mzmine.data.DataPoint;
import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.data.Scan;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.project.impl.RawDataFileImpl;
import net.sf.mzmine.project.impl.StorableScan;
import net.sf.mzmine.util.ScanUtils;

import java.text.Format;

public class CorrectedSpectrum extends StorableScan {
	/** Retention index obtained from FAME correction */
	private int retentionIndex;

	/** Regression algorithm for retention index correction */
	private CombinedRegression fit;

	/** Secondary base peak */
	private DataPoint secondaryBasePeak = null;


	public CorrectedSpectrum(Scan sc, RawDataFile dataFile, int storageID) {
		super(sc, (RawDataFileImpl)dataFile, sc.getNumberOfDataPoints(), storageID);
		this.retentionIndex = (sc instanceof CorrectedSpectrum)
				? ((CorrectedSpectrum) sc).getRetentionIndex()
				: -1;
	}

	public CorrectedSpectrum(RawDataFile dataFile, int storageID,
	                         int spectrumNumber, double retentionTime, DataPoint[] dataPoints) {
		super((RawDataFileImpl)dataFile, storageID, dataPoints.length, spectrumNumber, 1, retentionTime, -1, 0.0, 1, null, ScanUtils.isCentroided(dataPoints));
		this.retentionIndex = -1;
	}

	public CorrectedSpectrum(RawDataFile dataFile, int storageID,
	                         int spectrumNumber, double retentionTime, int retentionIndex,
	                         DataPoint[] dataPoints) {
		super((RawDataFileImpl)dataFile, storageID, dataPoints.length, spectrumNumber, 1, retentionTime, -1, 0.0, 1, null, ScanUtils.isCentroided(dataPoints));
		this.retentionIndex = retentionIndex;
	}

	/**
	 * Get the retention index of this spectrum.
	 * 
	 * @return retention index
	 */
	public int getRetentionIndex() {
		return retentionIndex;
	}

	/**
	 * Sets the retention index of this spectrum.
	 * 
	 * @param retentionIndex
	 *            retention index
	 */
	public void setRetentionIndex(int retentionIndex) {
		this.retentionIndex = retentionIndex;
	}

	/**
	 * Identifies whether this spectrum has had a retention index applied.
	 * 
	 * @return whether a retention index exists
	 */
	public boolean isRetentionCorrected() {
		return retentionIndex >= 0;
	}

	public CombinedRegression getRetentionCorrection() {
		return fit;
	}

	public void setRetentionCorrection(CombinedRegression fit) {
		this.fit = fit;
	}

	/**
	 * Return the data point of the ion with the second highest abundance
	 * @return data point corresponding to the secondary base peak
	 */
	public DataPoint getSecondaryBasePeak() {
		if(secondaryBasePeak == null && getNumberOfDataPoints() > 1) {
			secondaryBasePeak = getDataPoints()[0];

			for(DataPoint p : getDataPoints())
				if(p.getIntensity() > secondaryBasePeak.getIntensity() && p.getIntensity() < getBasePeak().getIntensity())
					secondaryBasePeak = p;
		}

		return secondaryBasePeak;
	}

	@Override
	public String toString() {
		Format rtFormat = MZmineCore.getConfiguration().getRTFormat();
		Format intensityFormat = MZmineCore.getConfiguration().getIntensityFormat();

		StringBuilder sb = new StringBuilder();
		sb.append("#");
		sb.append(getScanNumber());
		sb.append(" @");
		sb.append(rtFormat.format(getRetentionTime()));
		sb.append(" ");
		sb.append(intensityFormat.format(getBasePeak().getIntensity()));

		return sb.toString();
	}
}
