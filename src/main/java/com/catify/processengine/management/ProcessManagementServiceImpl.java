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
import java.io.FilenameFilter;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;

import akka.actor.ActorRef;

import com.catify.processengine.core.ProcessInitializer;
import com.catify.processengine.core.data.services.impl.IdService;
import com.catify.processengine.core.messages.TriggerMessage;
import com.catify.processengine.core.processdefinition.jaxb.TFlowElement;
import com.catify.processengine.core.processdefinition.jaxb.TFlowNode;
import com.catify.processengine.core.processdefinition.jaxb.TProcess;
import com.catify.processengine.core.services.ActorReferenceService;

@Configurable
public class ProcessManagementServiceImpl implements ProcessManagementService {

	static final Logger LOG = LoggerFactory.getLogger(ProcessManagementServiceImpl.class);
	
	/* (non-Javadoc)
	 * @see com.catify.processengine.management.ProcessManagementSer#startAllDeployedProcesses(java.lang.String)
	 */
	@Override
	public void startAllDeployedProcesses(String clientId) throws FileNotFoundException, JAXBException {
		
		// get a file list of all processes in the 'deployed' folder
		File deployDir = new File(ProcessImportService.DEPLOYDIR);
		if (!deployDir.exists()) {
			LOG.warn("The folder " + ProcessImportService.DEPLOYDIR + " does not exist. There are no processes to deploy.");
		} else {
			String[] fileList = deployDir.list(new FilenameFilter() {
			    public boolean accept(File d, String name) {
			       return name.toLowerCase().endsWith(".xml");
			    }
			});
			
			// transform the xml's to jaxb and init them
			XmlJaxbTransformer xmlJaxbTransformer = new XmlJaxbTransformer();
			ProcessInitializer processInitializer = new ProcessInitializer();
			
			for (String processDefinition : fileList) {
					this.logProcessDefinitionStart(processDefinition);
	
					String processDefinitionPath = deployDir + File.separator + processDefinition;
					processInitializer.initializeProcessDefinition(clientId, xmlJaxbTransformer.getTDefinitionsFromBpmnXml(processDefinitionPath));
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.catify.processengine.management.ProcessManagementSer#startDeployedProcess(java.lang.String, java.lang.String)
	 */
	@Override
	public void startDeployedProcess(String clientId, String processDefinitionFileName) throws FileNotFoundException, JAXBException {
		
		File deployDir = new File(ProcessImportService.DEPLOYDIR);
		if (!deployDir.exists()) {
			LOG.warn("The folder " + ProcessImportService.DEPLOYDIR + " does not exist. There are no processes to deploy.");
		} else {
		
		// transform the xml's to jaxb and init them
		XmlJaxbTransformer xmlJaxbTransformer = new XmlJaxbTransformer();
		ProcessInitializer processInitializer = new ProcessInitializer();
		
		this.logProcessDefinitionStart(processDefinitionFileName);
		
		processInitializer.initializeProcessDefinition(clientId, xmlJaxbTransformer.getTDefinitionsFromBpmnXml(
				ProcessImportService.DEPLOYDIR 
				+ File.separator
				+ processDefinitionFileName));
		}
	}
	
	/* (non-Javadoc)
	 * @see com.catify.processengine.management.ProcessManagementSer#startProcessFromDefinitionFile(java.lang.String, java.io.File)
	 */
	@Override
	public void startProcessFromDefinitionFile(String clientId, File processDefinition) throws FileNotFoundException, JAXBException {
		// transform the xml's to jaxb and init them
		XmlJaxbTransformer xmlJaxbTransformer = new XmlJaxbTransformer();
		ProcessInitializer processInitializer = new ProcessInitializer();
		
		this.logProcessDefinitionStart(processDefinition.getName());
		
		processInitializer.initializeProcessDefinition(clientId, xmlJaxbTransformer.getTDefinitionsFromBpmnXml(processDefinition));
	}
	
	/**
	 * Create a log message for a process definition start.
	 *
	 * @param processDefinition the process definition
	 */
	private void logProcessDefinitionStart(String processDefinition) {
		LOG.info(String.format("Starting processes defined in %s", processDefinition));
	}
	
	/* (non-Javadoc)
	 * @see com.catify.processengine.management.ProcessManagementSer#createProcessInstance(java.lang.String)
	 */
	@Override
	public void createProcessInstance(String uniqueFlowNodeId, TriggerMessage triggerMessage) {

		sendTriggerMessage(uniqueFlowNodeId, triggerMessage);
	}
	
	/* (non-Javadoc)
	 * @see com.catify.processengine.management.ProcessManagementService#createProcessInstance(java.lang.String, com.catify.processengine.core.processdefinition.jaxb.TProcess, java.lang.String, com.catify.processengine.core.messages.TriggerMessage)
	 */
	@Override
	public void createProcessInstance(String clientId, TProcess processJaxb,
			String startEventId, TriggerMessage triggerMessage) {
		this.sendTriggerMessage(clientId, processJaxb, startEventId, triggerMessage);
	}

	/* (non-Javadoc)
	 * @see com.catify.processengine.management.ProcessManagementService#sendTriggerMessage(java.lang.String, com.catify.processengine.core.processdefinition.jaxb.TProcess, java.lang.String, com.catify.processengine.core.messages.TriggerMessage)
	 */
	@Override
	public void sendTriggerMessage(String clientId, TProcess processJaxb,
			String nodeId, TriggerMessage triggerMessage) {
		
		String uniqueFlowNodeId = getUniqueFlowNodeId(clientId, processJaxb,
				nodeId);
		
		sendTriggerMessage(uniqueFlowNodeId, triggerMessage);
	}
	
	/* (non-Javadoc)
	 * @see com.catify.processengine.management.ProcessManagementService#sendTriggerMessage(java.lang.String, com.catify.processengine.core.messages.TriggerMessage)
	 */
	@Override
	public void sendTriggerMessage(String uniqueFlowNodeId, TriggerMessage triggerMessage) {

		ActorRef actorRef = new ActorReferenceService().getActorReference(uniqueFlowNodeId);
		
		LOG.debug("Sending TriggerMessage to " + actorRef);
		
		actorRef.tell(triggerMessage, null);
	}

	/**
	 * Gets the unique flow node id.
	 *
	 * @param clientId the client id
	 * @param processJaxb the process jaxb
	 * @param nodeId the node id
	 * @return the unique flow node id
	 */
	private String getUniqueFlowNodeId(String clientId, TProcess processJaxb,
			String nodeId) {
		TFlowNode flowNode = getFlowNodeById(processJaxb, nodeId);

		String uniqueFlowNodeId = IdService.getUniqueFlowNodeId(clientId, processJaxb, null, flowNode);
		return uniqueFlowNodeId;
	}

	/**
	 * Gets the flow node by id.
	 *
	 * @param processJaxb the process jaxb
	 * @param nodeId the node id
	 * @return the flow node by id
	 */
	private TFlowNode getFlowNodeById(TProcess processJaxb, String nodeId) {
		
		for (JAXBElement<? extends TFlowElement> flowElementJaxb : processJaxb
				.getFlowElement()) {
			if (flowElementJaxb.getValue() instanceof TFlowNode
					&& flowElementJaxb.getValue().getId().equals(nodeId)) {
				LOG.debug(String.format("Found Flow Node with id ",
						flowElementJaxb.getValue().getId()));
				return (TFlowNode) flowElementJaxb.getValue();
			}
		}
		LOG.error("The node id " + nodeId + " could not be found!");
		return null;
	}
	
}
