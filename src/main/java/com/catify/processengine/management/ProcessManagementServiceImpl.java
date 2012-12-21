package com.catify.processengine.management;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.catify.processengine.core.ProcessInitializer;

public class ProcessManagementServiceImpl implements ProcessManagementService {

	static final Logger LOG = LoggerFactory.getLogger(ProcessManagementServiceImpl.class);
	
	public void startDeployedProcesses(String clientId) {
		
		// get a file list of all processes in the 'deployed' folder
		File dir = new File("processes/deployed");
		if (!dir.exists()) {
			dir.mkdirs();
		} else {
			String[] fileList = dir.list(new FilenameFilter() {
			    public boolean accept(File d, String name) {
			       return name.toLowerCase().endsWith(".xml");
			    }
			});
			
			// transform the xml's to jaxb and init them
			XMLJaxbTransformer xmlParser = new XMLJaxbTransformer();
			ProcessInitializer processInitializer = new ProcessInitializer();
			
			for (String file : fileList) {
				try {
					LOG.info(String.format("Starting processes defined in %s", file));
	
					String filePath = dir + "/" + file;
					processInitializer.initializeProcessDefinition(clientId, xmlParser.getProcessDefinitionFromXML(filePath));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (JAXBException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void startProcess() {

	}

	public void stopService() {

	}

	public void removeProcess() {

	}

	
	
}
