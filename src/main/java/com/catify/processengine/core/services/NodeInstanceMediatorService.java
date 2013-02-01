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
import org.springframework.transaction.annotation.Transactional;

import com.catify.processengine.core.data.model.NodeInstaceStates;
import com.catify.processengine.core.data.model.entities.FlowNodeInstance;
import com.catify.processengine.core.data.services.FlowNodeInstanceRepositoryService;

/**
 * The NodeInstanceMediatorService handles all data access between the node
 * services ({@link com.catify.processengine.core.nodes}) and their database
 * representation ({@link com.catify.processengine.core.data.model.entities}).
 * The NodeInstanceMediatorService can only access the database node that has
 * been assigned to it at process start. For process wide access responsibility
 * use {@link ProcessInstanceMediatorService}.
 * 
 * @author christopher köster
 * 
 */
@Configurable
public class NodeInstanceMediatorService {

	static final Logger LOG = LoggerFactory
			.getLogger(NodeInstanceMediatorService.class);

	/** The flow node instance is cached to reduce the need for querying and act as a local cache. */
	private FlowNodeInstance nodeInstance;

	/** The unique process id. */
	private String uniqueProcessId;
	
	/** The unique flow node id. */
	private String uniqueFlowNodeId;

	@Autowired
	private FlowNodeInstanceRepositoryService flowNodeInstanceRepositoryService;

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

	/*
	 * ##### begin of neo4j specific methods ##### (this should be an
	 * implementation of an interface. see redmine #58)
	 */

	/**
	 * Persist any changes to the flow node instance to the db.
	 */
	@Transactional //FIXME: need to test if transaction is needed
	public void persistChanges() {
		flowNodeInstanceRepositoryService.save(this.nodeInstance);
	}

	/**
	 * Load a flow node instance from db.
	 * 
	 * @param uniqueProcessId
	 *            the unique process id
	 * @param uniqueFlowNodeId
	 *            the unique flow node id
	 * @param instanceId
	 *            the instance id
	 * @return the neo4j flow node instance
	 */
	private synchronized FlowNodeInstance loadFlowNodeInstance(String uniqueProcessId,
			String uniqueFlowNodeId, String processInstanceId) {
		// check if nodeInstance has already been loaded (eg. by another
		// operation) to avoid unnecessary db lookups
		if (this.nodeInstance == null
				|| !this.nodeInstance.getHasInstanceRelationship()
						.getInstanceId().equals(processInstanceId)) {
			
			FlowNodeInstance flowNodeInstance = flowNodeInstanceRepositoryService.findFlowNodeInstance(
					uniqueProcessId, uniqueFlowNodeId, processInstanceId);
			
			LOG.debug(String.format(
					"Searching FlowNodeInstance in db with parameters: %s, %s, %s. Found: %s",
					uniqueProcessId, uniqueFlowNodeId, processInstanceId,
					flowNodeInstance));
			
			return flowNodeInstance;
		} else {
			return this.nodeInstance;
		}
	}

	/*
	 * ##### end of neo4j specific methods #####
	 */

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

	/**
	 * Gets the state of a flow node instance.
	 * 
	 * @param instanceId
	 *            the instance id
	 * @return the state
	 */
	public String getNodeInstanceState(String processInstanceId) {
		this.nodeInstance = getNodeInstance(processInstanceId);

		if (this.nodeInstance == null) {
			return null;
		} else {
			return this.nodeInstance.getNodeInstanceState();
		}
	}

	/**
	 * Gets the fired flows needed.
	 * 
	 * @param instanceId
	 *            the instance id
	 * @return the fired flows needed
	 */
	public int getIncomingFiredFlowsNeeded(String processInstanceId) {
		this.nodeInstance = getNodeInstance(processInstanceId);

		return this.nodeInstance.getIncomingFiredFlowsNeeded();
	}

	/**
	 * Gets the sequence flows fired.
	 * 
	 * @param instanceId
	 *            the instance id
	 * @return the sequence flows fired
	 */
	public int getSequenceFlowsFired(String processInstanceId) {
		this.nodeInstance = getNodeInstance(processInstanceId);

		return this.nodeInstance.getFlowsFired();
	}

