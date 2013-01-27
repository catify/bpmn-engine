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
package com.catify.processengine.core.nodes;

import java.util.Date;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

import akka.actor.ActorRef;

import com.catify.processengine.core.data.dataobjects.DataObjectService;
import com.catify.processengine.core.data.model.NodeInstaceStates;
import com.catify.processengine.core.data.model.entities.FlowNodeInstance;
import com.catify.processengine.core.messages.ActivationMessage;
import com.catify.processengine.core.messages.ArchiveMessage;
import com.catify.processengine.core.messages.DeactivationMessage;
import com.catify.processengine.core.messages.DeletionMessage;
import com.catify.processengine.core.messages.TriggerMessage;
import com.catify.processengine.core.services.NodeInstanceMediatorService;
import com.catify.processengine.core.services.ProcessInstanceMediatorService;

/**
 * End point for a process. Ends a process instance if there are no active nodes
 * left. Has no outgoing flows. There can be multiple end event nodes in a
 * single process.
 * 
 * @author chris
 * 
 */
@Configurable
public class EndEventNode extends ThrowEvent {
	
	@Autowired
	private ProcessInstanceMediatorService processInstanceMediatorService;
	
	// FIXME: implement catify extension to set this value
	private boolean processInstanceArchiving = true;
	
	/** The sub process node that is parent of this end node (if any). */
	private ActorRef parentSubProcessNode;
	
	/** The data object ids of the whole process. Will be null if this is not a top level end event. */
	private Set<String> dataObjectIds;
	
	/** The process instance cleansing actor to either delete the process instance or archive it. */
	@Value("${core.processInstanceCleansingActor}")
	private String processInstanceCleansingActorName;
	private ActorRef processInstanceCleansingActor;

	public EndEventNode() {
	}

	/**
	 * Instantiates a new end event node.
	 *
	 * @param uniqueProcessId the process id
	 * @param uniqueFlowNodeId the unique flow node id
	 * @param eventDefinition the event definition
	 * @param parentSubProcessNode the sub process that is parent of this node (if any)
	 * @param dataObjectHandling the data object handling
	 * @param dataObjectIds the data object ids
	 */
	public EndEventNode(String uniqueProcessId, String uniqueFlowNodeId,
			ActorRef eventDefinition, ActorRef parentSubProcessNode, DataObjectService dataObjectHandling, Set<String> dataObjectIds) {
		this.setUniqueProcessId(uniqueProcessId);
		this.setUniqueFlowNodeId(uniqueFlowNodeId);
		this.setEventDefinition(eventDefinition);
		this.parentSubProcessNode = parentSubProcessNode;
		this.setNodeInstanceMediatorService(new NodeInstanceMediatorService(
				uniqueProcessId, uniqueFlowNodeId));
		this.setDataObjectHandling(dataObjectHandling);
		this.dataObjectIds = dataObjectIds;
	}
	
	/**
	 * Inits the annotated processInstanceCleansingActor after construction, because the @Value annotated fields get filled by spring <b>after</b> construction.
	 */
	@PostConstruct
	void initAnnotations() {
		this.processInstanceCleansingActor = this.getActorSystem().actorFor("user/" + processInstanceCleansingActorName);
	}

	@Override
	protected void activate(ActivationMessage message) {
		
		this.getNodeInstanceMediatorService().setNodeInstanceStartTime(message.getProcessInstanceId(), new Date());
		
		message.setPayload(this.getDataObjectService().loadObject(this.getUniqueProcessId(), message.getProcessInstanceId()));
		
		this.createAndCallEventDefinition(message);
		
		this.getNodeInstanceMediatorService().setNodeInstanceEndTime(message.getProcessInstanceId(), new Date());
		
		this.getNodeInstanceMediatorService().setState(
				message.getProcessInstanceId(), NodeInstaceStates.PASSED_STATE);
		
		this.getNodeInstanceMediatorService().persistChanges();
		
		this.endProcessInstance(message.getProcessInstanceId());
	}

	@Override
	protected void deactivate(DeactivationMessage message) {
		this.createAndCallEventDefinition(message);
		
		this.getNodeInstanceMediatorService().setNodeInstanceEndTime(message.getProcessInstanceId(), new Date());
		
		this.getNodeInstanceMediatorService().setState(
				message.getProcessInstanceId(),
				NodeInstaceStates.DEACTIVATED_STATE);
		
		this.getNodeInstanceMediatorService().persistChanges();
	}

	@Override
	protected void trigger(TriggerMessage message) {
		LOG.warn(String.format("Reaction to %s not implemented in %s. Please check your process.", message.getClass().getSimpleName(), this.getSelf()));
	}

	/**
	 * 
	 * End a process instance, based on a search over all flow node instances of
	 * the process. In bpmn a process instance can only end, if there are no
	 * active nodes left in that instance.
	 * 
	 * @param message
	 *            the message received
	 * @return true if there are no active nodes left in the process instance,
	 *         false if there are.
	 */
	public boolean endProcessInstance(String processInstanceId) {
		// check if there are node instances that have an active state
		Set<FlowNodeInstance> activeFlowNodeInstances = processInstanceMediatorService.findActiveFlowNodeInstances(getUniqueFlowNodeId(), processInstanceId);

		// if the process instance has no active nodes left, consider it ended
		if (activeFlowNodeInstances.size() == 0) {
			
			// embedded end events call their embedding sub processes to move on in the process
			if (this.parentSubProcessNode != null) {
				this.sendMessageToNodeActor(new TriggerMessage(processInstanceId, null), this.parentSubProcessNode);
			
			// top level end events will work with the whole process instance to either save or delete it
			} else {
				if (isProcessInstanceArchiving()) {
					this.sendMessageToNodeActor(new ArchiveMessage(this.getUniqueProcessId(), processInstanceId, new Date()),
							this.processInstanceCleansingActor);
				} else {
					this.sendMessageToNodeActor(new DeletionMessage(this.getUniqueProcessId(), processInstanceId, this.dataObjectIds),
							this.processInstanceCleansingActor);
				} 
				LOG.debug(String.format("Process instance with instance id '%s' ended sucessfully", processInstanceId));
			}
			// return instance has ended
			return true;
		} else {
			// return instance is still active
			return false;
		}
	}

	public boolean isProcessInstanceArchiving() {
		return processInstanceArchiving;
	}

	public void setProcessInstanceArchiving(boolean processInstanceArchiving) {
		this.processInstanceArchiving = processInstanceArchiving;
	}

	public ActorRef getSubProcessNode() {
		return parentSubProcessNode;
	}

	public void setSubProcessNode(ActorRef subProcessNode) {
		this.parentSubProcessNode = subProcessNode;
	}
	
	protected Set<String> getDataObjectIds() {
		return dataObjectIds;
	}

	protected void setDataObjectIds(Set<String> dataObjectIds) {
		this.dataObjectIds = dataObjectIds;
	}

}
