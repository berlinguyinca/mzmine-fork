package net.sf.mzmine.modules.deconvolutedanalysis.famealignment;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import edu.ucdavis.genomics.metabolomics.util.math.Similarity;
import net.sf.mzmine.data.DataPoint;
import net.sf.mzmine.data.impl.SimpleDataPoint;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.modules.deconvolutedanalysis.CorrectedSpectrum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class FameData {
	/** Local filename for FAME library spectrum data from Prime Binbase */
	public static final String PRIME_BINBASE_DATA_FILE = "fame-primebinbase.csv";

	/** Local filename for FAME library spectrum data from Volatile Binbase */
	public static final String VOC_BINBASE_DATA_FILE = "fame-vocbinbase.csv";

	/** Number of library FAME markers */
	public static final int N_FAMES = 13;

	/** FAME marker names */
	public static final String[] FAME_NAMES = new String[]{"C08", "C09", "C10",
			"C12", "C14", "C16", "C18", "C20", "C22", "C24", "C26", "C28",
			"C30"};

	/** FAME marker library retention times */
	public static final double[] FAME_RETENTION_TIMES = new double[]{8.412,
			9.697, 11.018, 13.567, 15.902, 18.033, 19.950, 21.733, 23.370,
			24.885, 26.295, 27.617, 29.017};

	/** FAME marker library retention indices */
	public static final int[] FAME_RETENTION_INDICES = new int[]{262320,
			323120, 381020, 491120, 582620, 668720, 747420, 819620, 886620,
			948820, 1006900, 1061700, 1113100};

	/** FAME marker library integer masses */
	public static final int[] FAME_MASSES = new int[]{158, 172, 186, 214, 242,
			270, 298, 326, 354, 382, 410, 438, 466};

	/** Possible base peak ions required for FAME marker */
	public static final int[] FAME_BASE_PEAKS = new int[]{43, 74, 87, 117, 147,
			174, 130};

	/** Stored spectrum information for each FAME marker */
	private static Map<String, FameMassSpectrum> primeBinBaseData,
			vocBinBaseData;

	private static class FameMassSpectrum {
		private final String name;
		private final double retentionTime;
		private final int retentionIndex;
		private final DataPoint[] spectrum;
		private Similarity similarity;

		public FameMassSpectrum(String name, double retentionTime,
				int retentionIndex, String spectrum) {
			this.name = name;
			this.retentionTime = retentionTime;
			this.retentionIndex = retentionIndex;
			this.spectrum = parseSpectrumData(spectrum);

			// Removes ions with m/z < 50
			if (false) {
				List<String> ions = Lists.newArrayList(spectrum.split(" "));
				for (Iterator<String> it = ions.iterator(); it.hasNext();) {
					double mass = Double.parseDouble(it.next().split(":")[0]);

					if (mass <= 50)
						it.remove();
				}

				spectrum = Joiner.on(" ").join(ions);
			}

			similarity = new Similarity();
			similarity.setLibrarySpectra(spectrum);
		}

		public String getName() {
			return name;
		}
		public double getRetentionTime() {
			return retentionTime;
		}
		public int getRetentionIndex() {
			return retentionIndex;
		}
		public DataPoint[] getSpectrum() {
			return spectrum;
		}
		public Similarity getSimilarity() {
			return similarity;
		}
	}

	static {
		try {
			primeBinBaseData = new TreeMap<String, FameMassSpectrum>();
			readFameData(PRIME_BINBASE_DATA_FILE, primeBinBaseData);

			vocBinBaseData = new TreeMap<String, FameMassSpectrum>();
			readFameData(VOC_BINBASE_DATA_FILE, vocBinBaseData);
		} catch (IOException e) {
			MZmineCore.getDesktop().displayErrorMessage("Load Error",
					"Unable to load FAME data:\n" + e.getMessage());
			e.printStackTrace();
		}
	}

	private static void readFameData(String filename,
			Map<String, FameMassSpectrum> data) throws IOException {
		// Load data
		ClassLoader classLoader = FameData.class.getClassLoader();
		InputStream is = classLoader.getResourceAsStream(filename);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		// Skip header
		br.readLine();

		// Parse each line
		String line;

		while ((line = br.readLine()) != null) {
			Scanner scan = new Scanner(line);
			scan.useDelimiter(",");

			String name = scan.next();
			int binBaseId = scan.nextInt();
			double retentionTime = scan.nextDouble();
			int retentionIndex = scan.nextInt();
			String spectrum = scan.next();

			data.put(name, new FameMassSpectrum(name, retentionTime,
					retentionIndex, spectrum));
		}

		br.close();
		is.close();
	}

	/**
	 * Converts a mass spectrum in string format into an array of DataPoint
	 * objects.
	 * 
	 * @param spectrum
	 *            mass spectrum in string format
	 * @return array of DataPoints for each ion in the mass spectrum
	 */
	private static DataPoint[] parseSpectrumData(String spectrum) {
		List<DataPoint> ions = new ArrayList<DataPoint>();

		for (String ion : spectrum.split(" ")) {
			String[] s = ion.split(":");
			ions.add(new SimpleDataPoint(Double.parseDouble(s[0]), Double
					.parseDouble(s[1])));
		}

		return ions.toArray(new DataPoint[ions.size()]);
	}

	/**
	 * Returns FAME marker spectrum from Volatile BinBase if it exists,
	 * otherwise from Prime BinBase.
	 * 
	 * @param name
	 *            name of FAME marker
	 * @return FAME marker spectrum
	 */
	public static FameMassSpectrum getFameSpectrum(String name) {
		if (!Arrays.asList(FAME_NAMES).contains(name))
			return null;
		else if (vocBinBaseData.keySet().contains(name))
			return vocBinBaseData.get(name);
		else
			return primeBinBaseData.get(name);
	}

	/**
	 * Calculates the similarity between a FAME library spectrum and a
	 * deconvoluted spectrum using the cosine correlation (dot product) method.
	 * Uses BinBase's similarity calculation routine.
	 * 
	 * @param name
	 *            name of FAME marker
	 * @param s
	 *            deconvoluted spectrum object
	 * @return similarity between referenced spectra
	 */
	public static double computeSimilarity(String name, CorrectedSpectrum s) {
		// Get data points and base peak intensity
		DataPoint[] p = s.getDataPoints();
		double maxAbundance = s.getBasePeak().getIntensity();

		// Produce formatted spectrum for use with BinBase's similarity
		// algorithm
		double[][] bigSpectrum = new double[p.length][Similarity.ARRAY_WIDTH];

		for (int i = 0; i < p.length; i++) {
			bigSpectrum[i][Similarity.FRAGMENT_ION_POSITION] = p[i].getMZ();
			bigSpectrum[i][Similarity.FRAGMENT_ABS_POSITION] = p[i]
					.getIntensity();
			bigSpectrum[i][Similarity.FRAGMENT_REL_POSITION] = p[i]
					.getIntensity() / maxAbundance * 100;
		}

		Similarity similarity = getFameSpectrum(name).getSimilarity();
		similarity.setUnknownSpectra(bigSpectrum);
		return similarity.calculateSimimlarity();

		/*
		 * double[][] x = similarity.getLibrarySpectra(), y =
		 * similarity.getUnknownSpectra();
		 * 
		 * double xsum = 0, ysum = 0, xysum = 0;
		 * 
		 * for(int i = 0; i < x.length; i++) { xsum +=
		 * x[i][Similarity.FRAGMENT_ABS_POSITION] *
		 * x[i][Similarity.FRAGMENT_ABS_POSITION]; ysum +=
		 * y[i][Similarity.FRAGMENT_ABS_POSITION] *
		 * y[i][Similarity.FRAGMENT_ABS_POSITION]; xysum +=
		 * x[i][Similarity.FRAGMENT_ABS_POSITION] *
		 * y[i][Similarity.FRAGMENT_ABS_POSITION]; }
		 * 
		 * for(int i = 0; i < x.length; i++) { xsum +=
		 * x[i][Similarity.FRAGMENT_ION_POSITION] *
		 * x[i][Similarity.FRAGMENT_REL_POSITION]; ysum +=
		 * y[i][Similarity.FRAGMENT_ION_POSITION] *
		 * y[i][Similarity.FRAGMENT_REL_POSITION]; xysum +=
		 * Math.sqrt(x[i][Similarity.FRAGMENT_REL_POSITION] *
		 * y[i][Similarity.FRAGMENT_REL_POSITION]) *
		 * x[i][Similarity.FRAGMENT_ION_POSITION]; } return xysum /
		 * Math.sqrt(xsum * ysum);
		 */
	}
}
