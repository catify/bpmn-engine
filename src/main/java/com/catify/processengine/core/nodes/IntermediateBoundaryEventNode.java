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
import com.catify.processengine.core.nodes.eventdefinition.EventDefinitionParameter;

/**
 * An intermediate catch event can receive (and save) messages sent from outside
 * of the process engine.
 * 
 * @author christopher köster
 * 
 */
public class IntermediateBoundaryEventNode extends CatchEvent {

	/** The activity a boundary event is connected to. */
	private ActorRef boundaryActivity;
	
	private boolean interrupting;

	/**
	 * Instantiates a new catch event node.
	 * 
	 * @param uniqueProcessId
	 *            the process id
	 * @param uniqueFlowNodeId
	 *            the unique flow node id
	 * @param eventDefinitionActor
	 *            the event definition
	 * @param outgoingNodes
	 *            the outgoing nodes
	 * @param boundaryActivity 
	 * @param nodeInstanceMediatorService
	 *            the node instance service
	 */
	public IntermediateBoundaryEventNode(String uniqueProcessId,
			String uniqueFlowNodeId, EventDefinitionParameter eventDefinitionParameter,
			List<ActorRef> outgoingNodes, DataObjectHandling dataObjectHandling, ActorRef boundaryActivity, boolean interrupting) {
		super(uniqueProcessId, uniqueFlowNodeId);
		this.setOutgoingNodes(outgoingNodes);
		this.setDataObjectHandling(dataObjectHandling);
		this.setBoundaryActivity(boundaryActivity);
		this.setInterrupting(interrupting);
		
		// create EventDefinition actor
		this.eventDefinitionActor = this.createEventDefinitionActor(eventDefinitionParameter);
	}

	@Override
	protected void activate(ActivationMessage message) {
		this.getNodeInstanceMediatorService().setState(
				message.getProcessInstanceId(), NodeInstaceStates.ACTIVE_STATE);
		
		this.getNodeInstanceMediatorService().setNodeInstanceStartTime(message.getProcessInstanceId(), new Date());
		
		this.getNodeInstanceMediatorService().persistChanges();
		
		this.callEventDefinitionActor(message);
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
		this.getDataObjectHandling().saveObject(this.getUniqueProcessId(), message.getProcessInstanceId(), message.getPayload());
		
		this.callEventDefinitionActor(message);
		
		if (isInterrupting()) {
			// deactivate the activity this event is bound to
			this.sendMessageToNodeActor(
				new DeactivationMessage(message.getProcessInstanceId()),
				this.getBoundaryActivity());
		}
		
		this.getNodeInstanceMediatorService().setNodeInstanceEndTime(message.getProcessInstanceId(), new Date());
		
		this.getNodeInstanceMediatorService().setState(
				message.getProcessInstanceId(), NodeInstaceStates.PASSED_STATE);
		
		this.getNodeInstanceMediatorService().persistChanges();

		this.sendMessageToNodeActors(
				new ActivationMessage(message.getProcessInstanceId()),
				this.getOutgoingNodes());
	}

	public ActorRef getBoundaryActivity() {
		return boundaryActivity;
	}

	public void setBoundaryActivity(ActorRef boundaryActivity) {
		this.boundaryActivity = boundaryActivity;
	}

	public boolean isInterrupting() {
		return interrupting;
	}

	public void setInterrupting(boolean interrupting) {
		this.interrupting = interrupting;
	}

}
