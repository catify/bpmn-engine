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

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.catify.processengine.core.data.model.NodeInstaceStates;
import com.catify.processengine.core.data.model.entities.FlowNode;
import com.catify.processengine.core.data.model.entities.FlowNodeInstance;
import com.catify.processengine.core.data.services.FlowNodeInstanceRepositoryService;

/**
 * The NodeInstanceMediatorService handles all data access between the node
 * services ({@link com.catify.processengine.core.nodes}) and their database
 * representation ({@link com.catify.processengine.core.data.model.entities}).
 * The NodeInstanceMediatorService can only access the database node that has
 * been assigned to it at process start. For process wide access responsibility
 * use {@link ProcessInstanceMediatorService}.
 * Note: Only use a single instance for each {@link FlowNode}! Otherwise the 
 * internal caching mechanism will lead to information loss.
 * 
 * @author christopher köster
 * 
 */
@Configurable
public class NodeInstanceMediatorService {

	static final Logger LOG = LoggerFactory
			.getLogger(NodeInstanceMediatorService.class);
	
	@Autowired 
	private Neo4jTemplate neo4jTemplate;
	
	@Autowired
	private FlowNodeInstanceRepositoryService flowNodeInstanceRepositoryService;

	/** The flow node instance is cached to reduce the need for querying and act as a local cache. */
	private FlowNodeInstance nodeInstance;
	
	/** The loop count of the current flow node instance is saved for the caching check. */
	private int loopCount;

	/** The unique process id. */
	private String uniqueProcessId;
	
	/** The unique flow node id. */
	private String uniqueFlowNodeId;


	/**
	 * Instantiates a new node instance mediator service.
	 */
	public NodeInstanceMediatorService() {
	}

	/**
	 * Instantiates a new node instance mediator service.
	 *
	 * @param uniqueProcessId the unique process id
	 * @param uniqueFlowNodeId the unique flow node id
	 */
	@Autowired
	public NodeInstanceMediatorService(String uniqueProcessId,
			String uniqueFlowNodeId) {
		this.uniqueProcessId = uniqueProcessId;
		this.uniqueFlowNodeId = uniqueFlowNodeId;
	}

	/**
	 * Creates a new node instance at the flow node managed by this service instance.
	 *
	 * @param processInstanceId the process instance id
	 * @param loopCount the loop count
	 */
	public void createNewNodeInstance(String processInstanceId, int loopCount) {
		FlowNode flowNode = this.nodeInstance.getFlowNode(neo4jTemplate);
		
		FlowNodeInstance flowNodeInstance = new FlowNodeInstance(NodeInstaceStates.INACTIVE_STATE, flowNode.getFiredFlowsNeeded(), loopCount);
		flowNodeInstance.addAsInstanceOf(flowNode, processInstanceId);
		flowNodeInstanceRepositoryService.save(flowNodeInstance);
		
		this.updateCache(flowNodeInstance, loopCount);
	}

	/**
	 * Creates a warn log if the sanity check failed.
	 *
	 * @param newState the new state
	 */
	private void createSanityCheckFailedLog(String newState) {
		LOG.warn(String
				.format("stateTransistionSanityCheck failed. State of graphId %s (%s) was at %s and should be set to %s",
						this.nodeInstance.getGraphId(),
						neo4jTemplate.fetch(this.nodeInstance.getFlowNode()).getUniqueFlowNodeId(),
						this.nodeInstance.getNodeInstanceState(), newState));
	}
	
	/**
	 * Gets the fired flows needed.
	 * 
	 * @param instanceId
	 *            the instance id
	 * @return the fired flows needed
	 */
	public int getIncomingFiredFlowsNeeded(String processInstanceId) {
		return this.getNodeInstance(processInstanceId).getIncomingFiredFlowsNeeded();
	}

	/**
	 * Gets the (current maximum) loop count of a node instance.
	 *
	 * @param processInstanceId the process instance id
	 * @return the loop count or 0 if the nodeInstance is NULL
	 */
	public int getLoopCount(String processInstanceId) {
		FlowNodeInstance nodeInstance = getNodeInstance(processInstanceId);

		if (nodeInstance == null) {
			return 0;
		} else {
			return nodeInstance.getLoopCount();
		} 
	}

