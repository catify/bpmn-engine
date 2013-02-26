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
package com.catify.processengine.core.data.repositories;

import java.util.Map;
import java.util.Set;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;

import com.catify.processengine.core.data.model.entities.FlowNodeInstance;

/**
 * The Spring Data neo4j managed interface FlowNodeInstanceRepository provides 
 * convenient methods for accessing {@link FlowNodeInstance}s in the database and 
 * has been enhanced with custom Cypher queries.
 * For implementation details please see {@link GraphRepository}.
 * 
 * @author christopher köster
 * 
 */
// * The speed of the queries needs to be tested under high load (see redmine #66).
public interface FlowNodeInstanceRepository extends GraphRepository<FlowNodeInstance> {

	/**
	 * Find a flow node instance based on the graph id of a flow node.
	 * 
	 * @param flowNodeGraphId
	 *            the flow node graph id
	 * @param processInstanceId
	 *            the process instance id
	 * @return the flow node instance
	 */
	@Query("start flownode=node({0}) match flownode-[r:HAS_INSTANCE]->flownodeinstance where r.processInstanceId = {1} and flownodeinstance.loopCount = {2} return flownodeinstance")
	FlowNodeInstance findFlowNodeInstance(Long flowNodeGraphId,
			String processInstanceId, int loopCount);

	/**
	 * Find a flow node instance (starting from a given process). 
	 * 
	 * @param uniqueProcessId
	 *            the unique process id
	 * @param uniqueFlowNodeId
	 *            the node id
	 * @param processInstanceId
	 *            the process instance id
	 * @return the flow node instance
	 */
	@Query("start process=node:ProcessNode(uniqueProcessId={0}) match process-[r1:HAS*1..1000]->flownode-[r2:HAS_INSTANCE]->flownodeinstance where flownode.uniqueFlowNodeId = {1} and r2.processInstanceId = {2} and flownodeinstance.loopCount = {3} return flownodeinstance")
	FlowNodeInstance findFlowNodeInstance(String uniqueProcessId, String uniqueFlowNodeId, String processInstanceId, int loopCount);
	
	/**
	 * Find a flow node instance (starting from a given process). 
	 * 
	 * @param uniqueProcessId
	 *            the unique process id
	 * @param uniqueFlowNodeId
	 *            the node id
	 * @param processInstanceId
	 *            the process instance id
	 * @return the flow node instance
	 */
	@Query("start process=node:ProcessNode(uniqueProcessId={0}) match process-[r1:HAS*1..1000]->flownode-[r2:HAS_INSTANCE]->flownodeinstance where flownode.uniqueFlowNodeId = {1} and r2.processInstanceId = {2} return MAX(flownodeinstance.loopCount)")
	Integer getFlowNodeInstanceMaxLoopCount(String uniqueProcessId, String uniqueFlowNodeId, String processInstanceId);

	/**
	 * Find flow node instances (starting at a list of flow nodes). Can be used if the flow nodes of the instances searched are already known.
	 * 
	 * @param targetNodes
	 *            the list of flow node graphIds
	 * @param instanceId
	 *            the process instance id
	 * @return the set of flow node instances
	 */
	@Query("start flownode=node({0}) match flownode-[r:HAS_INSTANCE]->flownodeinstance where r.processInstanceId = {1} return flownodeinstance")
	Set<FlowNodeInstance> findFlowNodeInstances(Set<Long> targetNodes, String instanceId);
	
	/**
	 * Find all flow node instances including sub processes (starting from a given process).
	 *
	 * @param uniqueProcessId the unique process id
	 * @param processInstanceId the process instance id
	 * @return the set of flow node instances
	 */
	@Query("start process=node:ProcessNode(uniqueProcessId={0}) match process-[r1:HAS*1..1000]->flownode-[r2:HAS_INSTANCE]->flownodeinstance return r2.processInstanceId")
	Set<String> findAllFlowNodeInstances(String uniqueProcessId);
	
	/**
	 * Find all flow node instances including sub processes (starting from a given process).
	 *
	 * @param uniqueProcessId the unique process id
	 * @param processInstanceId the process instance id
	 * @return the set of flow node instances
	 */
	@Query("start process=node:ProcessNode(uniqueProcessId={0}) match process-[r1:HAS*1..1000]->flownode-[r2:HAS_INSTANCE]->flownodeinstance where r2.processInstanceId = {1} return flownodeinstance")
	Set<FlowNodeInstance> findAllFlowNodeInstances(String uniqueProcessId, String processInstanceId);
	
	/**
	 * Find all flow node instances of a flow node at a given state.
	 *
	 * @param uniqueProcessId the unique process id
	 * @param uniqueFlowNodeId the unique flow node id
	 * @param state the flow node instance state
	 * @return the set of flow node instance ids at the given state of that node
	 */
	@Query("start process=node:ProcessNode(uniqueProcessId={0}) match process-[r1:HAS*1..1000]->flownode-[r2:HAS_INSTANCE]->flownodeinstance where flownode.uniqueFlowNodeId = {1} and flownodeinstance.nodeInstanceState = {2} return r2.processInstanceId")
	Set<String> findAllFlowNodeInstancesAtState(String uniqueProcessId, String uniqueFlowNodeId, String state);
	
