/**
 * 
 */
package com.catify.processengine.core.nodes;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import com.catify.processengine.core.processdefinition.jaxb.TDefinitions;
import com.catify.processengine.core.processdefinition.jaxb.TFlowElement;
import com.catify.processengine.core.processdefinition.jaxb.TFlowNode;
import com.catify.processengine.core.processdefinition.jaxb.TProcess;
import com.catify.processengine.core.processdefinition.jaxb.TRootElement;
import com.catify.processengine.core.processdefinition.jaxb.TSequenceFlow;

/**
 * @author chris
 * 
 */
public class Base_nodes {

	/**
	 * Gets the process from given xml.
	 *
	 * @param processLocation the process location
	 * @return the bpmn process xml
	 * @throws JAXBException the jAXB exception
	 * @throws FileNotFoundException the file not found exception
	 */
	public List<TProcess> getProcessesFromXML(String processLocation) throws JAXBException,
			FileNotFoundException {

		List<TProcess> processList = new ArrayList<TProcess>();
		
		TProcess process_jaxb = null;

		JAXBContext jaxbContext = JAXBContext
				.newInstance("com.catify.processengine.core.processdefinition.jaxb");
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

		// extract the TDefinitions root element (without @RootElement-Annotation)
		JAXBElement<TDefinitions> root = unmarshaller.unmarshal(new StreamSource(new File(processLocation)),TDefinitions.class);
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
	
	public List<TSequenceFlow> extractSequenceFlows(TProcess process_jaxb) {
		
		List<TSequenceFlow> sequenceFlows_jaxb = new ArrayList<TSequenceFlow>();
		
		// iterate through process elements and separate followingInstances and
		// instantiating sequenceFlows (because they need to be activated after each other)
		for (JAXBElement<? extends TFlowElement> flowElement_jaxb : process_jaxb
				.getFlowElement()) {
			if (flowElement_jaxb.getDeclaredType().equals(TSequenceFlow.class)) {
				sequenceFlows_jaxb.add((TSequenceFlow) flowElement_jaxb.getValue());
			} 
		}
		return sequenceFlows_jaxb;
	}

	public Map<String, TFlowNode> extractFlowNodes(TProcess process_jaxb) {
		
		Map<String, TFlowNode> flowNodes_jaxb = new HashMap<String, TFlowNode>();

		// iterate through process elements and separate followingInstances and
		// instantiating sequenceFlows (because they need to be activated after each other)
		for (JAXBElement<? extends TFlowElement> flowElement_jaxb : process_jaxb
				.getFlowElement()) {
			if (!flowElement_jaxb.getDeclaredType().equals(TSequenceFlow.class)) {
				flowNodes_jaxb.put(
						((TFlowNode) flowElement_jaxb.getValue()).getId(), 
						(TFlowNode) flowElement_jaxb.getValue()
						);
			} 
		}
		return flowNodes_jaxb;
	}

}
