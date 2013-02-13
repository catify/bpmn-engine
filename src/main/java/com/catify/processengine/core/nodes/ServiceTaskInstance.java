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

import com.catify.processengine.core.data.dataobjects.DataObjectService;
import com.catify.processengine.core.data.model.NodeInstaceStates;
import com.catify.processengine.core.integration.IntegrationMessage;
import com.catify.processengine.core.integration.MessageIntegrationSPI;
import com.catify.processengine.core.messages.ActivationMessage;
import com.catify.processengine.core.messages.DeactivationMessage;
import com.catify.processengine.core.messages.TriggerMessage;
import com.catify.processengine.core.processdefinition.jaxb.TMessageIntegration;
import com.catify.processengine.core.services.MessageDispatcherService;
import com.catify.processengine.core.services.NodeInstanceMediatorService;

/**
 * The ServiceTaskInstance is a synchronous node. It will load a value from a data object (if specified), 
 * issue a request to the provided message integration and stay active until it gets a reply (or runs in a timeout specified 
 * in the message integration implementation). The reply received is then saved to a data object (if specified).
 * 
 * @author christopher köster
 * 
 */
public class ServiceTaskInstance extends Task {
	
	private MessageIntegrationSPI integrationSPI;
	private MessageDispatcherService messageDispatcherService = null;
	
	public ServiceTaskInstance() {

	}

	/**
	 * Instantiates a new service task node.
	 * 
	 * @param uniqueProcessId
	 *            the process id
	 * @param uniqueFlowNodeId
	 *            the unique flow node id
	 * @param outgoingNodes
	 *            the outgoing nodes
	 * @param actorRef 
	 * @param nodeInstanceMediatorService
	 *            the node instance service
	 */
	public ServiceTaskInstance(String uniqueProcessId, String uniqueFlowNodeId,
			List<ActorRef> outgoingNodes,
			TMessageIntegration messageIntegrationInOut, DataObjectService dataObjectHandling, List<ActorRef> boundaryEvent) {
		this.setUniqueProcessId(uniqueProcessId);
		this.setUniqueFlowNodeId(uniqueFlowNodeId);
		this.setOutgoingNodes(outgoingNodes);
		this.setNodeInstanceMediatorService(new NodeInstanceMediatorService(
				uniqueProcessId, uniqueFlowNodeId));
		this.setDataObjectHandling(dataObjectHandling);
		this.setBoundaryEvents(boundaryEvent);
		
		// messages are handled by the integrationSpi
		if (messageIntegrationInOut != null) {
			this.integrationSPI = MessageIntegrationSPI
					.getMessageIntegrationImpl(messageIntegrationInOut.getPrefix());
			this.messageDispatcherService = new MessageDispatcherService(
					this.integrationSPI);
			registerRequestReply(messageIntegrationInOut);
		}
	}
	
	@Override
	protected void activate(ActivationMessage message) {
		this.getNodeInstanceMediatorService().setState(
				message.getProcessInstanceId(), NodeInstaceStates.ACTIVE_STATE);
		
		this.getNodeInstanceMediatorService().setNodeInstanceStartTime(message.getProcessInstanceId(), new Date());
		
		message.setPayload(this.getDataObjectService().loadObject(this.getUniqueProcessId(), message.getProcessInstanceId()));
		
		Object repliedDataObject = this.requestReply(message);
		
		this.getDataObjectService().saveObject(this.getUniqueProcessId(), message.getProcessInstanceId(), repliedDataObject);
		
		this.getNodeInstanceMediatorService().setNodeInstanceEndTime(message.getProcessInstanceId(), new Date());
		
		this.getNodeInstanceMediatorService().setState(
				message.getProcessInstanceId(), NodeInstaceStates.PASSED_STATE);
		
		this.getNodeInstanceMediatorService().persistChanges();
		
		this.deactivateBoundaryEvents(message);
		
		this.sendMessageToNodeActors(
				new ActivationMessage(message.getProcessInstanceId()),
				this.getOutgoingNodes());
		
		// stop this instance node
		this.getContext().stop(this.getSelf());
	}
	
	/**
	 * Request reply operation via the integration spi.
	 *
	 * @param message the message to send
	 * @return the object returned by the spi
	 */
	private Object requestReply(ActivationMessage message) {
		// get the data from the data store that is associated with this node
		Object data;
		if (message.getPayload() != null) {
			data = message.getPayload();
		} else {
			data = "no payload";
		}
		
		// create an IntegrationMessage to be send to the message dispatcher
		IntegrationMessage integrationMessage = new IntegrationMessage(
				this.uniqueProcessId, this.uniqueFlowNodeId,
						message.getProcessInstanceId(), data);
		
		// dispatch that message via the integration spi and return the response
		return messageDispatcherService.requestReplyViaIntegrationSPI(
				this.uniqueFlowNodeId, integrationMessage);
	}
	
	/**
	 * Register throwing message event definition.
	 * 
	 * @param messageIntegration
	 *            the jaxb message integration
	 */
	public final void registerRequestReply(
			TMessageIntegration messageIntegration) {
		// start the message integration implementation for this flow node (like
		// routes etc.)
		integrationSPI.startRequestReply(
				this.uniqueFlowNodeId,
				messageIntegration.getIntegrationstring());
	}

	@Override
	protected void deactivate(DeactivationMessage message) {
		LOG.warn(String.format("Reaction to %s not implemented in %s. Please check your process.", message.getClass().getSimpleName(), this.getSelf()));
	}

	@Override
	protected void trigger(TriggerMessage message) {
		LOG.warn(String.format("Reaction to %s not implemented in %s. Please check your process.", message.getClass().getSimpleName(), this.getSelf()));
	}
}
