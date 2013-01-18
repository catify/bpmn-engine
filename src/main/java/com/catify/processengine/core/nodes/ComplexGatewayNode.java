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
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import akka.actor.ActorRef;

import com.catify.processengine.core.data.model.NodeInstaceStates;
import com.catify.processengine.core.messages.ActivationMessage;
import com.catify.processengine.core.messages.DeactivationMessage;
import com.catify.processengine.core.messages.Message;
import com.catify.processengine.core.messages.TriggerMessage;
import com.catify.processengine.core.services.ActorReferenceService;
import com.catify.processengine.core.services.NodeInstanceMediatorService;
import com.catify.processengine.core.services.ProcessInstanceMediatorService;

/**
 * A complex gateway triggers on a user defined condition. At this stage of
 * implementation it will cover the 'n of m'-condition: If a user defined number
 * 'n' of the incoming flows 'm' fired, the gateway will fire.
 * 
 * @author chris
 * 
 */
@Configurable
public class ComplexGatewayNode extends FlowElement implements NOfMService {
	
	static final Logger LOG = LoggerFactory.getLogger(ComplexGatewayNode.class);
	
	@Autowired
	private ProcessInstanceMediatorService processInstanceMediatorService;

	public ComplexGatewayNode() {
	}

	/**
	 * Instantiates a new complex gateway node.
	 * 
	 * @param uniqueProcessId
	 *            the process id
	 * @param uniqueFlowNodeId
	 *            the unique flow node id
	 * @param outgoingNodes
	 *            the outgoing nodes
	 * @param nodeInstanceMediatorService
	 *            the node instance service
	 * @param firedFlowsNeeded
	 *            how many incoming flows need to fire until gateway fires
	 */
	public ComplexGatewayNode(String uniqueProcessId, String uniqueFlowNodeId,
			List<ActorRef> outgoingNodes) {
		this.setUniqueProcessId(uniqueProcessId);
		this.setUniqueFlowNodeId(uniqueFlowNodeId);
		this.setOutgoingNodes(outgoingNodes);
		this.setNodeInstanceMediatorService(new NodeInstanceMediatorService(
				uniqueProcessId, uniqueFlowNodeId));
	}

	@Override
	protected void activate(ActivationMessage message) {
		
		String iid = message.getProcessInstanceId();
		
		// if this is the first call, set state to active and set start time
		if (this.getNodeInstanceMediatorService().getSequenceFlowsFired(iid) == 0) {
			this.getNodeInstanceMediatorService().setActive(iid);
			this.getNodeInstanceMediatorService().setNodeInstanceStartTime(iid, new Date());
		}
		
		int flowsFired = this.incrementSequenceFlowsFired(message,
				this.getNodeInstanceMediatorService().getSequenceFlowsFired(iid));
		
		this.getNodeInstanceMediatorService().persistChanges();
		
		// check the n of m condition (encapsulated as a whole to be able to plug in other trigger conditions in a later step)
		// and react only if it is fulfilled
		if (checkNOfMCondition(iid, flowsFired)) {
			this.getNodeInstanceMediatorService().setNodeInstanceEndTime(iid, new Date());

			this.deactivatePreviousLoosingNodes(message);
			
			this.getNodeInstanceMediatorService().setPassed(iid);
			
			this.getNodeInstanceMediatorService().persistChanges();
			
			this.sendMessageToNodeActors(
					new ActivationMessage(message.getProcessInstanceId()),
					this.getOutgoingNodes());
		}

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
		LOG.warn(String.format("Reaction to %s not implemented in %s. Please check your process.", message.getClass().getSimpleName(), this.getSelf()));
	}

	/**
	 * Deactivate previous loosing nodes.
	 *
	 * @param message the message received
	 */
	public void deactivatePreviousLoosingNodes(Message message) {
		
		Set<String> loosingUniqueFlowNodeIds = 
				processInstanceMediatorService.getPreviousLoosingNodeIds(getUniqueProcessId(), getUniqueFlowNodeId(), message.getProcessInstanceId());
		
		for (String loosingUniqueFlowNodeId : loosingUniqueFlowNodeIds) {
			this.sendMessageToNodeActor(
					new DeactivationMessage(message.getProcessInstanceId()),
					new ActorReferenceService().getActorReference(loosingUniqueFlowNodeId));
		}
	}

	@Override
	public boolean checkNOfMCondition(String iid, int flowsFired) {
			return (this.getNodeInstanceMediatorService().getIncomingFiredFlowsNeeded(iid) == flowsFired);
	}

	@Override
	public int incrementSequenceFlowsFired(Message message, int flowsFired) {
		int flowsFiredIncreased = ++flowsFired;
		this.getNodeInstanceMediatorService().setSequenceFlowsFired(
				message.getProcessInstanceId(), flowsFiredIncreased);
		return flowsFiredIncreased;
	}
}