	/**
	 * Sets the state of a flow node instance.
	 * 
	 * @param state
	 *            the new state
	 */
	public void setState(String processInstanceId, String state) {
		this.nodeInstance = getNodeInstance(processInstanceId);

		if (stateTransistionSanityCheck(state)) {
			LOG.debug(String.format(
					"Setting state of graphId: %s from %s to %s (uniqueFlowNodeId:%s)",
					nodeInstance.getGraphId(),
					nodeInstance.getNodeInstanceState(), state,
					this.getUniqueFlowNodeId()));
			nodeInstance.setNodeInstanceState(state);
		}
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
	 * Shortcut to set "active state" for node.
	 * 
	 * @param processInstanceId
	 */
	public void setActive(String processInstanceId) {
		this.setState(processInstanceId, NodeInstaceStates.ACTIVE_STATE);
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
	 * Shortcut to set "deactivated state" for node.
	 * 
	 * @param processInstanceId
	 */
	public void setDeactivated(String processInstanceId) {
		this.setState(processInstanceId, NodeInstaceStates.DEACTIVATED_STATE);
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
	 * Sets the fired flows needed.
	 * 
	 * @param firedFlowsNeeded
	 *            the new fired flows needed
	 */
	public void setFiredFlowsNeeded(String processInstanceId,
			int firedFlowsNeeded) {
		this.nodeInstance = getNodeInstance(processInstanceId);

		LOG.debug(String.format("Setting firedFlowsNeeded of %s from %s to %s",
				nodeInstance, nodeInstance.getIncomingFiredFlowsNeeded(),
				firedFlowsNeeded));
		this.nodeInstance.setIncomingFiredFlowsNeeded(firedFlowsNeeded);
	}

	/**
	 * Sets the sequence flows fired.
	 * 
	 * @param flowsFired
	 *            the new sequence flows fired
	 */
	public void setSequenceFlowsFired(String processInstanceId, int flowsFired) {
		this.nodeInstance = getNodeInstance(processInstanceId);

		LOG.debug(String.format(
				"Setting sequenceFlowsFired of %s from %s to %s", nodeInstance,
				nodeInstance.getFlowsFired(), flowsFired));
		this.nodeInstance.setFlowsFired(flowsFired);
	}
	
	public void setNodeInstanceStartTime(String processInstanceId, Date nodeInstanceStart) {
		this.nodeInstance = getNodeInstance(processInstanceId);

		LOG.debug(String.format(
				"Setting start time of %s to %s", nodeInstance, nodeInstanceStart));
		this.nodeInstance.setNodeInstanceStartTime(nodeInstanceStart);
	}
	
	public Date getNodeInstanceStartTime(String processInstanceId) {
		this.nodeInstance = getNodeInstance(processInstanceId);

		return this.nodeInstance.getNodeInstanceStartTime();
	}
	
	public void setNodeInstanceEndTime(String processInstanceId, Date nodeInstanceEnd) {
		this.nodeInstance = getNodeInstance(processInstanceId);

		LOG.debug(String.format(
				"Setting end time of %s to %s", nodeInstance, nodeInstanceEnd));
		this.nodeInstance.setNodeInstanceEndTime(nodeInstanceEnd);
	}

	/**
	 * Plausibility check of the transition from one state to another.
	 * 
	 * @param newState
	 *            the new state to be set
	 * @return true, if successful, false if state should not be changed
	 */
	private boolean stateTransistionSanityCheck(String newState) {
		if (this.nodeInstance.getNodeInstanceState().equals(newState)) {
			LOG.info(String
					.format("Cannot set node instance state. State of %s is already at %s",
							this.nodeInstance.getGraphId(),
							this.nodeInstance.getNodeInstanceState()));
			return false;

			// transition from INACTIVE_STATE to newState
		} else if (this.nodeInstance.getNodeInstanceState().equals(
				NodeInstaceStates.INACTIVE_STATE)) {
			if (newState.equals(NodeInstaceStates.ACTIVE_STATE)) {
				return true;
			} else if (newState.equals(NodeInstaceStates.PASSED_STATE)) {
				return true;
			} else {
				LOG.warn(String
						.format("stateTransistionSanityCheck failed. State of %s was at %s and should be set to %s",
								this.nodeInstance.getGraphId(),
								this.nodeInstance.getNodeInstanceState(),
								newState));
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
				LOG.warn(String
						.format("stateTransistionSanityCheck failed. State of %s was at %s and should be set to %s",
								this.nodeInstance.getGraphId(),
								this.nodeInstance.getNodeInstanceState(),
								newState));
				return false;
			}

			// transition from PASSED_STATE to newState
		} else if (this.nodeInstance.getNodeInstanceState().equals(
				NodeInstaceStates.PASSED_STATE)) {
			LOG.warn(String
					.format("stateTransistionSanityCheck failed. State of %s was at %s and should be set to %s",
							this.nodeInstance.getGraphId(),
							this.nodeInstance.getNodeInstanceState(), newState));
			return false;

			// transition from DEACTIVATED_STATE to newState
		} else if (this.nodeInstance.getNodeInstanceState().equals(
				NodeInstaceStates.DEACTIVATED_STATE)) {
			LOG.warn(String
					.format("stateTransistionSanityCheck failed. State of %s was at %s and should be set to %s",
							this.nodeInstance.getGraphId(),
							this.nodeInstance.getNodeInstanceState(), newState));
			return false;

			// unhandled transitions
		} else {
			LOG.warn(String
					.format("stateTransistionSanityCheck failed. State of %s was at %s and should be set to %s",
							this.nodeInstance.getGraphId(),
							this.nodeInstance.getNodeInstanceState(), newState));
			return false;
		}

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
	 * Sets the unique process id.
	 *
	 * @param uniqueProcessId the new unique process id
	 */
	public void setUniqueProcessId(String uniqueProcessId) {
		this.uniqueProcessId = uniqueProcessId;
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
	 * Sets the unique flow node id.
	 *
	 * @param uniqueFlowNodeId the new unique flow node id
	 */
	public void setUniqueFlowNodeId(String uniqueFlowNodeId) {
		this.uniqueFlowNodeId = uniqueFlowNodeId;
	}

	
	/**
	 * Checks if the node instance has been initialized.
	 *
	 * @return true, if is initialized
	 */
	public boolean isInitialized(){
		return (this.nodeInstance != null);
	}
	
}
