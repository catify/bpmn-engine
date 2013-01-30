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
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import com.catify.processengine.core.processdefinition.jaxb.TDefinitions;
import com.catify.processengine.core.processdefinition.jaxb.TProcess;
import com.catify.processengine.core.processdefinition.jaxb.TRootElement;

/**
 * Utility class to work with bpmn process files and jaxb.
 * 
 * @author christopher köster
 * 
 */
public class XmlJaxbTransformer {

	/**
	 * Gets the jaxb TDefinitions from a bpmn xml file path.
	 *
	 * @param processDefinitionPath the process definition path
	 * @return the t definitions from bpmn xml
	 * @throws JAXBException the jAXB exception
	 * @throws FileNotFoundException the file not found exception
	 */
	public TDefinitions getTDefinitionsFromBpmnXml(String processDefinitionPath)
			throws JAXBException, FileNotFoundException {

		Unmarshaller unmarshaller = this.getUnmarshaller();

		return this.getTDefinitions(unmarshaller, new File(processDefinitionPath));
	}
	
	/**
	 * Gets the jaxb TDefinitions from a bpmn xml file.
	 *
	 * @param processDefinition the process definition
	 * @return the t definitions from bpmn xml
	 * @throws JAXBException the jAXB exception
	 * @throws FileNotFoundException the file not found exception
	 */
	public TDefinitions getTDefinitionsFromBpmnXml(File processDefinition)
			throws JAXBException, FileNotFoundException {

		Unmarshaller unmarshaller = this.getUnmarshaller();

		return this.getTDefinitions(unmarshaller, processDefinition);
	}
	
	/**
	 * Gets the jaxb TProcesses from a bpmn xml file path.
	 *
	 * @param processDefinitionPath the process definition path
	 * @return the t processes from bpmn xml
	 * @throws JAXBException the jAXB exception
	 * @throws FileNotFoundException the file not found exception
	 */
	public List<TProcess> getTProcessesFromBpmnXml(String processDefinitionPath)
			throws JAXBException, FileNotFoundException {

		Unmarshaller unmarshaller = this.getUnmarshaller();

		TDefinitions definition = this.getTDefinitions(unmarshaller, new File(processDefinitionPath));

		return getTProcesses(definition);
	}
	
	/**
	 * Gets the jaxb TProcesses from a bpmn xml file.
	 *
	 * @param processDefinition the process definition
	 * @return the TProcesses from the bpmn xml
	 * @throws JAXBException the jAXB exception
	 * @throws FileNotFoundException the file not found exception
	 */
	public List<TProcess> getTProcessesFromBpmnXml(File processDefinition)
			throws JAXBException, FileNotFoundException {

		Unmarshaller unmarshaller = this.getUnmarshaller();

		TDefinitions definition = this.getTDefinitions(unmarshaller, processDefinition);

		return getTProcesses(definition);
	}
	
	/**
	 * Gets the jaxb unmarshaller.
	 *
	 * @return the unmarshaller
	 * @throws JAXBException the jAXB exception
	 */
	private Unmarshaller getUnmarshaller() throws JAXBException {
		JAXBContext jaxbContext = JAXBContext
				.newInstance("com.catify.processengine.core.processdefinition.jaxb");
		
		return jaxbContext.createUnmarshaller();
	}

	/**
	 * Gets the jaxb TDefinitions from a bpmn xml file.
	 *
	 * @param unmarshaller the unmarshaller
	 * @param processDefinition the process definition
	 * @return the t definitions
	 * @throws JAXBException the jAXB exception
	 */
	private TDefinitions getTDefinitions(Unmarshaller unmarshaller, File processDefinition) throws JAXBException {
		// extract the TDefinitions root element (without
		// @RootElement-Annotation)
		JAXBElement<TDefinitions> root = unmarshaller
				.unmarshal(new StreamSource(processDefinition),
						TDefinitions.class);
		return root.getValue();
	}
	
	/**
	 * Gets the jaxb TProcesses from a bpmn xml file.
	 *
	 * @param definition the definition
	 * @return the t processes
	 */
	private List<TProcess> getTProcesses(TDefinitions definition) {
		TProcess process_jaxb;
		List<TProcess> processList = new ArrayList<TProcess>();
		
		// extract the process from the element
		for (Iterator<JAXBElement<? extends TRootElement>> iterator = definition
				.getRootElement().iterator(); iterator.hasNext();) {

			TRootElement rootElement = iterator.next().getValue();

			if (rootElement.getClass().equals(TProcess.class)) {
				process_jaxb = (TProcess) rootElement;
				processList.add(process_jaxb);
			}
		}
		
		return processList;
	}
}
