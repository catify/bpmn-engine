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
package com.catify.processengine.core.data.services.impl;

import java.util.ArrayList;

import javax.xml.bind.JAXBElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.catify.processengine.core.processdefinition.jaxb.TFlowElement;
import com.catify.processengine.core.processdefinition.jaxb.TFlowNode;
import com.catify.processengine.core.processdefinition.jaxb.TProcess;
import com.catify.processengine.core.processdefinition.jaxb.TSubProcess;
import com.catify.processengine.core.processdefinition.jaxb.services.ExtensionService;

/**
 * The Class IdService generates unique ids for processes and flow nodes.
 * Because the akka node services and the data services need the same
 * understanding of these ids, this is managed in a central service.
 */
public final class IdService {
	
	static final Logger LOG = LoggerFactory.getLogger(IdService.class);
	
	private IdService() {
		
	}
	
	public final static String ARCHIVEPREFIX = "Archive:";
	
	/**
	 * Gets the unique process id from a jaxb process, which is built via
	 * concatenation of the process id, name and version.
	 * 
	 * @param processJaxb
	 *            the jaxb process
	 * @return the unique process id
	 */
	public static String getUniqueProcessId(String clientId, TProcess processJaxb) {
//		String processVersion = "defaultVersion";
//		if (ExtensionService.getTVersion(processJaxb) != null) {
//			processVersion = ExtensionService.getTVersion(processJaxb).getVersion();
//		}

		return new String(clientId + processJaxb.getName() + processJaxb.getId()
				+ ExtensionService.getTVersion(processJaxb).getVersion());
	}
	
	/**
	 * Gets the unique process id from a jaxb process, which is built via
	 * concatenation of the process id, name and version.
	 * 
	 * @param processJaxb
	 *            the jaxb process
	 * @return the unique process id
	 */
	public static String getArchivedUniqueProcessId(String clientId, TProcess processJaxb) {
//		String processVersion = "defaultVersion";
//		if (ExtensionService.getTVersion(processJaxb) != null) {
//			processVersion = ExtensionService.getTVersion(processJaxb).getVersion();
//		}

		return new String(ARCHIVEPREFIX + clientId + processJaxb.getName() + processJaxb.getId()
				+ ExtensionService.getTVersion(processJaxb).getVersion());
	}
	
	/**
	 * Gets the unique flow node id, which is built via concatenation of the client id, the
	 * process id, name and version, the parent sub process ids and names and the flow node id and name.
	 *
	 * @param clientId the client id
	 * @param processJaxb the jaxb process
	 * @param subProcessesJaxb the jaxb sub processes
	 * @param flowNodeJaxb the jaxb flow node that the unique id is searched for
	 * @return the unique flow node id
	 */
	public static String getUniqueFlowNodeId(String clientId, TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb,
			TFlowNode flowNodeJaxb) {
		StringBuilder parentSubProcesses = getSubProcessesString(subProcessesJaxb);

		return new String(clientId 
				+ processJaxb.getId() + processJaxb.getName() + ExtensionService.getTVersion(processJaxb).getVersion() 
				+ parentSubProcesses
				+ flowNodeJaxb.getId() + flowNodeJaxb.getName());
	}
	
	/**
	 * Gets the unique flow node id of archive nodes, which is built via concatenation of the archive prefix, the client id, the
	 * process id, name and version, the parent sub process ids and names and the flow node id and name.
	 *
	 * @param clientId the client id
	 * @param processJaxb the jaxb process
	 * @param subProcessesJaxb the jaxb sub processes
	 * @param flowNodeJaxb the jaxb flow node that the unique id is searched for
	 * @return the unique flow node id
	 */
	public static String getArchivedUniqueFlowNodeId(String clientId, TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb,
			TFlowNode flowNodeJaxb) {
		StringBuilder parentSubProcesses = getSubProcessesString(subProcessesJaxb);
		
		return new String(ARCHIVEPREFIX + clientId 
				+ processJaxb.getId() + processJaxb.getName() + ExtensionService.getTVersion(processJaxb).getVersion() 
				+ parentSubProcesses
				+ flowNodeJaxb.getId() + flowNodeJaxb.getName());
	}
	
	
	/**
	 * Gets the unique flow node id of a top level flow node.
	 *
	 * @param clientId the client id
	 * @param processJaxb the process jaxb
	 * @param subProcessesJaxb the sub processes jaxb
	 * @param nodeId the node id
	 * @return the unique flow node id
	 */
	public static String getUniqueFlowNodeId(String clientId, TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb,
			String nodeId) {
		TFlowNode flowNode = getTFlowNodeById(processJaxb, nodeId);

		String uniqueFlowNodeId = IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb, flowNode);
		return uniqueFlowNodeId;
	}
	
	/**
	 * Gets the unique flow node id of a top level flow node.
	 *
	 * @param clientId the client id
	 * @param processJaxb the process jaxb
	 * @param subProcessesJaxb the sub processes jaxb
	 * @param nodeId the node id
	 * @return the unique flow node id
	 */
	public static String getArchivedUniqueFlowNodeId(String clientId, TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb,
			String nodeId) {
		TFlowNode flowNode = getTFlowNodeById(processJaxb, nodeId);

		String uniqueFlowNodeId = IdService.getArchivedUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb, flowNode);
		return uniqueFlowNodeId;
	}
	
	/**
	 * Gets the sub processes string (if any).
	 *
	 * @param subProcessesJaxb the sub processes jaxb
	 * @return the sub processes string
	 */
	private static StringBuilder getSubProcessesString(
			ArrayList<TSubProcess> subProcessesJaxb) {
		StringBuilder parentSubProcesses = new StringBuilder();
		
		if (subProcessesJaxb != null) {
			for (TSubProcess tSubProcess : subProcessesJaxb) {
				parentSubProcesses.append(tSubProcess.getId() + tSubProcess.getName());
			}
		}
		return parentSubProcesses;
	}
	
	/**
	 * Gets the flow node by id.
	 *
	 * @param processJaxb the process jaxb
	 * @param nodeId the node id
	 * @return the flow node by id
	 */
	private static TFlowNode getTFlowNodeById(TProcess processJaxb, String nodeId) {
		
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
