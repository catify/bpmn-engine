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
package com.catify.processengine.core.data.services;

import com.catify.processengine.core.nodes.eventdefinition.EventDefinitionParameter;
import com.catify.processengine.core.processdefinition.jaxb.TFlowNode;
import com.catify.processengine.core.processdefinition.jaxb.TProcess;
import com.catify.processengine.core.processdefinition.jaxb.services.ExtensionService;

/**
 * Bean to create IDs.
 * 
 * @author claus straube
 *
 */
public class IdParams {
	
	/**
	 * Fill Bean to get a Process ID.
	 * 
	 * @param clientId
	 * @param processName
	 * @param processVersion
	 * @param processId
	 */
	public IdParams(String clientId, String processName, String processVersion, String processId) {
		this.setProcessParams(clientId, processName, processVersion, processId);
	}
	
	/**
	 * Fill Bean to get a Process ID.
	 * 
	 * @param clientId
	 * @param processJaxb
	 */
	public IdParams(String clientId, TProcess processJaxb) {
		this.setProcessParams(clientId, processJaxb.getName(), ExtensionService.getTVersion(processJaxb).getVersion(), processJaxb.getId());
	}
	
	/**
	 * Fill Bean to get a Process ID.
	 * 
	 * @param params
	 */
	public IdParams(EventDefinitionParameter params) {
		this.setProcessParams(params.clientId,  params.processJaxb.getName(), ExtensionService.getTVersion(params.processJaxb).getVersion(), params.processJaxb.getId());
	}
	
	/**
	 * Fill Bean to get a Node ID.
	 * 
	 * @param clientId
	 * @param processName
	 * @param processVersion
	 * @param processId
	 * @param subProcesses
	 * @param flowNodeId
	 * @param flowNodeName
	 */
	public IdParams(String clientId, String processName, String processVersion, String processId, String subProcesses, String flowNodeId, String flowNodeName) {
		this.setProcessParams(clientId, processName, processVersion, processId);
		this.setNodeParams(flowNodeId, flowNodeName, subProcesses);
	}
	
	/**
	 * Fill Bean to get a Node ID.
	 * 
	 * @param clientId
	 * @param processJaxb
	 * @param subProcesses
	 * @param flowNodeJaxb
	 */
	public IdParams(String clientId, TProcess processJaxb, String subProcesses, TFlowNode flowNodeJaxb) {
		this.setProcessParams(clientId, processJaxb.getName(), ExtensionService.getTVersion(processJaxb).getVersion(), processJaxb.getId());
		this.setNodeParams(flowNodeJaxb.getId(), flowNodeJaxb.getName(), subProcesses);
	}
	
	private String clientId;
	private String processName;
	private String processVersion;
	private String processId;
	private String subProcesses;
	private String flowNodeId;
	private String flowNodeName;
	
	private void setProcessParams(String clientId, String processName, String processVersion, String processId) {
		this.clientId = clientId;
		this.processName = processName;
		this.processId = processId;
		this.processVersion = processVersion;
	}
	
	private void setNodeParams(String nodeId, String nodeName, String subProcesses) {
		this.flowNodeId = nodeId;
		this.flowNodeName = nodeName;
		this.subProcesses = subProcesses;
	}
	
	public String getClientId() {
		return clientId;
	}

	public String getProcessName() {
		return processName;
	}

	public String getProcessVersion() {
		return processVersion;
	}

	public String getProcessId() {
		return processId;
	}
	
	public String getSubProcesses() {
		return subProcesses;
	}

	public String getFlowNodeId() {
		return flowNodeId;
	}

	public String getFlowNodeName() {
		return flowNodeName;
	}
	
	/**
	 * Creates a unique process ID (client id + process name + process id + process version).
 	 * 
	 * @return process id
	 */
	public String getUniqueProcessId() {
		return new String(this.clientId + this.processName + this.processId + this.processVersion);
	}
	
	/**
	 * Creates a unique  process ID with the given prefix (prefix + client id + process name + process id + process version).
	 * 
	 * @param prefix
	 * @return process id with prefix
	 */
	public String getUniqueProcessId(String prefix) {
		return prefix + this.getUniqueProcessId();
	}
	
	/**
	 * Creates a unique node ID (unique process id + sub processes + flow node id + flow node name).
	 * 
	 * @return node id
	 */
	public String getUniqueFlowNodeId() {
		return new String(this.getUniqueProcessId() + this.subProcesses + this.flowNodeId + this.flowNodeName);
	}
	
	/**
	 * Creates a unique node ID with the given prefix (prefix + unique process id + sub processes + flow node id + flow node name).
	 * 
	 * @param prefix
	 * @return node id
	 */
	public String getUniqueFlowNodeId(String prefix) {
		return prefix + this.getUniqueFlowNodeId();
	}
	
}
