package net.sf.mzmine.modules.deconvolutedanalysis;

import edu.ucdavis.genomics.metabolomics.util.math.CombinedRegression;
import net.sf.mzmine.data.DataPoint;
import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.data.Scan;
import net.sf.mzmine.modules.deconvolutedanalysis.famealignment.FameCorrection;
import net.sf.mzmine.modules.deconvolutedanalysis.famealignment.FameData;
import net.sf.mzmine.project.impl.RawDataFileImpl;
import net.sf.mzmine.project.impl.StorableScan;
import net.sf.mzmine.util.Range;
import net.sf.mzmine.util.ScanUtils;

import java.util.Map;

/**
 * StorableScan with the added functionality of allowing for retention index
 * correction, secondary base peak, and unique mass.
 */
public class CorrectedSpectrum extends StorableScan {
	/**
	 * Retention index obtained from FAME correction
	 */
	private int retentionIndex;

	/**
	 * Retention time corrected to the library standard by using the computed
	 * retention index as input into a reversed, linear-polynomial fit between
	 * the library retention times and indices.
	 * 
	 * @see net.sf.mzmine.modules.deconvolutedanalysis.famealignment.FameData#FAME_INDICES_TO_TIMES
	 */
	private double correctedRetentionTime;

	/**
	 * Integer m/z value of this spectrum's unique mass as determined by ChromaTOF
	 */
	private int uniqueMassValue;

	/**
	 * The unique mass as determined by ChromaTOF
	 */
	private DataPoint uniqueMass = null;

	/**
	 * Regression algorithm for retention index correction
	 */
	private CombinedRegression fit = null;

	/**
	 * Map of retention correction results
	 */
	private Map<String, FameCorrection> correctionResults = null;

	/**
	 * Secondary base peak
	 */
	private DataPoint secondaryBasePeak = null;

	/**
	 * Constructor that accepts an existing Scan object as a cloning base as
	 * well as the unique storage ID and raw data file object in which the data
	 * points are stored.
	 * 
	 * Additionally requires the number of data points and the within the given
	 * data file since the `numberOfDataPoints` parameter in the Scan object may
	 * be outdated if the data points in `dataFile` are modified.
	 * 
	 * @param sc
	 *            Scan object to clone
	 * @param dataFile
	 *            data file object containing this spectrum's data points
	 * @param numberOfDataPoints
	 *            number of data points contained in `dataFile`
	 * @param storageID
	 *            unique storage id of the location of data points in `dataFile`
	 */
	public CorrectedSpectrum(Scan sc, RawDataFile dataFile,
			int numberOfDataPoints, int storageID) {
		super(sc, (RawDataFileImpl) dataFile, numberOfDataPoints, storageID);

		if (sc instanceof CorrectedSpectrum) {
			CorrectedSpectrum s = (CorrectedSpectrum) sc;
			this.setRetentionTime(s.getOriginalRetentionTime());
			this.uniqueMass = s.getUniqueMass();
			this.retentionIndex = s.getRetentionIndex();
			this.correctedRetentionTime = s.getCorrectedRetentionTime();
			this.fit = s.getRetentionCorrection();
			this.correctionResults = s.getRetentionCorrectionResults();
		} else {
			this.retentionIndex = Integer.MIN_VALUE;
			this.correctedRetentionTime = Integer.MIN_VALUE;
		}
	}

	/**
	 * Base constructor to build a new, non retention index corrected spectrum
	 * object with the minimal required information.
	 * 
	 * @param dataFile
	 *            data file object containing this spectrum's data points
	 * @param storageID
	 *            unique storage id of the location of data points in `dataFile`
	 * @param spectrumNumber
	 *            sequential identifier of this spectrum in `dataFile`
	 * @param retentionTime
	 *            retention time associated with this spectrum
	 * @param dataPoints
	 *            array of data points in this spectrum
	 */
	public CorrectedSpectrum(RawDataFile dataFile, int storageID,
			int spectrumNumber, double retentionTime, DataPoint[] dataPoints) {
		this(dataFile, storageID, spectrumNumber, retentionTime,
				Integer.MIN_VALUE, -1, dataPoints);
	}

	/**
	 * Constructor with added `uniqueMass` parameter.
	 * 
	 * @param dataFile
	 *            data file object containing this spectrum's data points
	 * @param storageID
	 *            unique storage id of the location of data points in `dataFile`
	 * @param spectrumNumber
	 *            sequential identifier of this spectrum in `dataFile`
	 * @param retentionTime
	 *            retention time associated with this spectrum
	 * @param uniqueMass
	 *            unique mass of this spectrum for quantification
	 * @param dataPoints
	 *            array of data points in this spectrum
	 */
	public CorrectedSpectrum(RawDataFile dataFile, int storageID,
			int spectrumNumber, double retentionTime, int uniqueMass,
			DataPoint[] dataPoints) {
		this(dataFile, storageID, spectrumNumber, retentionTime,
				Integer.MIN_VALUE, uniqueMass, dataPoints);
	}

