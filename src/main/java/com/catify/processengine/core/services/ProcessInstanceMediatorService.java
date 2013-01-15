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
package com.catify.processengine.core.services;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.catify.processengine.core.data.model.NodeInstaceStates;
import com.catify.processengine.core.data.model.entities.FlowNode;
import com.catify.processengine.core.data.model.entities.FlowNodeInstance;
import com.catify.processengine.core.data.model.entities.ProcessInstanceNode;
import com.catify.processengine.core.data.model.entities.ProcessNode;
import com.catify.processengine.core.data.services.FlowNodeInstanceRepositoryService;
import com.catify.processengine.core.data.services.FlowNodeRepositoryService;
import com.catify.processengine.core.data.services.ProcessInstanceNodeRepositoryService;
import com.catify.processengine.core.data.services.ProcessRepositoryService;
import com.catify.processengine.core.processdefinition.jaxb.TStartEvent;
import com.catify.processengine.core.processdefinition.jaxb.TSubProcess;

/**
 * The ProcessInstanceMediatorService handles the data access between node
 * services ({@link com.catify.processengine.core.nodes}) and their database
 * representations ({@link com.catify.processengine.core.data.model.entities})
 * on a process wide scope. The service is therefore used if a node service 
 * needs access to more than its own database representation (eg. a start node
 * that instantiates other nodes).
 * 
 * @author chris
 * 
 */
@Component
public class ProcessInstanceMediatorService {

	static final Logger LOG = LoggerFactory.getLogger(ProcessInstanceMediatorService.class);
	
	@Autowired 
	private Neo4jTemplate neo4jTemplate;
	
	@Autowired
	private ProcessRepositoryService processRepositoryService;
	
	@Autowired 
	private FlowNodeRepositoryService flowNodeRepositoryService;
	
	@Autowired 
	private ProcessInstanceNodeRepositoryService processInstanceNodeRepositoryService;
	
	@Autowired 
	private FlowNodeInstanceRepositoryService flowNodeInstanceRepositoryService;

	private ProcessInstanceNode processInstanceNode;
	
	public ProcessInstanceMediatorService() {

	}
	
	/**
	 * Load process instance node. The method will try to avoid unnecessary db lookups if possible.
	 *
	 * @param processInstanceId the process instance id
	 * @return the process instance node
	 */
	public ProcessInstanceNode findProcessInstanceNode(String uniqueProcessId, String processInstanceId) {
		return loadProcessInstanceNode(uniqueProcessId, processInstanceId);
	}
	
	/**
	 * Create a new process instance (database level).
	 * 
	 * @return the processInstanceId
	 */
	@Transactional
	public synchronized String createProcessInstance(String uniqueProcessId) {
		// get a unique id as process instance id
		String processInstanceId = UUID.randomUUID().toString();

		// get the process
		ProcessNode process = processRepositoryService
				.findByUniqueProcessId(uniqueProcessId);
		
		LOG.debug(String.format(
				"Starting to instantiate process %s with instanceId %s",
				process, processInstanceId));
		
		// create a process instance node
		ProcessInstanceNode processInstanceNode = new ProcessInstanceNode(process,
				processInstanceId, new Date());

		Set<FlowNode> flowNodes = neo4jTemplate.fetch(process.getFlowNodes());
		
		// create an instance of each FlowNode
		createFlowNodeInstances(processInstanceId, flowNodes);

		// create relationships between process instance node and start node instances
		// FIXME: this is a pretty costly operation and should be evaluated
		for (FlowNode flowNode : flowNodes) {
			if (flowNode.getNodeType().equals(TStartEvent.class.toString())) {
				FlowNodeInstance startNodeInstance = flowNodeInstanceRepositoryService
				.findFlowNodeInstance(uniqueProcessId, flowNode.getUniqueFlowNodeId(), processInstanceId);
				processInstanceNode.addRelationshipToStartEventInstance(startNodeInstance);
			}
		}
		
		processInstanceNodeRepositoryService.save(processInstanceNode);

		LOG.debug(String.format(
				"Finished instantiating process %s with instanceId %s",
				process, processInstanceId));

		return processInstanceId;
	}
	
