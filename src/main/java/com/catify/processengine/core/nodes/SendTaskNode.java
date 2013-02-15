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

import akka.actor.ActorRef;

import com.catify.processengine.core.data.dataobjects.DataObjectHandling;
import com.catify.processengine.core.data.model.NodeInstaceStates;
import com.catify.processengine.core.messages.ActivationMessage;
import com.catify.processengine.core.messages.DeactivationMessage;
import com.catify.processengine.core.messages.TriggerMessage;
import com.catify.processengine.core.nodes.eventdefinition.EventDefinitionHandling;
import com.catify.processengine.core.nodes.eventdefinition.EventDefinitionParameter;
import com.catify.processengine.core.services.NodeInstanceMediatorService;

/**
 * The SendTaskNode acts like a throw event with a message event definition.
 * 
 * @author christopher köster
 * 
 */
public class SendTaskNode extends Task {

	public SendTaskNode() {

	}

	/**
	 * Instantiates a new send task node.
	 *
	 * @param uniqueProcessId the process id
	 * @param uniqueFlowNodeId the unique flow node id
	 * @param outgoingNodes the outgoing nodes
	 * @param actorRefString the actor ref string
	 * @param eventDefinitionParameter the event definition
	 * @param dataObjectHandling the data object handling
	 */
	public SendTaskNode(String uniqueProcessId, String uniqueFlowNodeId,
			List<ActorRef> outgoingNodes, String actorRefString,
			EventDefinitionParameter eventDefinitionParameter, DataObjectHandling dataObjectHandling, List<ActorRef> boundaryEvent) {
		this.setUniqueProcessId(uniqueProcessId);
		this.setUniqueFlowNodeId(uniqueFlowNodeId);
		this.setOutgoingNodes(outgoingNodes);
		this.setNodeInstanceMediatorService(new NodeInstanceMediatorService(
				uniqueProcessId, uniqueFlowNodeId));
		this.setEventDefinitionParameter(eventDefinitionParameter);
		this.setDataObjectHandling(dataObjectHandling);
		this.setBoundaryEvents(boundaryEvent);
		
		// create EventDefinition actor
		this.eventDefinitionActor = EventDefinitionHandling
				.createEventDefinitionActor(uniqueFlowNodeId, this.getContext(), eventDefinitionParameter);
	}

	@Override
	protected void activate(ActivationMessage message) {
		this.getNodeInstanceMediatorService().setNodeInstanceStartTime(message.getProcessInstanceId(), new Date());
		
		this.callEventDefinitionActor(message);
		
		this.activateBoundaryEvents(message);
		
		this.getNodeInstanceMediatorService().setNodeInstanceEndTime(message.getProcessInstanceId(), new Date());
		
		this.getNodeInstanceMediatorService().setState(
				message.getProcessInstanceId(), NodeInstaceStates.PASSED_STATE);
		
		this.getNodeInstanceMediatorService().persistChanges();
		
		this.deactivateBoundaryEvents(message);
		
		this.sendMessageToNodeActors(
				new ActivationMessage(message.getProcessInstanceId()),
				this.getOutgoingNodes());
	}

	@Override
	protected void deactivate(DeactivationMessage message) {
		this.callEventDefinitionActor(message);
		
		this.deactivateBoundaryEvents(message);
		
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

}
