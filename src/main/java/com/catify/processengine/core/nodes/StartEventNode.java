/**
 * -------------------------------------------------------
 * Copyright (C) 2013 catify <info@catify.com>
 * -------------------------------------------------------
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import akka.actor.ActorRef;

import com.catify.processengine.core.data.dataobjects.DataObjectService;
import com.catify.processengine.core.data.model.NodeInstaceStates;
import com.catify.processengine.core.messages.ActivationMessage;
import com.catify.processengine.core.messages.DeactivationMessage;
import com.catify.processengine.core.messages.TriggerMessage;
import com.catify.processengine.core.nodes.eventdefinition.EventDefinition;
import com.catify.processengine.core.services.NodeInstanceMediatorService;
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
 * @author chris
 * 
 */
@Configurable
public class StartEventNode extends CatchEvent {

	static final Logger LOG = LoggerFactory.getLogger(StartEventNode.class);
	
	@Autowired
	private ProcessInstanceMediatorService processInstanceMediatorService;

	private List<ActorRef> otherStartNodes;

	public StartEventNode() {
	}

	/**
	 * Instantiates a new start event node.
	 * 
	 * @param uniqueProcessId
	 *            the process id
	 * @param uniqueFlowNodeId
	 *            the unique flow node id
	 * @param eventDefinition
	 *            the event definition
	 * @param outgoingNodes
	 *            the outgoing nodes
	 * @param processInstanceMediatorService
	 *            the process instance service
	 * @param nodeInstanceMediatorService
	 *            the node instance service
	 */
	public StartEventNode(String uniqueProcessId, String uniqueFlowNodeId,
			EventDefinition eventDefinition, List<ActorRef> outgoingNodes,
			List<ActorRef> otherStartNodes,
			DataObjectService dataObjectHandling) {
		this.setUniqueProcessId(uniqueProcessId);
		this.setUniqueFlowNodeId(uniqueFlowNodeId);
		this.setEventDefinition(eventDefinition);
		this.setOutgoingNodes(outgoingNodes);
		this.setNodeInstanceMediatorService(new NodeInstanceMediatorService(
				uniqueProcessId, uniqueFlowNodeId));
		this.setOtherStartNodes(otherStartNodes);
		this.setDataObjectHandling(dataObjectHandling);
	}

	@Override
	protected void activate(ActivationMessage message) {
		this.getNodeInstanceMediatorService().setState(
				message.getProcessInstanceId(), NodeInstaceStates.ACTIVE_STATE);
		
		this.getNodeInstanceMediatorService().setNodeInstanceStartTime(message.getProcessInstanceId(), new Date());
		
		this.getNodeInstanceMediatorService().persistChanges();
		
		eventDefinition.acitivate(message);
	}

	@Override
	protected void deactivate(DeactivationMessage message) {
		eventDefinition.deactivate(message);

		this.getNodeInstanceMediatorService().setNodeInstanceEndTime(message.getProcessInstanceId(), new Date());
		
		this.getNodeInstanceMediatorService().setState(
				message.getProcessInstanceId(),
				NodeInstaceStates.DEACTIVATED_STATE);
		
		this.getNodeInstanceMediatorService().persistChanges();
	}

	@Override
	protected void trigger(TriggerMessage message) {
		String processInstanceId;

		// work with the given process instance id, if the process instance has
		// already been initialized, or create a new process instance
		if (message.getProcessInstanceId() != null) {
			LOG.debug("Already instantiated process instance detected with pid: " + message.getProcessInstanceId());
			processInstanceId = message.getProcessInstanceId();
		} else {
			processInstanceId = processInstanceMediatorService.createProcessInstance(this.getUniqueProcessId());

			// the default activation of other top level start nodes has been deactivated (see redmine #109)
			// other start nodes are activated by default because they are catching events and their included event definitions need to be triggered
			// this.sendMessageToNodeActors(new ActivationMessage(processInstanceId), this.getOtherStartNodes());
		}
		
		// set the time only, if it has not been done already (will be null if start event has been triggered with 
		// an ActivationMessage before, eg. through a timer service, related to remine #109)
		if (this.getNodeInstanceMediatorService().getNodeInstanceStartTime(processInstanceId) == null) {
			this.getNodeInstanceMediatorService().setNodeInstanceStartTime(processInstanceId, new Date());
		}
		
		this.getDataObjectService().saveObject(this.getUniqueProcessId(),
				processInstanceId, message.getPayload());

		eventDefinition.trigger(message);

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

}