	/**
	 * Creates the flow node instances.
	 *
	 * @param processInstanceId the process instance id
	 * @param flowNodes the flow nodes
	 * @return the sets the
	 */
	private Collection<FlowNodeInstance> createFlowNodeInstances(String processInstanceId, Set<FlowNode> flowNodes) {
		
		Map<FlowNode, FlowNodeInstance> flowNodeInstances = new HashMap<FlowNode, FlowNodeInstance>();

		for (FlowNode flowNode : flowNodes) {
			LOG.debug(String
					.format("FlowNode to be instantiated: %s", flowNode));
			
			FlowNodeInstance flowNodeInstance = new FlowNodeInstance(
					NodeInstaceStates.INACTIVE_STATE,
					flowNode.getFiredFlowsNeeded());
			
			flowNodeInstance.addAsInstanceOf(flowNode, processInstanceId);
			
			flowNodeInstanceRepositoryService.save(flowNodeInstance);
			
			flowNodeInstances.put(flowNode, flowNodeInstance);
			
			// create nested sub process flow node instances 
			if (flowNode.getNodeType().equals(TSubProcess.class.toString())) {
				this.createFlowNodeInstances(processInstanceId, neo4jTemplate.fetch(flowNode.getSubProcessNodes()));
			}
		}   
		
		// create relationships between the node instances
		for (FlowNode flowNode : flowNodeInstances.keySet()) {
			
			// get the flow node this instance node is an instance of (it has
			// the needed relationship information)
			FlowNodeInstance flowNodeInstance = flowNodeInstances.get(flowNode);
			
			// mirror the flow node relationships to the flow node instances
			for (FlowNode followingFlowNode : neo4jTemplate.fetch(flowNode.getFollowingFlowNodes())) {

				// (FIXME: evaluate if a query beginning at the process might be faster)
				FlowNodeInstance followingFlowNodeInstance = flowNodeInstanceRepositoryService
						.findFlowNodeInstance(followingFlowNode.getGraphId(),
								processInstanceId);

				flowNodeInstance.addFollowingInstance(followingFlowNodeInstance);

				flowNodeInstanceRepositoryService.save(flowNodeInstance);
			}
		}
		return flowNodeInstances.values();
	}

	/**
	 * Gets the unique flow node ids of previous nodes that lost a race condition 
	 * (eg. when a gateway closes an incoming branch).
	 * @param uniqueProcessId the unique process id
	 * @param uniqueFlowNodeId the unique flow node id
	 * @param processInstanceId the process instance id
	 *
	 * @return the previous loosing node ids
	 */
	public Set<String> getPreviousLoosingNodeIds(String uniqueProcessId,
			String uniqueFlowNodeId, String processInstanceId) {

		return flowNodeInstanceRepositoryService.findLoosingFlowNodeIds(uniqueProcessId, uniqueFlowNodeId, processInstanceId);
	}

	/**
	 * Archive a process instance.
	 * <p> The {@link ProcessInstanceNode} and the {@link FlowNodeInstance}s will be moved to the
	 * archive representation of the process node.
	 *
	 * @param uniqueProcessId the unique process id
	 * @param processInstanceId the process instance id
	 */
	@Transactional
	public synchronized void archiveProcessInstance(String uniqueProcessId, String processInstanceId, Date endTime) {	
		
		// save the flow node instances to their archived flow nodes
		Iterable<Map<String,Object>> result = flowNodeInstanceRepositoryService.findAllFlowNodeInstancesAndFlowNodeIds(uniqueProcessId, processInstanceId);
		for (Map<String, Object> map : result) {
			FlowNodeInstance flowNodeInstance = neo4jTemplate.convert(map.get("flownodeinstance"), FlowNodeInstance.class);
			String flowNodeId = (String) map.get("flownode.uniqueFlowNodeId");
			
			FlowNode archivedFlowNode = flowNodeRepositoryService.findArchivedByRunningUniqueFlowNodeId(flowNodeId);
			flowNodeInstance.moveToArchive(archivedFlowNode, processInstanceId);
		}
		
		// save the process instance node to the archived process node 
		ProcessNode archivedProcessNode = processRepositoryService.findArchivedByRunningUniqueProcessId(uniqueProcessId);
		ProcessInstanceNode processInstanceNode = loadProcessInstanceNode(uniqueProcessId, processInstanceId);

		processInstanceNode.moveInstanceToArchive(archivedProcessNode, processInstanceId);
		
		processInstanceNode.setProcessInstanceEndTime(endTime);
	}
	