	/**
	 * Gets the node instance.
	 * 
	 * @param instanceId
	 *            the instance id
	 * @return the node instance
	 */
	public FlowNodeInstance getNodeInstance(String processInstanceId) {
		return loadFlowNodeInstance(this.uniqueProcessId,
				this.uniqueFlowNodeId, processInstanceId);
	}

	public Date getNodeInstanceStartTime(String processInstanceId) {
		return this.getNodeInstance(processInstanceId).getNodeInstanceStartTime();
	}
	
	/**
	 * Gets the state of a flow node instance.
	 *
	 * @param processInstanceId the process instance id
	 * @return the state
	 */
	public String getNodeInstanceState(String processInstanceId) {
		FlowNodeInstance nodeInstance = getNodeInstance(processInstanceId);

		if (nodeInstance == null) {
			return null;
		} else {
			return nodeInstance.getNodeInstanceState();
		}
	}
	
	/**
	 * Gets the sequence flows fired.
	 * 
	 * @param instanceId
	 *            the instance id
	 * @return the sequence flows fired
	 */
	public int getSequenceFlowsFired(String processInstanceId) {
		return this.getNodeInstance(processInstanceId).getFlowsFired();
	}

	/**
	 * Gets the unique flow node id.
	 *
	 * @return the unique flow node id
	 */
	public String getUniqueFlowNodeId() {
		return uniqueFlowNodeId;
	}

	/**
	 * Gets the unique process id.
	 *
	 * @return the unique process id
	 */
	public String getUniqueProcessId() {
		return uniqueProcessId;
	}

	/**
	 * Checks if node instance is in "active state".
	 *
	 * @param processInstanceId the process instance id
	 * @return true, if is active
	 */
	public boolean isActive(String processInstanceId) {
		return this.getNodeInstanceState(processInstanceId).equals(NodeInstaceStates.ACTIVE_STATE);
	}
	
	/**
	 * Checks if the node instance has been initialized (is != null).
	 *
	 * @return true, if is initialized
	 */
	public boolean isInitialized(){
		return (this.nodeInstance != null);
	}
	
	/**
	 * Load a flow node instance from the cache of this object if possible or else from the db.
	 * 
	 * @param uniqueProcessId
	 *            the unique process id
	 * @param uniqueFlowNodeId
	 *            the unique flow node id
	 * @param instanceId
	 *            the instance id
	 * @return the neo4j flow node instance
	 */
	private FlowNodeInstance loadFlowNodeInstance(String uniqueProcessId,
			String uniqueFlowNodeId, String processInstanceId) {
		// check if nodeInstance has already been loaded (eg. by another operation) to avoid unnecessary db lookups
		if (this.nodeInstance == null || 
				! ( (this.nodeInstance.getHasInstanceRelationship().getProcessInstanceId().equals(processInstanceId))
						&& this.nodeInstance.getLoopCount() == this.loopCount)) {

		return refreshFlowNodeInstance(uniqueProcessId, uniqueFlowNodeId, processInstanceId);
		} else { // we are still in the same node instance
			return this.nodeInstance;
		}
	}
	
	/**
	 * Persist any changes to the flow node instance to the db.
	 */
	@Transactional //FIXME: need to test if transaction is needed
	public void persistChanges() {
		flowNodeInstanceRepositoryService.save(this.nodeInstance);
	}
	
	/**
	 * Load flow node instance directly from db, avoiding any cached objects. <br><br>
	 * <b>Only use this method directly if you are working with multiple {@link NodeInstanceMediatorService}s
	 * on the same {@link FlowNode} or you explicitly need to refresh the node instance attached to this service instance.</b> <br><br>
	 * For all other cases use {@link NodeInstanceMediatorService#loadFlowNodeInstanceFromCache}.
	 *
	 * @param uniqueProcessId the unique process id
	 * @param uniqueFlowNodeId the unique flow node id
	 * @param processInstanceId the process instance id
	 * @return the flow node instance
	 */
	public FlowNodeInstance refreshFlowNodeInstance(String uniqueProcessId,
			String uniqueFlowNodeId, String processInstanceId) {
		// we need to get the latest/current flow node instance
		int currentLoopCount = flowNodeInstanceRepositoryService.getFlowNodeInstanceMaxLoopCount(uniqueProcessId, uniqueFlowNodeId, processInstanceId);
			
		FlowNodeInstance flowNodeInstance = flowNodeInstanceRepositoryService.findFlowNodeInstance(
				uniqueProcessId, uniqueFlowNodeId, processInstanceId, currentLoopCount);
		
		LOG.debug(String.format(
				"Searching FlowNodeInstance in db with parameters: %s, %s, %s. Found: %s",
				uniqueProcessId, uniqueFlowNodeId, processInstanceId,
				flowNodeInstance));
		
		this.updateCache(flowNodeInstance, currentLoopCount);
		
		return flowNodeInstance;
	}
	
