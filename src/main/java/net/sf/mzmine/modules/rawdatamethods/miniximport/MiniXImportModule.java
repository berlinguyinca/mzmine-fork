/*
 * Copyright 2006-2013 The MZmine 2 Development Team
 * 
 * This file is part of MZmine 2.
 * 
 * MZmine 2 is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * MZmine 2 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * MZmine 2; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * St, Fifth Floor, Boston, MA 02110-1301 USA
 */

package net.sf.mzmine.modules.rawdatamethods.miniximport;

import java.io.File;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.ConnectException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ValueNode;
import org.codehaus.jackson.JsonParseException;

import javax.annotation.Nonnull;

import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.data.RawDataFileWriter;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.modules.MZmineModuleCategory;
import net.sf.mzmine.modules.MZmineProcessingModule;
import net.sf.mzmine.modules.rawdatamethods.rawdataimport.fileformats.AgilentCsvReadTask;
import net.sf.mzmine.modules.rawdatamethods.rawdataimport.fileformats.MzDataReadTask;
import net.sf.mzmine.modules.rawdatamethods.rawdataimport.fileformats.MzMLReadTask;
import net.sf.mzmine.modules.rawdatamethods.rawdataimport.fileformats.MzXMLReadTask;
import net.sf.mzmine.modules.rawdatamethods.rawdataimport.fileformats.NetCDFReadTask;
import net.sf.mzmine.modules.rawdatamethods.rawdataimport.fileformats.XcaliburRawFileReadTask;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.taskcontrol.Task;
import net.sf.mzmine.taskcontrol.TaskEvent;
import net.sf.mzmine.taskcontrol.TaskListener;
import net.sf.mzmine.taskcontrol.TaskStatus;
import net.sf.mzmine.util.ExitCode;

public class MiniXImportModule implements MZmineProcessingModule, TaskListener {
    
    private Logger logger = Logger.getLogger(this.getClass().getName());

    private static final String MODULE_NAME = "MiniX Data Import";
    private static final String MODULE_DESCRIPTION = 
        "Imports raw data indexed by MiniX into the project";

    @Override
    public @Nonnull String getName() {
        return MODULE_NAME;
    }

    @Override
    public @Nonnull String getDescription() {
        return MODULE_DESCRIPTION;
    }

