package net.sf.mzmine.modules.rawdatamethods.deconvolutedanalysis.massdetection;

import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.modules.peaklistmethods.identification.adductsearch.AdductType;

import java.util.List;

public class MassCandidate {
	private RawDataFile dataFile;
	private int spectrumNumber;
	private double retentionTime;
	private int ionMass;
	private SpectrumType ionizationType;
	private AdductType[] adductMatches;

	public MassCandidate(RawDataFile dataFile, int spectrumNumber,
			double retentionTime, int ionMass, SpectrumType ionizationType,
			List<AdductType> adductMatches) {
		this.dataFile = dataFile;
		this.spectrumNumber = spectrumNumber;
		this.retentionTime = retentionTime;
		this.ionMass = ionMass;
		this.ionizationType = ionizationType;
		this.adductMatches = adductMatches.toArray(new AdductType[adductMatches
				.size()]);
	}

	public RawDataFile getDataFile() {
		return dataFile;
	}

	public int getSpectrumNumber() {
		return spectrumNumber;
	}

	public double getRetentionTime() {
		return retentionTime;
	}

	public AdductType[] getAdductMatches() {
		return adductMatches;
	}

	public int getIonMass() {
		return ionMass;
	}

	public SpectrumType getIonizationType() {
		return ionizationType;
	}
}
