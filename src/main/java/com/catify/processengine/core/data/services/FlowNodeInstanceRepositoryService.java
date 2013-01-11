/**
 * -------------------------------------------------------
 * Copyright (C) 2013 catify <info@catify.com>
 * -------------------------------------------------------
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
/**
 * 
 */
package com.catify.processengine.core.data.services;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.catify.processengine.core.data.model.entities.FlowNodeInstance;

/**
 * The FlowNodeInstanceRepositoryService wraps methods of the FlowNodeRepository
 * and provides additional methods that hide implementation details.
 * 
 * @author chris
 */
@Component
public interface FlowNodeInstanceRepositoryService {

	/**
	 * Load a flow node instance from db.
	 * 
	 * @param uniqueProcessId
	 *            the unique process id
	 * @param uniqueFlowNodeId
	 *            the node id
	 * @param instanceId
	 *            the instance id
	 * @return the neo4j flow node instance
	 */
	FlowNodeInstance findFlowNodeInstance(String uniqueProcessId,
			String uniqueFlowNodeId, String instanceId);

	/**
	 * Load a flow node instance from db.
	 * 
	 * @param flowNodeGraphId
	 *            the flow node graph id
	 * @param processInstanceId
	 *            the process instance id
	 * @return the neo4j flow node instance
	 */
	FlowNodeInstance findFlowNodeInstance(Long flowNodeGraphId,
			String processInstanceId);

	/**
	 * Delete a flow node instance by graph id.
	 * 
	 * @param flowNodeInstance
	 *            the graphId of the flow node
	 * @return true, if successful, false if no process with given id found
	 */
	void delete(FlowNodeInstance flowNodeInstance);

	/**
	 * Save a FlowNodeInstance to the database.
	 * 
	 * @param FlowNodeInstance
	 *            the flow node
	 * @return the process
	 */
	FlowNodeInstance save(FlowNodeInstance flowNodeInstance);
	
	/**
	 * Find all flow node instances including sub processes (starting from a given process).
	 *
	 * @param uniqueProcessId the unique process id
	 * @param processInstanceId the process instance id
	 * @return the set of flow node instances
	 */
	Set<FlowNodeInstance> findAllFlowNodeInstances(String uniqueProcessId, String processInstanceId);
	
	/**
	 * Find all flow node instances and their flow node ids.
	 *
	 * @param uniqueProcessId the unique process id
	 * @param processInstanceId the process instance id
	 * @return the iterable
	 */
	Iterable<Map<String,Object>> findAllFlowNodeInstancesAndFlowNodeIds(String uniqueProcessId, String processInstanceId);
	
	/**
	 * Delete all flow node instance nodes.
	 *
	 * @param uniqueProcessId the unique process id
	 * @param processInstanceId the process instance id
	 */
	void deleteAllFlowNodeInstanceNodes(String uniqueProcessId, String processInstanceId);
	
	/**
	 * Find loosing flow node instances. Starts a search from the flow node
	 * instance provided and iterates the graph backwards
	 * 
	 * 
	 * @param uniqueProcessId
	 *            the unique process id
	 * @param uniqueFlowNodeId
	 *            the unique flow node id
	 * @param instanceId
	 *            the instance id
	 * @return the set of flow node instances that lost the race
	 */
	Iterable<FlowNodeInstance> findLoosingFlowNodeInstances(
			String uniqueProcessId, String uniqueFlowNodeId, String instanceId);
	
	/**
	 * Find flow node ids of previous loosing flow node instances.
	 *
	 * @param graphId the graph id
	 * @param state the state
	 * @return the sets the
	 */
	Set<String> findLoosingFlowNodeIds(String uniqueProcessId,
			String uniqueFlowNodeId, String instanceId);
	
	/**
	 * Find all flow nodes of a given process level (not including sub and parent processes) that have a given state.
	 *
	 * @param uniqueProcessId the unique process id
	 * @param uniqueFlowNodeId the unique flow node id
	 * @param instanceId the instance id
	 * @param state the state
	 * @return the set of flow node instances
	 */
	Set<FlowNodeInstance> findFlowNodeInstancesAtCurrentLevelByState(String uniqueFlowNodeId, String instanceId, String state);
}
