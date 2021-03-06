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
package com.catify.processengine.core.services;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import com.catify.processengine.core.integration.IntegrationMessage;
import com.catify.processengine.core.integration.MessageIntegrationSPI;
import com.catify.processengine.core.messages.MetaDataMessage;
import com.catify.processengine.core.messages.TriggerMessage;
import com.catify.processengine.core.nodes.eventdefinition.EventDefinition;

/**
 * The MessageDispatcherService dispatches messages between the nodes of the
 * process engine and the {@link MessageIntegrationSPI} implementation.
 * 
 * @author christopher köster
 * 
 */
@Configurable
public class MessageDispatcherService {

	static final Logger LOG = LoggerFactory.getLogger(MessageDispatcherService.class);

	/**
	 * Holds the message integration implementation.
	 * 
	 * @see EventDefinition
	 */
	private MessageIntegrationSPI integrationSPI;
	
	@Autowired
	private ActorSystem actorSystem;
	
	/** The meta data actor that asynchronously writes meta data to a process instance. */
	@Value("${core.metaDataActor}")
	private String metaDataActorName;
	private ActorRef metaDataActor;

	/** The unique flow node id to actor ref map. */
	public static Map<String, String> uniqueFlowNodeIdToActorRefMap = new HashMap<String, String>();

	public MessageDispatcherService(MessageIntegrationSPI integrationSPI) {
		this.integrationSPI = integrationSPI;
	}
	
	/**
	 * Inits the annotated processInstanceCleansingActor after construction, because the @Value annotated fields get filled by spring <b>after</b> construction.
	 */
	@PostConstruct
	void initAnnotations() {
		this.metaDataActor = this.actorSystem.actorFor("user/" + metaDataActorName);
	}
	
	/**
	 * Dispatch messages from message integration to the engine.
	 * 
	 * @param integrationMessage
	 *            the message that should be dispatched to the engine
	 * @param metaData
	 *            the meta Data map that holds meta data names and their 
	 *            values returned by their according xpath query
	 */
	public void dispatchToEngine(IntegrationMessage integrationMessage, Map<String, Object> metaData) {

		// get the actor to send the integration message to
		String targetNodeActorString = uniqueFlowNodeIdToActorRefMap
				.get(integrationMessage.getUniqueFlowNodeId());
		ActorRef targetNodeActor = this.actorSystem
				.actorFor("user/" + targetNodeActorString);
		
		// create the integration message
		TriggerMessage triggerMessage = new TriggerMessage(
				integrationMessage.getProcessInstanceId(),
				integrationMessage.getPayload());

		LOG.debug("Message Dispatcher sending trigger message to " + targetNodeActor);
		
		// send the integration message to the actor
		targetNodeActor.tell(triggerMessage, null);
		
		// send the meta data to the meta data actor (if it is not a start event, which can not collect meta data)
		if (integrationMessage.getProcessInstanceId() != null) {
			MetaDataMessage metaDataMessage = new MetaDataMessage(integrationMessage.getProcessId(), integrationMessage.getProcessInstanceId(), metaData);
			
			LOG.debug("Message Dispatcher sending meta data message to " + this.metaDataActor);
			
			metaDataActor.tell(metaDataMessage, null);
		}
	}

	/**
	 * Dispatch messages from the engine via the integration spi implementation.
	 * 
	 * @param integrationPrefix
	 *            the integration prefix
	 * @param uniqueFlowNodeId
	 *            the unique flow node id
	 * @param message
	 *            the message that should be dispatched via the spi
	 *            implementation
	 */
	public void dispatchViaIntegrationSPI(final String uniqueFlowNodeId, final IntegrationMessage message) {
		integrationSPI.send(message);
	}

	/**
	 * Dispatch messages from the engine via the integration spi implementation and return the response.
	 * 
	 * @param integrationPrefix
	 *            the integration prefix
	 * @param uniqueFlowNodeId
	 *            the unique flow node id
	 * @param message
	 *            the message that should be dispatched via the spi
	 *            implementation
	 */
	public Object requestReplyViaIntegrationSPI(final String uniqueFlowNodeId, final IntegrationMessage message) {
		return integrationSPI.requestReply(message);
	}
}
