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
package com.catify.processengine.core.nodes.eventdefinition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.catify.processengine.core.integration.IntegrationMessage;
import com.catify.processengine.core.integration.MessageIntegrationSPI;
import com.catify.processengine.core.messages.ActivationMessage;
import com.catify.processengine.core.messages.DeactivationMessage;
import com.catify.processengine.core.messages.TriggerMessage;
import com.catify.processengine.core.nodes.NodeFactory;
import com.catify.processengine.core.processdefinition.jaxb.TMessageIntegration;
import com.catify.processengine.core.services.MessageDispatcherService;

// TODO: Auto-generated Javadoc
/**
 * Each (akka) node that has a catching message event definition instantiates
 * and binds an object of the MessageEventDefinition_Catch class. This class
 * implements the messaging part of the catching message event node. Each time
 * the node gets a trigger message its trigger method saves the payload to the
 * object defined in the process.xml. For instantiation of this node see {@link NodeFactory}.
 */
public class MessageEventDefinition_InOut implements SynchronousEventDefinition {

	static final Logger LOG = LoggerFactory
			.getLogger(MessageEventDefinition_InOut.class);
	
	private final String uniqueProcessId;
	private final String uniqueFlowNodeId;

	private MessageIntegrationSPI integrationSPI;
	private MessageDispatcherService messageDispatcherService = null;

	/**
	 * Instantiates a new throwing message event definition.
	 * 
	 * @param uniqueProcessId
	 *            the unique process id
	 * @param uniqueFlowNodeId
	 *            the unique flow node id
	 * @param actorRefString
	 *            the actor reference string
	 * @param messageIntegration
	 *            the message event definition
	 */
	public MessageEventDefinition_InOut(String uniqueProcessId, String uniqueFlowNodeId, TMessageIntegration messageIntegration) {

		this.uniqueProcessId = uniqueProcessId;
		this.uniqueFlowNodeId = uniqueFlowNodeId;

		if (messageIntegration != null) {
			this.integrationSPI = MessageIntegrationSPI
					.getMessageIntegrationImpl(messageIntegration.getPrefix());
			this.messageDispatcherService = new MessageDispatcherService(
					this.integrationSPI);
			registerMessageEventDefinition_inOut(messageIntegration);
		}
	}

	@Override
	public Object acitivate(ActivationMessage message) {

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

	@Override
	public void deactivate(DeactivationMessage message) {
		// deactivation is done on process level
	}

	@Override
	public void trigger(TriggerMessage message) {
		// messages are dispatched by MessageDispatcherService (so there is nothing to do)
	}

	/**
	 * Register throwing message event definition.
	 * 
	 * @param messageIntegration
	 *            the jaxb message integration
	 */
	public final void registerMessageEventDefinition_inOut(
			TMessageIntegration messageIntegration) {
		// start the message integration implementation for this flow node (like
		// routes etc.)
		integrationSPI.startRequestReply(
				this.uniqueFlowNodeId,
				messageIntegration.getIntegrationstring());
	}

	public String getUniqueProcessId() {
		return uniqueProcessId;
	}

	public MessageDispatcherService getMessageDispatcherService() {
		return messageDispatcherService;
	}

	public void setMessageDispatcherService(MessageDispatcherService messageDispatcherService) {
		this.messageDispatcherService = messageDispatcherService;
	}
}
