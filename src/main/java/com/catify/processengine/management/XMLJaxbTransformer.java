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

public class XMLJaxbTransformer {

	public TDefinitions getProcessDefinitionFromXML(String processLocation)
			throws JAXBException, FileNotFoundException {

		
		JAXBContext jaxbContext = JAXBContext
				.newInstance("com.catify.processengine.core.processdefinition.jaxb");
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

		// extract the TDefinitions root element (without
		// @RootElement-Annotation)
		JAXBElement<TDefinitions> root = unmarshaller
				.unmarshal(new StreamSource(new File(processLocation)),
						TDefinitions.class);
		TDefinitions definition = root.getValue();

		return definition;
	}
	
	public List<TProcess> getProcessesFromXML(String processLocation)
			throws JAXBException, FileNotFoundException {

		List<TProcess> processList = new ArrayList<TProcess>();

		TProcess process_jaxb = null;

		JAXBContext jaxbContext = JAXBContext
				.newInstance("com.catify.processengine.core.processdefinition.jaxb");
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

		// extract the TDefinitions root element (without
		// @RootElement-Annotation)
		JAXBElement<TDefinitions> root = unmarshaller
				.unmarshal(new StreamSource(new File(processLocation)),
						TDefinitions.class);
		TDefinitions definition = root.getValue();

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
