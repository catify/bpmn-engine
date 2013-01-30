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
package com.catify.processengine.core;

import java.util.Iterator;

import javax.xml.bind.JAXBElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;

import com.catify.processengine.core.processdefinition.jaxb.TDefinitions;
import com.catify.processengine.core.processdefinition.jaxb.TProcess;
import com.catify.processengine.core.processdefinition.jaxb.TRootElement;

/**
 * Delegates the initialization of the node instance services and their data
 * beans defined in a given process.
 * 
 * @author christopher köster
 * 
 */
@Configurable
public class ProcessInitializer {

	static final Logger LOG = LoggerFactory.getLogger(ProcessInitializer.class);

	/**
	 * Initialize process(es) of a jaxb definition object. A definition object
	 * can hold multiple process objects. (root element of bpmn definition)
	 * 
	 * @param definition
	 *            {@link TDefinitions}
	 */
	public void initializeProcessDefinition(String clientId,
			TDefinitions definition) {
		for (Iterator<JAXBElement<? extends TRootElement>> iterator = definition
				.getRootElement().iterator(); iterator.hasNext();) {

			TRootElement rootElement = iterator.next().getValue();
			TProcess process = null;

			if (rootElement.getClass().equals(TProcess.class)) {
				process = (TProcess) rootElement;
				this.initializeProcess(clientId, process);
			}
		}
	}

	/**
	 * Initialize a process with a jaxb process object. A process consists of
	 * flow nodes and sequence flows.
	 * 
	 * @param clientId
	 *            the client id
	 * @param processJaxb
	 *            the process jaxb {@link TProcess}
	 */
	public void initializeProcess(String clientId, TProcess processJaxb) {
		LOG.debug(String.format("Instantiating %s:%s with %s elements",
				processJaxb.getClass().getSimpleName(), processJaxb.getId(),
				processJaxb.getFlowElement().size()));

		// initialize data entities (data representation)
		EntityInitialization entityInit = new EntityInitialization();
		entityInit.initializeProcess(clientId, processJaxb);
	}

}
