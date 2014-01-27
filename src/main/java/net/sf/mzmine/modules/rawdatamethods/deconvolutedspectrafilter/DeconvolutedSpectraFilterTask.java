package net.sf.mzmine.modules.rawdatamethods.deconvolutedspectrafilter;

import net.sf.mzmine.data.DataPoint;
import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.data.RawDataFileWriter;
import net.sf.mzmine.data.Scan;
import net.sf.mzmine.data.impl.SimpleScan;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.project.MZmineProject;
import net.sf.mzmine.taskcontrol.AbstractTask;
import net.sf.mzmine.taskcontrol.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DeconvolutedSpectraFilterTask extends AbstractTask {
    // Logger.
    private static final Logger LOG = Logger
            .getLogger(DeconvolutedSpectraFilterTask.class.getName());

    // Double value tolerance
    public static final double EPSILON = 1.0e-14;

    // Original data file and newly created baseline corrected file.
    private final RawDataFile origDataFile;
    private RawDataFile correctedDataFile;

    // Progress counters.
    private int progress;
    private int progressMax;

    // Filename suffix
    private String suffix;

    // Remove original data file.
    private final boolean removeOriginal;

    // User parameters
    private double c13IsotopeCut;
    private int noiseThreshold;
    private double basePeakCut;


    public DeconvolutedSpectraFilterTask(final RawDataFile dataFile,
              final ParameterSet parameters) {

        // Initialize.
        origDataFile = dataFile;
        correctedDataFile = null;
        progressMax = 0;
        progress = 0;

        // Get parameters.
        suffix = parameters.getParameter(DeconvolutedSpectraFilterParameters.SUFFIX)
                .getValue();
        removeOriginal = parameters.getParameter(
                DeconvolutedSpectraFilterParameters.REMOVE_ORIGINAL).getValue();

        c13IsotopeCut = parameters.getParameter(
                DeconvolutedSpectraFilterParameters.C13_ISOTOPE_CUT).getValue();
        noiseThreshold = parameters.getParameter(
                DeconvolutedSpectraFilterParameters.NOISE_THRESHOLD).getValue();
        basePeakCut = parameters.getParameter(
                DeconvolutedSpectraFilterParameters.BASE_PEAK_CUT).getValue();
    }

    @Override
    public String getTaskDescription() {
        return "Filtering deconvoluted spectra in " + origDataFile;
    }

    @Override
    public double getFinishedPercentage() {
        return progressMax == 0 ? 0.0 : (double) progress
                / (double) progressMax;
    }

    @Override
    public Object[] getCreatedObjects() {
        return new Object[]{correctedDataFile};
    }


    public void run() {
        // Update the status of this task
        setStatus(TaskStatus.PROCESSING);
        LOG.info("Started deconvoluted spectra filter on " + origDataFile);

        // Set total number of scans to process
        progressMax = origDataFile.getNumOfScans();

        try {
            // Create a new file
            final RawDataFileWriter rawDataFileWriter = MZmineCore
                    .createNewFile(origDataFile.getName() + ' ' + suffix);


            // Process each deconvoluted spectrum
            for(int scanNumber : origDataFile.getScanNumbers(1)) {
                // Canceled?
                if (isCanceled())
                    return;

                // Duplicate current spectrum, obtain data points and create list of filtered data points
                SimpleScan scan = new SimpleScan(origDataFile.getScan(scanNumber));
                DataPoint[] dataPoints = scan.getDataPoints();
                List<DataPoint> filteredDataPoints = new ArrayList<DataPoint>();

                // Filter the data points given pre-defined conditions
                for(int i = dataPoints.length - 1; i >= 0; i--) {
                    // Step #1: Remove C13 Isotopes
                    if(i > 0 && dataPoints[i].getMZ() - dataPoints[i - 1].getMZ() < EPSILON &&
                            dataPoints[i - 1].getIntensity() >= (1 +  c13IsotopeCut) * dataPoints[i].getIntensity())
                        continue;

                    // Step #2: Remove all peaks < 100 counts
                    else if(dataPoints[i].getIntensity() < noiseThreshold)
                        continue;

                    // Step #3: Remove all peaks < 1% of base peak
                    else if(dataPoints[i].getIntensity() < basePeakCut * scan.getBasePeak().getIntensity())
                        continue;

                    // If the data point passes all filters, keep it.
                    else
                        filteredDataPoints.add(0, dataPoints[i]);
                }

                // Update the scan with the filtered data points
                scan.setDataPoints(filteredDataPoints.toArray(new DataPoint[filteredDataPoints.size()]));

                // Add scan to new data file
                rawDataFileWriter.addScan(scan);

                progress++;
            }

            // If this task was canceled, stop processing
            if (!isCanceled()) {
                // Finalize writing
                correctedDataFile = rawDataFileWriter.finishWriting();

                // Add the newly created file to the project
                final MZmineProject project = MZmineCore.getCurrentProject();
                project.addFile(correctedDataFile);

                // Remove the original data file if requested
                if (removeOriginal)
                    project.removeFile(origDataFile);

                // Set task status to FINISHED
                setStatus(TaskStatus.FINISHED);

                LOG.info("Finished deconvoluted spectra filter " + origDataFile.getName());
            }
        } catch(Throwable t) {
            LOG.log(Level.SEVERE, "Deconvoluted spectra filtering error", t);
            setStatus(TaskStatus.ERROR);
            errorMessage = t.getMessage();
        }
    }
}