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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;

import com.catify.processengine.core.messages.ActivationMessage;
import com.catify.processengine.core.messages.DeactivationMessage;
import com.catify.processengine.core.messages.Message;
import com.catify.processengine.core.messages.TriggerMessage;
import com.catify.processengine.core.services.NodeInstanceMediatorService;
import com.catify.processengine.core.util.GatewayUtil;

/**
 * The ParallelGatewayNode triggers when all incoming flow nodes have triggered.
 * 
 * @author christopher köster
 * 
 */
public class ParallelGatewayNode extends FlowElement implements NOfMService {

	static final Logger LOG = LoggerFactory.getLogger(ParallelGatewayNode.class);

	public ParallelGatewayNode() {
	}

	/**
	 * Instantiates a new parallel gateway node.
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
	public ParallelGatewayNode(String uniqueProcessId, String uniqueFlowNodeId,
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
		if (this.getNodeInstanceMediatorService().getSequenceFlowsFired(message.getProcessInstanceId()) == 0) {
			this.getNodeInstanceMediatorService().setActive(iid);
			this.getNodeInstanceMediatorService().setNodeInstanceStartTime(message.getProcessInstanceId(), new Date());
		}
		
		int flowsFired = GatewayUtil.setFiredPlusOne(getNodeInstanceMediatorService(), iid);
		
		this.getNodeInstanceMediatorService().persistChanges();
		
		// check the n of m condition and react only if it is fulfilled
		if (checkNOfMCondition(iid, flowsFired)) {
			this.getNodeInstanceMediatorService().setNodeInstanceEndTime(iid, new Date());
			this.getNodeInstanceMediatorService().setPassed(iid);
			
			this.getNodeInstanceMediatorService().persistChanges();
			
			this.sendMessageToNodeActors(
					new ActivationMessage(iid),
					this.getOutgoingNodes());
		}

	}

	@Override
	protected void deactivate(DeactivationMessage message) {
		
		String iid = message.getProcessInstanceId();
		
		this.getNodeInstanceMediatorService().setNodeInstanceEndTime(iid, new Date());
		this.getNodeInstanceMediatorService().setDeactivated(iid);
		this.getNodeInstanceMediatorService().persistChanges();
	}

	@Override
	protected void trigger(TriggerMessage message) {
		LOG.warn(String.format("Reaction to %s not implemented in %s. Please check your process.", message.getClass().getSimpleName(), this.getSelf()));
	}

	@Override
	public boolean checkNOfMCondition(String iid, int flowsFired) {
		return (this.getNodeInstanceMediatorService().getIncomingFiredFlowsNeeded(iid) == flowsFired);
	}

	/**
	 * TODO --> refactor this
	 */
	@Override
	public int incrementSequenceFlowsFired(Message message, int flowsFired) {
		// TODO Auto-generated method stub
		return 0;
	}

}
