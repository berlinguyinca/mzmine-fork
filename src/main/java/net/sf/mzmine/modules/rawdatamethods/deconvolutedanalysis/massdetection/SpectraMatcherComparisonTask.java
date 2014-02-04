package net.sf.mzmine.modules.rawdatamethods.deconvolutedanalysis.massdetection;

import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.parameters.parametertypes.IntegerParameter;
import net.sf.mzmine.taskcontrol.AbstractTask;
import net.sf.mzmine.taskcontrol.TaskStatus;
import net.sf.mzmine.util.Range;

import java.util.*;
import java.util.logging.Logger;

public class SpectraMatcherComparisonTask extends AbstractTask {
	// Logger
	private Logger LOG = Logger.getLogger(this.getClass().getName());

	// Collection of spectra processing tasks
	private List<SpectraMatcherProcessingTask> processingTasks;

    // Collection of mass candidates sorted by data file
    private Map<SpectrumType, List<MassCandidate>> massCandidates;

    // Collection of matched mass candidates
    private Map<Integer, List<MassCandidate>> matchedCandidates;

	// Progress counters
	private int processedScans = 0;
	private int totalScans;


	public SpectraMatcherComparisonTask(List<SpectraMatcherProcessingTask> processingTasks,
                                        Map<SpectrumType, List<MassCandidate>> massCandidates,
                                        Map<Integer, List<MassCandidate>> matchedCandidates) {
		this.processingTasks = processingTasks;
        this.massCandidates = massCandidates;
        this.matchedCandidates = matchedCandidates;
	}

	@Override
	public String getTaskDescription() {
		return "Comparing detected masses from multiple ionization sources.";
	}

	@Override
	public double getFinishedPercentage() {
		return totalScans == 0 ? 0.0 : (double) processedScans
				/ (double) totalScans;
	}

	public void run() {
        // Wait until all other processing tasks have finished
        try {
            while(isBusy())
                Thread.sleep(250);
        } catch(Throwable t) {}

        // Canceled?
        if (isCanceled())
            return;

		// Update the status of this task
		setStatus(TaskStatus.PROCESSING);
		LOG.info("Started detected mass comparison.");

        // Intersection of all detected mass lists
        Set<Integer> sharedMasses = new TreeSet<Integer>();

        for(Map.Entry<SpectrumType, List<MassCandidate>> e : massCandidates.entrySet()) {
            // Produce a collection of ion masses
            List<Integer> masses = new ArrayList<Integer>();
            for(MassCandidate mass : e.getValue()) {
                System.out.println((mass == null));
                masses.add(mass.getIonMass());
            }

            if(sharedMasses.size() == 0)
                sharedMasses.addAll(masses);
            else
                sharedMasses.retainAll(masses);
        }

        System.out.println(Arrays.toString(sharedMasses.toArray()));


        for(int mass : sharedMasses)
            matchedCandidates.put(mass, new ArrayList<MassCandidate>());

        for(Map.Entry<SpectrumType, List<MassCandidate>> e : massCandidates.entrySet()) {
            for(MassCandidate mass : e.getValue()) {
                if(sharedMasses.contains(mass.getIonMass()))
                    matchedCandidates.get(mass.getIonMass()).add(mass);
            }
        }


        /*
        // Output to CSV
        try {
            FileWriter fout = new FileWriter("pci-matches-20140131.csv");
            fout.append("Candidate Match,Filename,Retention Time,Matched Adducts\n");

            for(Integer mass : matchedCandidates.keySet()) {
                for(MassCandidate m : matchedCandidates.get(mass)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(m.getIonMass());
                    sb.append(',');
                    sb.append(m.getDataFile().getName());
                    sb.append(',');
                    sb.append(m.getScanNumber());
                    sb.append(',');
                    sb.append(new DecimalFormat("####.000").format(m.getRetentionTime()));
                    sb.append(',');
                    for(AdductType a : m.getAdductMatches())
                        sb.append(a.getName() +" ");
                    sb.deleteCharAt(sb.length() - 1);
                    sb.append('\n');

                    fout.append(sb.toString());
                }
            }


            fout.flush();
            fout.close();
        } catch(Throwable t) {}
        */


        // If this task was canceled, stop processing
        if (!isCanceled()) {
            // Set task status to FINISHED
            setStatus(TaskStatus.FINISHED);

            LOG.info("Finished comparing detected masses.");
        }
	}

    private boolean isBusy() {
        // If the current task is cancelled, we are not busy
        if(isCanceled())
            return false;

        for(SpectraMatcherProcessingTask task : processingTasks) {
            // If a task is still working, we are still busy
            if(!task.isFinished())
                return true;
        }

        // If all tasks are finished, we are not busy
        return false;
    }
}
