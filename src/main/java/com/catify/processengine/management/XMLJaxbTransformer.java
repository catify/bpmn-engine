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
