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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import akka.actor.ActorRef;

import com.catify.processengine.core.data.model.NodeInstaceStates;
import com.catify.processengine.core.messages.ActivationMessage;
import com.catify.processengine.core.messages.DeactivationMessage;
import com.catify.processengine.core.messages.Message;
import com.catify.processengine.core.messages.TriggerMessage;
import com.catify.processengine.core.messages.WinningMessage;
import com.catify.processengine.core.services.NodeInstanceMediatorService;

/**
 * An event based gateway triggers immediately if one of the following event
 * nodes fires. Only one direct following event node can have a winning
 * condition, the other ones will be deactivated.
 * 
 * The event nodes following to the event based gateway will be activated with a
 * special parameter, so that they wait for the go from the event based gateway
 * until they proceed in the process.
 * 
 * @author chris
 * 
 */
public class EventBasedGatewayNode extends FlowElement {

	public EventBasedGatewayNode() {
	}

	/**
	 * Instantiates a new event based gateway node.
	 * 
	 * @param uniqueProcessId
	 *            the process id
	 * @param uniqueFlowNodeId
	 *            the unique flow node id
	 * @param outgoingNodes
	 *            the outgoing nodes
	 * @param nodeInstanceMediatorService
	 *            the node instance service
	 */
	public EventBasedGatewayNode(String uniqueProcessId,
			String uniqueFlowNodeId, List<ActorRef> outgoingNodes) {
		this.setUniqueProcessId(uniqueProcessId);
		this.setUniqueFlowNodeId(uniqueFlowNodeId);
		this.setOutgoingNodes(outgoingNodes);
		this.setNodeInstanceMediatorService(new NodeInstanceMediatorService(
				uniqueProcessId, uniqueFlowNodeId));
	}

	@Override
	protected void activate(ActivationMessage message) {
		this.getNodeInstanceMediatorService().setState(
				message.getProcessInstanceId(),
				NodeInstaceStates.ACTIVE_STATE);
		
		this.getNodeInstanceMediatorService().setNodeInstanceStartTime(message.getProcessInstanceId(), new Date());
		
		this.getNodeInstanceMediatorService().persistChanges();
		
		this.sendMessageToNodeActors(
				new ActivationMessage(message.getProcessInstanceId()),
				this.getOutgoingNodes());
	}

	@Override
	protected void deactivate(DeactivationMessage message) {
		this.getNodeInstanceMediatorService().setNodeInstanceEndTime(message.getProcessInstanceId(), new Date());
		
		this.getNodeInstanceMediatorService().setState(
				message.getProcessInstanceId(),
				NodeInstaceStates.DEACTIVATED_STATE);
		
		this.getNodeInstanceMediatorService().persistChanges();
	}

	@Override
	protected void trigger(TriggerMessage message) {
		this.getNodeInstanceMediatorService().setNodeInstanceEndTime(message.getProcessInstanceId(), new Date());
		
		this.getNodeInstanceMediatorService().setState(
				message.getProcessInstanceId(),
				NodeInstaceStates.PASSED_STATE);
		this.deactivateLoosingEventNodes(message);
		
		this.getNodeInstanceMediatorService().persistChanges();
		
		this.sendMessageToNodeActor(
				new WinningMessage(message.getProcessInstanceId()),
				this.getSender());
	}

	/**
	 * Deactivate loosing event nodes.
	 *
	 * @param message the message received
	 */
	public void deactivateLoosingEventNodes(Message message) {
		
		List<ActorRef> loosingNodes = this.getCopyOfOutgoingNodes();
		loosingNodes.remove(this.getSender());
		
		this.sendMessageToNodeActors(
		new DeactivationMessage(message.getProcessInstanceId()),loosingNodes);
	}
	
	/**
	 * Get a copy of the outgoing nodes list.
	 * 
	 * @return the copy of outgoing nodes
	 */
	private List<ActorRef> getCopyOfOutgoingNodes() {
		List<ActorRef> copy = new ArrayList<ActorRef>(this.getOutgoingNodes());
		return copy;
	}

}