	/**
	 * Shortcut to set "active state" for node.
	 * 
	 * @param processInstanceId
	 */
	public void setActive(String processInstanceId) {
		this.setState(processInstanceId, NodeInstaceStates.ACTIVE_STATE);
	}
	

	/**
	 * Shortcut to set "deactivated state" for node.
	 * 
	 * @param processInstanceId
	 */
	public void setDeactivated(String processInstanceId) {
		this.setState(processInstanceId, NodeInstaceStates.DEACTIVATED_STATE);
	}

	/**
	 * Sets the fired flows needed.
	 * 
	 * @param firedFlowsNeeded
	 *            the new fired flows needed
	 */
	public void setFiredFlowsNeeded(String processInstanceId,
			int firedFlowsNeeded) {
		FlowNodeInstance nodeInstance = getNodeInstance(processInstanceId);

		LOG.debug(String.format("Setting firedFlowsNeeded of %s from %s to %s",
				nodeInstance, nodeInstance.getIncomingFiredFlowsNeeded(),
				firedFlowsNeeded));
		nodeInstance.setIncomingFiredFlowsNeeded(firedFlowsNeeded);
		
		this.updateCache(nodeInstance, nodeInstance.getLoopCount());
	}
	
	/**
	 * Shortcut to set "inactive state" for node.
	 * 
	 * @param processInstanceId
	 */
	public void setInactive(String processInstanceId) {
		this.setState(processInstanceId, NodeInstaceStates.INACTIVE_STATE);
	}
	
	/**
	 * Set the loop count of a node instance.
	 *
	 * @param processInstanceId the process instance id
	 * @return the loop count
	 */
	public void setLoopCount(String processInstanceId, int loopCount) {
		FlowNodeInstance nodeInstance = getNodeInstance(processInstanceId);

		if (nodeInstance != null) {
			nodeInstance.setLoopCount(loopCount);
			this.updateCache(nodeInstance, loopCount);
		} else {
			LOG.error(String.format("Loop Count should be set to %s, but flow node instance was NULL", loopCount));
		}
	}
	
	public void setNodeInstanceEndTime(String processInstanceId, Date nodeInstanceEnd) {
		FlowNodeInstance nodeInstance = getNodeInstance(processInstanceId);

		LOG.debug(String.format(
				"Setting end time of %s to %s", nodeInstance, nodeInstanceEnd));
		nodeInstance.setNodeInstanceEndTime(nodeInstanceEnd);
		
		this.updateCache(nodeInstance, nodeInstance.getLoopCount());
	}
	
	public void setNodeInstanceStartTime(String processInstanceId, Date nodeInstanceStart) {
		FlowNodeInstance nodeInstance = getNodeInstance(processInstanceId);

		LOG.debug(String.format(
				"Setting start time of %s to %s", nodeInstance, nodeInstanceStart));
		nodeInstance.setNodeInstanceStartTime(nodeInstanceStart);
		
		this.updateCache(nodeInstance, nodeInstance.getLoopCount());
	}

	/**
	 * Shortcut to set "passed state" for node.
	 * 
	 * @param processInstanceId
	 */
	public void setPassed(String processInstanceId) {
		this.setState(processInstanceId, NodeInstaceStates.PASSED_STATE);
	}

	/**
	 * Sets the sequence flows fired.
	 * 
	 * @param flowsFired
	 *            the new sequence flows fired
	 */
	public void setSequenceFlowsFired(String processInstanceId, int flowsFired) {
		FlowNodeInstance nodeInstance = getNodeInstance(processInstanceId);

		LOG.debug(String.format(
				"Setting sequenceFlowsFired of %s from %s to %s", nodeInstance,
				nodeInstance.getFlowsFired(), flowsFired));
		nodeInstance.setFlowsFired(flowsFired);
		this.updateCache(nodeInstance, nodeInstance.getLoopCount());
	}