	/**
	 * Find all flow node instances including sub processes (starting from a given process).
	 *
	 * @param uniqueProcessId the unique process id
	 * @param processInstanceId the process instance id
	 * @return the set of flow node instances
	 */
	@Query("start process=node:ProcessNode(uniqueProcessId={0}) match process-[r1:HAS*1..1000]->flownode-[r2:HAS_INSTANCE]->flownodeinstance where r2.processInstanceId = {1} return flownodeinstance, flownode.uniqueFlowNodeId")
	Iterable<Map<String,Object>> findAllFlowNodeInstancesAndFlowNodeIds(String uniqueProcessId, String processInstanceId);
	
	/**
	 * Delete all instance nodes (including flow node instance nodes and process instance nodes). 
	 *
	 * @param uniqueProcessId the unique process id
	 * @param processInstanceId the process instance id
	 */
	@Query("start process=node:ProcessNode(uniqueProcessId={0}) match ()-[rOtherPi]-processinstance<-[rPi:HAS_PROCESS_INSTANCE]-process-[r1:HAS*1..1000]->flownode-[r2:HAS_INSTANCE]->flownodeinstance-[rOther]-() where r2.processInstanceId = {1} and processinstance.processInstanceId = {1} DELETE r2, rOther, flownodeinstance, rPi, rOtherPi, processinstance")
	void deleteAllInstanceNodes(String uniqueProcessId, String processInstanceId);
	
	/**
	 * Delete all flow node instance nodes.
	 *
	 * @param uniqueProcessId the unique process id
	 * @param processInstanceId the process instance id
	 */
	@Query("start process=node:ProcessNode(uniqueProcessId={0}) match process-[r1:HAS*1..1000]->flownode-[r2:HAS_INSTANCE]->flownodeinstance-[rOther]-() where r2.processInstanceId = {1} DELETE r2, rOther, flownodeinstance")
	void deleteAllFlowNodeInstanceNodes(String uniqueProcessId, String processInstanceId);
	
	/**
	 * Delete all process instance nodes.
	 *
	 * @param uniqueProcessId the unique process id
	 * @param processInstanceId the process instance id
	 */
	@Query("start process=node:ProcessNode(uniqueProcessId={0}) match process-[rPi:HAS_PROCESS_INSTANCE]->processinstance-[rOther]-() where processinstance.processInstanceId = {1} DELETE rPi, rOther, processinstance")
	void deleteAllProcessInstanceNodes(String uniqueProcessId, String processInstanceId);
	
	/**
	 * Find all flow nodes of a given process level (not including sub and parent processes) by a given state.
	 *
	 * @param uniqueProcessId the unique process id
	 * @param uniqueFlowNodeIdParent the unique flow node id of the parent node (might be a process or sub process)
	 * @param instanceId the instance id
	 * @param state the state
	 * @return the set of flow node instances
	 */
	@Query("start currentNode=node:FlowNode(uniqueFlowNodeId={0}) match currentNode<-[r1:HAS]-parentFlowNode-[r2:HAS]->childFlowNode-[r3:HAS_INSTANCE]->flownodeinstance where r3.processInstanceId = {1} and flownodeinstance.nodeInstanceState = {2} return flownodeinstance")
	Set<FlowNodeInstance> findFlowNodeInstancesAtCurrentLevelByState(String uniqueFlowNodeId, String instanceId, String state);
	
	/**
	 * Find previous flow node ids of instances that are not in a given state.
	 *
	 * @param graphId the graph id
	 * @param state the first state to check
	 * @param state2 the second state to check
	 * @return the sets the
	 */
	// two versions of the same cypher query: the first is pretty simple and will return _all_ previous nodes, that are not one of the states, 
	// the second query will return _only_ the active nodes until it reaches on of the states --> this could be slower, but might have less side effects
//	@Query("start startingNodeInstance=node({0}) match startingNodeInstance<-[r1:HAS_FOLLOWING_INSTANCE*1..1000]-previousNodeInstance<-[r2:HAS_INSTANCE]-flownode where (previousNodeInstance.nodeInstanceState <> {1} OR previousNodeInstance.nodeInstanceState <> {2}) return flownode.uniqueFlowNodeId")
	@Query("start startingNodeInstance=node({0}) match path=startingNodeInstance<-[r1:HAS_FOLLOWING_INSTANCE*1..1000]-previousNodeInstance, previousNodeInstance<-[r2:HAS_INSTANCE]-flownode WHERE ALL(n in NODES(path) WHERE (n.nodeInstanceState <> {1} OR n.nodeInstanceState <> {2})) return flownode.uniqueFlowNodeId")
	Set<String> findPreviousFlowNodeIdsNotInGivenStates(Long graphId, String state, String state2);
	
	
	
	
	
	
	
	
	
	// prepared archive query, when cypher is able to add new properties to the index
	
//	@Query("START process=node:ProcessNode(uniqueProcessId={0}) MATCH process-[r1:HAS*1..1000]->flownode " +
//	"WITH flownode MATCH path = (flownode-[r2:HAS_INSTANCE]->flownodeinstance) WHERE r2.processInstanceId = {1} " +
//	"WITH path CREATE a-[r:RELTYPE]->b")
//Set<FlowNodeInstance> archiveAllFlowNodeInstanceNodes(String uniqueProcessId, String processInstanceId, String uniqueProcessIdArchived);
}