    @Override
    @Nonnull
    public ExitCode runModule(@Nonnull ParameterSet parameters,
       @Nonnull Collection<Task> tasks) {
        String studyID = parameters.getParameter (
            MiniXImportParameters.id
        ).getValue();
        
        // get our sample IDs from MiniX
        StringBuffer response = new StringBuffer ();
        try {
        	URL getSamplesURL = new URL("http://fauxminix.com/getSamplesForStudyIdAsJSON/"+studyID);
        	HttpURLConnection getSamplesCon = (HttpURLConnection)getSamplesURL.openConnection();
        	BufferedReader bfdRead = new BufferedReader (
    	            new InputStreamReader (
    	            		getSamplesCon.getInputStream()
    	            )
    	        );
            String inputLine;
            while ((inputLine = bfdRead.readLine()) != null) {
                response.append (inputLine);
            }
            bfdRead.close();
        } catch (MalformedURLException e) {
            MZmineCore.getDesktop().displayErrorMessage("Invalid Study Name");
            return ExitCode.CANCEL;
        } catch (FileNotFoundException e) {
        	MZmineCore.getDesktop().displayErrorMessage(
    			"MiniX was unable to find a study for the ID \"" +
    			studyID +
    			'"'
			);
            return ExitCode.CANCEL;
        } catch (IOException e) {
        	logger.log (Level.SEVERE, "network error", e);
        	MZmineCore.getDesktop().displayErrorMessage(
    			"Either MiniX or your internet connection are not working right."
			);
        	return ExitCode.ERROR;
        }
        
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = null;
        try {
        	rootNode = mapper.readValue(response.toString(), JsonNode.class);
        } catch (Exception e) {
        	logger.severe ("MiniX index service returned something other than a JSON Array.");
        	MZmineCore.getDesktop().displayErrorMessage(
    			"MiniX does not appear to be working correctly."
			);
        	return ExitCode.ERROR;
        }

        if (!rootNode.isArray()) {
        	logger.severe ("MiniX index service returned something other than a JSON Array.");
        	MZmineCore.getDesktop().displayErrorMessage(
    			"MiniX does not appear to be working correctly."
			);
        	return ExitCode.ERROR;
        }
        
        for (JsonNode node: rootNode) {
            String sampleID = node.getValueAsText();
        	HttpURLConnection getDataURLCon = null;
        	try {
	            getDataURLCon = 
		    		(HttpURLConnection) new URL (
		                "http://127.0.0.1:9001/data/"+sampleID
		            ).openConnection();
	            getDataURLCon.connect();
                int i = 0;
                String disposition = getDataURLCon.getHeaderField ("Content-Disposition");
                String extension = null;
                if (disposition != null) {
                    String[] frags = disposition.substring(0,disposition.length()-1).split("\\.");
                    extension = frags[frags.length-1];
                } else {
                    logger.severe ("masspec_glue did not set content-disposition.");
                    MZmineCore.getDesktop().displayErrorMessage(
                        "MiniX does not appear to be working correctly."
                    );
                    return ExitCode.ERROR;
                }
	            ReadableByteChannel rbc = Channels.newChannel(getDataURLCon.getInputStream());
                File temp = File.createTempFile (sampleID,extension);
                temp.deleteOnExit();
                FileOutputStream fos = new FileOutputStream (temp);
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                
                RawDataFileWriter newMZmineFile;
                try {
                    newMZmineFile = MZmineCore.createNewFile (sampleID);
                } catch (IOException e) {
                    MZmineCore.getDesktop().displayErrorMessage(
                        "Could not create a new temporary file " + e);
                    logger.log(Level.SEVERE,
                        "Could not create a new temporary file ", e);
                    return ExitCode.ERROR;
                }

                Task newTask = null;
                if (extension.equals ("mzdata")) {
                    newTask = new MzDataReadTask(temp, newMZmineFile);
                }
                if (extension.equals ("mzxml")) {
                    newTask = new MzXMLReadTask(temp, newMZmineFile);
                }
                if (extension.equals ("mzml")) {
                    newTask = new MzMLReadTask(temp, newMZmineFile);
                }
                if (extension.equals ("cdf")) {
                    newTask = new NetCDFReadTask(temp, newMZmineFile);
                }
                if (extension.equals ("raw")) {
                    newTask = new XcaliburRawFileReadTask(temp,
                        newMZmineFile);
                }
                if (extension.equals ("csv")) {
                    newTask = new AgilentCsvReadTask(temp, newMZmineFile);
                }
                if (extension.equals ("xml")) try {
                    // Check the first 512 bytes of the file, to determine the
                    // file type
                    FileReader reader = new FileReader(temp);
                    char buffer[] = new char[512];
                    reader.read(buffer);
                    reader.close();
                    String fileHeader = new String(buffer);
                    if (fileHeader.contains("mzXML")) {
                        newTask = new MzXMLReadTask(temp, newMZmineFile);
                    }
                    if (fileHeader.contains("mzData")) {
                        newTask = new MzDataReadTask(temp,
                        newMZmineFile);
                    }
                    if (fileHeader.contains("mzML")) {
                        newTask = new MzMLReadTask(temp, newMZmineFile);
                    }
                } catch (Exception e) {
                    logger.warning("Cannot read file " + temp + ": "
                        + e);
                    return ExitCode.ERROR;
                }

                if (newTask == null) {
                    logger.severe ("Cannot determine file type of file "
                        + temp);
                    MZmineCore.getDesktop().displayErrorMessage(
                        "Obtained malformed sample file for \"" +
                         sampleID + 
                         '"'
                    );
                    return ExitCode.ERROR;
                }

                newTask.addTaskListener(this);
                tasks.add(newTask);
        	} catch (FileNotFoundException e) {
                logger.severe (
                    "Could not find expected sample \"" +
                     sampleID + 
                     '"'
                );
                MZmineCore.getDesktop().displayErrorMessage(
                    "Could not find expected sample \"" +
                     sampleID + 
                     '"'
                );
                return ExitCode.ERROR;
            } catch (ConnectException e) {
                logger.severe ("Connection refused while obtaining sample file.");
                MZmineCore.getDesktop().displayErrorMessage (
                    "Connection refused while obtaining sample file. MiniX is probably down."
                );
                return ExitCode.ERROR;
        	} catch (Exception e) {
                logger.log (
                    Level.SEVERE,
                    "An unknown disaster occured while downloading sample \"" +
                     sampleID + 
                     '"',
                    e
                );
                MZmineCore.getDesktop().displayErrorMessage (
                    "An unknown disaster occured while downloading sample \"" +
                     sampleID + 
                     '"'
                );
                return ExitCode.ERROR;
        	}
        }
        
        return ExitCode.OK;
    }

    @Override
    public void statusChanged(TaskEvent e) {
        if (e.getStatus() == TaskStatus.FINISHED) {
            MZmineCore.getCurrentProject().addFile (
                (RawDataFile) e.getSource().getCreatedObjects()[0]
            );
        }
    }
    
    @Override
    public @Nonnull MZmineModuleCategory getModuleCategory() {
        return MZmineModuleCategory.RAWDATA;
    }

    @Override
    public @Nonnull Class<? extends ParameterSet> getParameterSetClass() {
        return MiniXImportParameters.class;
    }
}