	/**
	 * Delete a process instance.
	 * <p> The {@link ProcessInstanceNode} and the {@link FlowNodeInstance}s will be deleted via cypher.
	 *
	 * @param processInstanceId the process instance id
	 */
	@Transactional
	public synchronized void deleteProcessInstance(String uniqueProcessId, String processInstanceId) {
		flowNodeInstanceRepositoryService.deleteAllFlowNodeInstanceNodes(uniqueProcessId, processInstanceId);
	}

	/**
	 * Sets the process instance start time.
	 *
	 * @param processInstanceId the process instance id
	 * @param startTime the start time
	 */
	public void setProcessInstanceStartTime(String uniqueProcessId, String processInstanceId, Date startTime) {
		ProcessInstanceNode processInstanceNode = loadProcessInstanceNode(uniqueProcessId, processInstanceId);
		processInstanceNode.setProcessInstanceStart(startTime);
		processInstanceNodeRepositoryService.save(processInstanceNode);
	}
	
	/**
	 * Sets the process instance end time.
	 *
	 * @param processInstanceId the process instance id
	 * @param endTime the end time
	 */
	public void setProcessInstanceEndTime(String uniqueProcessId, String processInstanceId, Date endTime) {
		ProcessInstanceNode processInstanceNode = loadProcessInstanceNode(uniqueProcessId, processInstanceId);
		processInstanceNode.setProcessInstanceEndTime(endTime);
		processInstanceNodeRepositoryService.save(processInstanceNode);
	}
	
	/**
	 * Sets the meta data properties.
	 *
	 * @param processInstanceId the process instance id
	 * @param metaData the meta data
	 */
	@Transactional
	public void setMetaDataProperties(String uniqueProcessId, String processInstanceId, Map<String, Object> metaData) {
		ProcessInstanceNode processInstanceNode = loadProcessInstanceNode(uniqueProcessId, processInstanceId);

		// the instance node could have been moved to the archive (because archiving and writing of meta data happens asynchronously) 
		if (processInstanceNode == null) {
			processInstanceNode = processInstanceNodeRepositoryService.findArchivedProcessInstanceNode(uniqueProcessId, processInstanceId);
		}

		processInstanceNode.setMetaDataProperties(processInstanceNode
				.getMetaDataProperties().createFrom(metaData));
		
		LOG.debug("Setting meta data properties on node " + processInstanceNode.getGraphId());

		processInstanceNodeRepositoryService.save(processInstanceNode);
	}

	/**
	 * Load process instance node. The method will try to avoid unnecessary db lookups if possible.
	 *
	 * @param processInstanceId the process instance id
	 * @return the process instance node
	 */
	private ProcessInstanceNode loadProcessInstanceNode(String uniqueProcessId, String processInstanceId) {
		// check if processInstance has already been loaded (eg. by another
		// operation) to avoid unnecessary db lookups
		if (this.processInstanceNode == null
				|| !this.processInstanceNode.getInstanceId().equals(processInstanceId)) {
			return processInstanceNodeRepositoryService.findProcessInstanceNode(uniqueProcessId, processInstanceId);
		} else {
			return this.processInstanceNode;
		}
	}

	/**
	 * Find active flow node instances at current level only. This excludes instances of flow nodes 
	 * in sub processes and flow node instances at a higher level than the provided flow node.
	 *
	 * @param uniqueFlowNodeId the unique flow node id
	 * @param processInstanceId the process instance id
	 * @return the set of FlowNodeInstances
	 */
	public Set<FlowNodeInstance> findActiveFlowNodeInstances(String uniqueFlowNodeId,
			String processInstanceId) {
		return flowNodeInstanceRepositoryService.findFlowNodeInstancesAtCurrentLevelByState(uniqueFlowNodeId, processInstanceId, NodeInstaceStates.ACTIVE_STATE);
	}
}
