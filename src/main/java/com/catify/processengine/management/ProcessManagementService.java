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

import javax.xml.bind.JAXBException;

import com.catify.processengine.core.messages.TriggerMessage;
import com.catify.processengine.core.processdefinition.jaxb.TProcess;
import com.catify.processengine.core.processdefinition.jaxb.TSubProcess;

/**
 * The Interface ProcessManagementService defines the api to manage processes.
 * 
 * @author christopher köster
 * 
 */
public interface ProcessManagementService {

	/**
	 * Start all deployed processes found in the {@link ProcessImportServiceImpl.DEPLOYDIR}.
	 *
	 * @param clientId the client id
	 * @throws FileNotFoundException the file not found exception
	 * @throws JAXBException the jAXB exception
	 */
	public abstract void startAllDeployedProcesses(String clientId)
			throws FileNotFoundException, JAXBException;

	/**
	 * Start a deployed process by filename.
	 *
	 * @param clientId the client id
	 * @param processDefinitionFileName the process definition file name
	 * @throws FileNotFoundException the file not found exception
	 * @throws JAXBException the jAXB exception
	 */
	public abstract void startDeployedProcess(String clientId,
			String processDefinitionFileName) throws FileNotFoundException,
			JAXBException;

	/**
	 * Start a process from a bpmn process definition file.
	 *
	 * @param clientId the client id
	 * @param processDefinitionPath the process definition path
	 * @throws FileNotFoundException the file not found exception
	 * @throws JAXBException the jAXB exception
	 */
	public abstract void startProcessFromDefinitionFile(String clientId,
			File processDefinition) throws FileNotFoundException, JAXBException;

	/**
	 * Creates a process instance.
	 *
	 * @param uniqueFlowNodeId the unique flow node id of the start event
	 * @param triggerMessage the trigger message
	 */
	void createProcessInstance(String uniqueFlowNodeId,
			TriggerMessage triggerMessage);

	/**
	 * Creates a process instance.
	 *
	 * @param clientId the client id
	 * @param processJaxb the jaxb process
	 * @param startEventId the start event id in the bpmn process xml
	 * @param triggerMessage the trigger message
	 */
	void createProcessInstance(String clientId, TProcess processJaxb,
			String startEventId, TriggerMessage triggerMessage);

	/**
	 * Send trigger message to a given node. 
	 *
	 * @param clientId the client id
	 * @param processJaxb the jaxb process 
	 * @param nodeId the node id in the bpmn process xml
	 * @param triggerMessage the trigger message
	 */
	void sendTriggerMessage(String clientId, TProcess processJaxb,
			String nodeId, TriggerMessage triggerMessage);

	/**
	 * Send trigger message to a given top level node.
	 *
	 * @param uniqueFlowNodeId the unique flow node id
	 * @param triggerMessage the trigger message
	 */
	void sendTriggerMessage(String uniqueFlowNodeId,
			TriggerMessage triggerMessage);

	/**
	 * Send trigger message to a given node (including sub process nodes).
	 *
	 * @param clientId the client id
	 * @param processJaxb the process jaxb
	 * @param subProcessesJaxb the sub processes jaxb
	 * @param nodeId the node id
	 * @param triggerMessage the trigger message
	 */
	void sendTriggerMessage(String clientId, TProcess processJaxb,
			ArrayList<TSubProcess> subProcessesJaxb, String nodeId,
			TriggerMessage triggerMessage);

	
}
