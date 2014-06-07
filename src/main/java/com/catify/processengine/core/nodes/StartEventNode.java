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
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import akka.actor.ActorRef;

import com.catify.processengine.core.spi.DataObjectHandling;
import com.catify.processengine.core.data.model.NodeInstaceStates;
import com.catify.processengine.core.messages.ActivationMessage;
import com.catify.processengine.core.messages.DeactivationMessage;
import com.catify.processengine.core.messages.TriggerMessage;
import com.catify.processengine.core.nodes.eventdefinition.EventDefinitionParameter;
import com.catify.processengine.core.services.ProcessInstanceMediatorService;

/**
 * Start event nodes instantiate a new process instance and trigger following
 * nodes. There can be multiple start event nodes in a single process. <br>
 * Note that in bpmn start nodes are catching events. This means, if there has
 * been no action on an process instance it has no representation (it does not
 * exist). <br>
 * Therefore start events that should create a new process instance must receive
 * a trigger message (without former receiving of an activation message). This
 * is different to all other nodes, which have to be activated first in order to
 * react to messages. <br>
 * Activation messages should therefore be only send to already instantiated
 * start nodes (eg. to trigger underlying event definitions).
 * 
 * @author christopher köster
 * 
 */
@Configurable
public class StartEventNode extends CatchEvent {

	static final Logger LOG = LoggerFactory.getLogger(StartEventNode.class);
	
	@Autowired
	private ProcessInstanceMediatorService processInstanceMediatorService;

	/** The other start nodes in the same process. */
	private List<ActorRef> otherStartNodes;
	
	/** The uniqueFlowNodeId of the parent sub process node of this start event (if any). */
	private String parentsUniqueFlowNodeId;

	/**
	 * Instantiates a new start event node.
	 * 
	 * @param uniqueProcessId
	 *            the process id
	 * @param uniqueFlowNodeId
	 *            the unique flow node id
	 * @param eventDefinitionParameter
	 *            the event definition
	 * @param outgoingNodes
	 *            the outgoing nodes
	 * @param processInstanceMediatorService
	 *            the process instance service
	 * @param nodeInstanceMediatorService
	 *            the node instance service
	 */
	public StartEventNode(String uniqueProcessId, String uniqueFlowNodeId,
			EventDefinitionParameter eventDefinitionParameter, List<ActorRef> outgoingNodes,
			List<ActorRef> otherStartNodes, String parentsUniqueFlowNodeId,
			DataObjectHandling dataObjectHandling) {
		super(uniqueProcessId, uniqueFlowNodeId);
		this.setOutgoingNodes(outgoingNodes);
		this.setOtherStartNodes(otherStartNodes);
		this.setParentsUniqueFlowNodeId(parentsUniqueFlowNodeId);
		this.setDataObjectHandling(dataObjectHandling);
		
		// create EventDefinition actor
		this.eventDefinitionActor = this.createEventDefinitionActor(eventDefinitionParameter);
	}
	
	

	@Override
	protected void activate(ActivationMessage message) {
		
		String instanceId = message.getProcessInstanceId();
		
		if (instanceId != null && this.getNodeInstanceMediatorService().getNodeInstanceState(instanceId).equals(NodeInstaceStates.PASSED_STATE)) {
			LOG.debug("Start Event received Activiation Message. Assuming loop.");
			this.sendMessageToNodeActor(new TriggerMessage(instanceId, null), this.getSelf());
		} else {
			LOG.debug("Inactive Start Event received Activiation Message. Assuming parallel run.");
			this.getNodeInstanceMediatorService().setState(
					instanceId, NodeInstaceStates.PASSED_STATE);
			
			this.getNodeInstanceMediatorService().setNodeInstanceStartTime(instanceId, new Date());
			
			this.getNodeInstanceMediatorService().persistChanges();
			
			this.callEventDefinitionActor(message);
			
			this.sendMessageToNodeActors(new ActivationMessage(instanceId),
					this.getOutgoingNodes());
		}
	}

	@Override
	protected void deactivate(DeactivationMessage message) {
		this.callEventDefinitionActor(message);

		this.getNodeInstanceMediatorService().setNodeInstanceEndTime(message.getProcessInstanceId(), new Date());
		
		this.getNodeInstanceMediatorService().setState(
				message.getProcessInstanceId(),
				NodeInstaceStates.DEACTIVATED_STATE);
		
		this.getNodeInstanceMediatorService().persistChanges();
	}

	@Override
	protected void trigger(TriggerMessage message) {
		String processInstanceId;

		if (message.getProcessInstanceId() != null) {
			processInstanceId = message.getProcessInstanceId();
			LOG.debug("Process instance starting with given processInstanceId: " + message.getProcessInstanceId());
		} else {
			// get a unique id as process instance id
			processInstanceId = UUID.randomUUID().toString();
			LOG.debug("Process instance starting with new randomUUID processInstanceId: " + processInstanceId);
		}
		
		// create a top level process instance
		if (parentsUniqueFlowNodeId == null) {
			processInstanceMediatorService.createProcessInstance(this.getUniqueProcessId(), processInstanceId);
		} 
		// or create a sub process instance on the current level
		else {
			processInstanceMediatorService.createSubProcessInstance(this.getUniqueProcessId(), this.getParentsUniqueFlowNodeId(), processInstanceId);
		}

		// the default activation of other top level start nodes has been deactivated (see redmine #109)
		// other start nodes are activated by default because they are catching events and their included event definitions need to be triggered
		// this.sendMessageToNodeActors(new ActivationMessage(processInstanceId), this.getOtherStartNodes());
		
		// set the time only, if it has not been done already (will be null if start event has been triggered with 
		// an ActivationMessage before, eg. through a timer service, related to remine #109)
		if (this.getNodeInstanceMediatorService().getNodeInstanceStartTime(processInstanceId) == null) {
			this.getNodeInstanceMediatorService().setNodeInstanceStartTime(processInstanceId, new Date());
		}
		
		this.getDataObjectHandling().saveObject(this.getUniqueProcessId(),
				processInstanceId, message.getPayload());

		this.callEventDefinitionActor(message);

		this.getNodeInstanceMediatorService().setNodeInstanceEndTime(processInstanceId, new Date());
		
		this.getNodeInstanceMediatorService().setState(processInstanceId,
				NodeInstaceStates.PASSED_STATE);
		
		this.getNodeInstanceMediatorService().persistChanges();
		
		this.sendMessageToNodeActors(new ActivationMessage(processInstanceId),
				this.getOutgoingNodes());
	}

	public List<ActorRef> getOtherStartNodes() {
		return otherStartNodes;
	}

	public final void setOtherStartNodes(List<ActorRef> otherStartNodes) {
		this.otherStartNodes = otherStartNodes;
	}

	public String getParentsUniqueFlowNodeId() {
		return parentsUniqueFlowNodeId;
	}

	public void setParentsUniqueFlowNodeId(String parentUniqueFlowNodeId) {
		this.parentsUniqueFlowNodeId = parentUniqueFlowNodeId;
	}
}