	/**
	 * Sets the state of a flow node instance.
	 * 
	 * @param state
	 *            the new state
	 */
	public void setState(String processInstanceId, String state) {
		FlowNodeInstance nodeInstance = getNodeInstance(processInstanceId);

		if (stateTransistionSanityCheck(state)) {
			LOG.debug(String.format(
					"Setting state of graphId: %s from %s to %s (uniqueFlowNodeId:%s)",
					nodeInstance.getGraphId(),
					nodeInstance.getNodeInstanceState(), state,
					this.getUniqueFlowNodeId()));
			nodeInstance.setNodeInstanceState(state);
			
			this.updateCache(nodeInstance, nodeInstance.getLoopCount());
		}
	}

	/**
	 * Sets the unique flow node id.
	 *
	 * @param uniqueFlowNodeId the new unique flow node id
	 */
	public void setUniqueFlowNodeId(String uniqueFlowNodeId) {
		this.uniqueFlowNodeId = uniqueFlowNodeId;
	}

	/**
	 * Sets the unique process id.
	 *
	 * @param uniqueProcessId the new unique process id
	 */
	public void setUniqueProcessId(String uniqueProcessId) {
		this.uniqueProcessId = uniqueProcessId;
	}

	/**
	 * Plausibility check of the transition from one state to another.
	 * 
	 * @param newState
	 *            the new state to be set
	 * @return true, if transition is valid, false if state should not be changed
	 */
	private boolean stateTransistionSanityCheck(String newState) {
		if (this.nodeInstance.getNodeInstanceState().equals(newState)) {
			LOG.warn(String
					.format("Cannot set node instance state. State of %s (%s) is already at %s",
							this.nodeInstance.getGraphId(),
							neo4jTemplate.fetch(this.nodeInstance.getFlowNode()).getUniqueFlowNodeId(),
							this.nodeInstance.getNodeInstanceState()));
			return false;

			// transition from INACTIVE_STATE to newState 
		} else if (this.nodeInstance.getNodeInstanceState().equals(
				NodeInstaceStates.INACTIVE_STATE)) {
			if (newState.equals(NodeInstaceStates.ACTIVE_STATE)) {
				return true;
			} else if (newState.equals(NodeInstaceStates.PASSED_STATE)) {
				return true;
			} else if (newState.equals(NodeInstaceStates.DEACTIVATED_STATE)) {
				LOG.debug(String
						.format("State of graphId %s (%s) was at %s and should be set to %s. Ignoring state change (this is expected behavior).",
								this.nodeInstance.getGraphId(),
								neo4jTemplate.fetch(this.nodeInstance.getFlowNode()).getUniqueFlowNodeId(),
								this.nodeInstance.getNodeInstanceState(),
								newState));
				return false;
			} else {
				createSanityCheckFailedLog(newState);
				return false;
			}

			// transition from ACTIVE_STATE to newState
		} else if (this.nodeInstance.getNodeInstanceState().equals(
				NodeInstaceStates.ACTIVE_STATE)) {
			if (newState.equals(NodeInstaceStates.PASSED_STATE)) {
				return true;
			} else if (newState.equals(NodeInstaceStates.DEACTIVATED_STATE)) {
				return true;
			} else {
				createSanityCheckFailedLog(newState);
				return false;
			}

			// transition from PASSED_STATE to newState
		} else if (this.nodeInstance.getNodeInstanceState().equals(
				NodeInstaceStates.PASSED_STATE)) {
			// loop transition
			if (newState.equals(NodeInstaceStates.INACTIVE_STATE)) {
				return true;
			} else {
				createSanityCheckFailedLog(newState);
				return false;
			}

			// transition from DEACTIVATED_STATE to newState
		} else if (this.nodeInstance.getNodeInstanceState().equals(
				NodeInstaceStates.DEACTIVATED_STATE)) {
			createSanityCheckFailedLog(newState);
			return false;

			// unhandled transitions
		} else {
			createSanityCheckFailedLog(newState);
			return false;
		}

	}
	
	/**
	 * Update the cached node instance.
	 *
	 * @param flowNodeInstance the flow node instance
	 * @param loopCount the loop count
	 */
	private void updateCache(FlowNodeInstance flowNodeInstance, int loopCount) {
		this.nodeInstance = flowNodeInstance;
		this.loopCount = loopCount;
	}
	
}
