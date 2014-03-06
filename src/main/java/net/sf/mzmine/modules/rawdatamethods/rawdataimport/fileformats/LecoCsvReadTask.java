package net.sf.mzmine.modules.rawdatamethods.rawdataimport.fileformats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import net.sf.mzmine.data.DataPoint;
import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.data.RawDataFileWriter;
import net.sf.mzmine.data.impl.SimpleDataPoint;
import net.sf.mzmine.modules.deconvolutedanalysis.DeconvolutedSpectrum;
import net.sf.mzmine.project.impl.RawDataFileImpl;
import net.sf.mzmine.taskcontrol.AbstractTask;
import net.sf.mzmine.taskcontrol.TaskStatus;
import net.sf.mzmine.util.ExceptionUtils;
import net.sf.mzmine.util.Range;

public class LecoCsvReadTask extends AbstractTask {

	protected String dataSource;
	private File file;
	private RawDataFileImpl newMZmineFile;
	private RawDataFile finalRawDataFile;

	private int totalScans, parsedScans;

	/**
	 * Creates a new LecoCsvReadTask
	 * 
	 * @param fileToOpen
	 *            A File instance containing the file to be read
	 * @param newMZmineFile
	 *            Mzmine data container to which the parsed data is added
	 */
	public LecoCsvReadTask(File fileToOpen, RawDataFileWriter newMZmineFile) {
		this.file = fileToOpen;
		this.newMZmineFile = (RawDataFileImpl) newMZmineFile;
	}

	/**
	 * Reads the file.
	 */
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		Scanner scanner;

		try {
			scanner = new Scanner(this.file);
			scanner.useDelimiter("[,\r\n]+");
			scanner.nextLine();

			dataSource = this.file.getName();
			totalScans = this.getLineCount();

			for (parsedScans = 1; parsedScans < totalScans; parsedScans++) {

				if (isCanceled()) {
					return;
				} // if the task is canceled.

				String ionName = scanner.next();
				while (ionName.length() - ionName.replace("\"", "").length() == 1)
					ionName += scanner.next();
				ionName = ionName.replace("\"", "");

				double retentionTime = Double.parseDouble(scanner.next()
						.replace("\"", "")) / 60;
				scanner.next(); // Type
				int uniqueMass = Integer.parseInt(scanner.next().replace("\"",
						""));
				scanner.next(); // Concentration
				scanner.next(); // Sample Concentration
				scanner.next(); // Match

				ArrayList<Integer> quantMasses = new ArrayList<Integer>();
				for (String s : scanner.next().replace("\"", "").split("\\+"))
					quantMasses.add(Integer.parseInt(s));

				// Quantitative signal to noise
				Double.parseDouble(scanner.next().replace("\"", ""));
				Double.parseDouble(scanner.next().replace("\"", "")); // Area
				scanner.next(); // Baseline Modified
				scanner.next(); // Quantification

				ArrayList<DataPoint> mass_spectrum = new ArrayList<DataPoint>();
				Range mzRange = null;
				SimpleDataPoint p;
				for (String s : scanner.next().split(" ")) {
					String[] ion = s.split(":");
					p = new SimpleDataPoint(Integer.parseInt(ion[0]),
							Integer.parseInt(ion[1]));
					mass_spectrum.add(p);

					if (mzRange == null)
						mzRange = new Range(p.getMZ(), p.getMZ());
					else
						mzRange.extendRange(p.getMZ());
				}
				int spectrumSize = mass_spectrum.size();
				DataPoint[] dataPoints = mass_spectrum
						.toArray(new DataPoint[spectrumSize]);

				int msLevel = 1; // not sure about this value
				int charge = 1; // default positive charge?

				newMZmineFile.setMZRange(1, mzRange);
				newMZmineFile.setRTRange(1, new Range(retentionTime,
						retentionTime));

				int storageID = newMZmineFile.storeDataPoints(dataPoints);
				newMZmineFile.addScan(new DeconvolutedSpectrum(newMZmineFile,
						storageID, parsedScans, retentionTime, dataPoints));
				scanner.nextLine();
			}

			finalRawDataFile = newMZmineFile.finishWriting();

		} catch (Exception e) {
			// errorMessage = e.getMessage();
			errorMessage = ExceptionUtils.exceptionToString(e);
			this.setStatus(TaskStatus.ERROR);
			e.printStackTrace();
			return;
		}

		this.setStatus(TaskStatus.FINISHED);

	}

	/**
	 * Determines the total number of lines in this CSV file. Current approach
	 * is efficient up to ~100k lines - reading blocks and scanning for newlines
	 * would be far more efficient for larger files.
	 * 
	 * @return line count of this CSV file
	 * @throws IOException
	 */
	private int getLineCount() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(this.file));
		int lines = 0;

		while (reader.readLine() != null)
			lines++;
		reader.close();

		return lines;
	}

	/**
	 * @see net.sf.mzmine.taskcontrol.Task#getFinishedPercentage()
	 */
	public double getFinishedPercentage() {
		return totalScans == 0 ? 0 : (double) parsedScans / totalScans;
	}

	public String getTaskDescription() {
		return "Opening file " + file;
	}

	public Object[] getCreatedObjects() {
		return new Object[]{finalRawDataFile};
	}

}
