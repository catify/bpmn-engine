/**
 * *******************************************************
 * Copyright (C) 2013 catify <info@catify.com>
 * *******************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.catify.processengine.management;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ProcessImportService provides an api to import bpmn process files.
 * 
 * @author christopher köster
 * 
 */
public class ProcessImportServiceImpl implements ProcessImportService {

	static final Logger LOG = LoggerFactory.getLogger(ProcessImportServiceImpl.class);
	
	/** The relative path to all deployed processes. */
	public static final String DEPLOYDIR = "processes/deployed";
	
	/* (non-Javadoc)
	 * @see com.catify.processengine.management.ProcessImportService#importProcessDefinition(java.io.File)
	 */
	@Override
	public void importProcessDefinition(File processDefinition) throws IOException {

		// create the 'deployed' folder if its non existent
		File deployDir = new File(DEPLOYDIR);
		if (!deployDir.exists()) {
			deployDir.mkdirs();
		}
		
		// FIXME: provide file content checks to decide if the process is valid and does not already exists
		
		if (processDefinition.exists()) {
			FileUtils.copyFileToDirectory(processDefinition, deployDir);
			LOG.info("Processdefinition " + processDefinition.getName() + " successfully deployed.");
		} else {
			LOG.info("Processdefinition " + processDefinition.getName() + " does not exist.");
		}
		

	}
	
	/* (non-Javadoc)
	 * @see com.catify.processengine.management.ProcessImportService#removeProcessDefinition(java.io.File)
	 */
	@Override
	public void removeProcessDefinition(File processDefinition) throws IOException {
		if (processDefinition.exists()) {
			processDefinition.delete();
			LOG.info("Processdefinition " + processDefinition.getName() + " successfully removed.");
		} else {
			LOG.info("Processdefinition " + processDefinition.getName() + " could not be found.");
		}
	}

}