	/**
	 * Constructor with added `uniqueMass` and 'retentionIndex' parameters.
	 * 
	 * @param dataFile
	 *            data file object containing this spectrum's data points
	 * @param storageID
	 *            unique storage id of the location of data points in `dataFile`
	 * @param spectrumNumber
	 *            sequential identifier of this spectrum in `dataFile`
	 * @param retentionTime
	 *            retention time associated with this spectrum
	 * @param retentionIndex
	 *            corrected retention index of this spectrum
	 * @param uniqueMass
	 *            unique mass of this spectrum for quantification
	 * @param dataPoints
	 *            array of data points in this spectrum
	 */
	public CorrectedSpectrum(RawDataFile dataFile, int storageID,
			int spectrumNumber, double retentionTime, int retentionIndex,
			int uniqueMass, DataPoint[] dataPoints) {
		this(dataFile, storageID, spectrumNumber, retentionTime,
				retentionIndex, uniqueMass, dataPoints.length, ScanUtils
						.isCentroided(dataPoints));
	}

	/**
	 * Constructor with added `uniqueMass` and 'retentionIndex' parameters.
	 * 
	 * @param dataFile
	 *            data file object containing this spectrum's data points
	 * @param storageID
	 *            unique storage id of the location of data points in `dataFile`
	 * @param spectrumNumber
	 *            sequential identifier of this spectrum in `dataFile`
	 * @param retentionTime
	 *            retention time associated with this spectrum
	 * @param retentionIndex
	 *            corrected retention index of this spectrum
	 * @param uniqueMass
	 *            unique mass of this spectrum for quantification
	 * @param numberOfDataPoints
	 *            number of data points in this spectrum
	 * @param isCentroided
	 *            whether or not this spectrum is centroided
	 */
	public CorrectedSpectrum(RawDataFile dataFile, int storageID,
			int spectrumNumber, double retentionTime, int retentionIndex,
			int uniqueMass, int numberOfDataPoints, boolean isCentroided) {

		super((RawDataFileImpl) dataFile, storageID, numberOfDataPoints,
				spectrumNumber, 1, retentionTime, -1, 0.0, 1, null,
				isCentroided);

		this.retentionIndex = retentionIndex;
		this.correctedRetentionTime = (retentionIndex > Integer.MIN_VALUE)
				? FameData.FAME_INDICES_TO_TIMES.getY(retentionIndex)
				: Integer.MIN_VALUE;

		this.uniqueMassValue = uniqueMass;
	}

	/**
	 * Gets the original retention time if the spectrum has not been retention
	 * index corrected, or otherwise returns the 'corrected' retention time.
	 * 
	 * @return retention time
	 */
	@Override
	public double getRetentionTime() {
		return isRetentionCorrected() ? correctedRetentionTime : super
				.getRetentionTime();
	}

	/**
	 * Get the original retention file from data file.
	 * 
	 * @return retention time
	 */
	public double getOriginalRetentionTime() {
		return super.getRetentionTime();
	}

	/**
	 * Get the corrected retention time of this spectrum.
	 * 
	 * @return retention time
	 */
	public double getCorrectedRetentionTime() {
		return correctedRetentionTime;
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
		this.correctedRetentionTime = (retentionIndex > Integer.MIN_VALUE)
				? FameData.FAME_INDICES_TO_TIMES.getY(retentionIndex)
				: Integer.MIN_VALUE;
	}

	/**
	 * Gets the unique mass of this spectrum.
	 * 
	 * @return unique mass
	 */
	public DataPoint getUniqueMass() {
		if(uniqueMass == null && uniqueMassValue > 0) {
			DataPoint[] p = getDataPointsByMass(new Range(uniqueMassValue, uniqueMassValue));
			uniqueMass = (p.length == 1) ? p[0] : null;
		}

		return uniqueMass;
	}

	/**
	 * Identifies whether this spectrum has had a retention index applied.
	 * 
	 * @return whether a retention index exists
	 */
	public boolean isRetentionCorrected() {
		return retentionIndex > Integer.MIN_VALUE;
	}

	/**
	 * Get the regression formula used to convert this spectrum's retention time
	 * to a retention index.
	 * 
	 * @return regression formula
	 */
	public CombinedRegression getRetentionCorrection() {
		return fit;
	}

	/**
	 * Returns a map of the correction results, indicating the detected FAME
	 * markers and their corresponding retention times and indices.
	 * 
	 * @return retention correction results
	 */
	public Map<String, FameCorrection> getRetentionCorrectionResults() {
		return correctionResults;
	}

	/**
	 * Sets the regression formula used to convert this spectrum's retention
	 * time to a retention index.
	 * 
	 * @param fit
	 *            new regression formula
	 */
	public void setRetentionCorrection(CombinedRegression fit,
			Map<String, FameCorrection> correctionResults) {
		this.fit = fit;
		this.correctionResults = correctionResults;
	}

	/**
	 * Return the data point of the ion with the second highest abundance
	 * 
	 * @return DataPoint object corresponding to the secondary base peak
	 */
	public DataPoint getSecondaryBasePeak() {
		if (secondaryBasePeak == null && getNumberOfDataPoints() > 1) {
			secondaryBasePeak = getDataPoints()[0];

			for (DataPoint p : getDataPoints())
				if (p.getIntensity() > secondaryBasePeak.getIntensity()
						&& p.getIntensity() < getBasePeak().getIntensity())
					secondaryBasePeak = p;
		}

		return secondaryBasePeak;
	}
}
