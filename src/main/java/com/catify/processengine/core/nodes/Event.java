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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import akka.pattern.Patterns;
import akka.util.Timeout;

import com.catify.processengine.core.data.dataobjects.DataObjectService;
import com.catify.processengine.core.messages.Message;
import com.catify.processengine.core.nodes.eventdefinition.EventDefinitionFactory;
import com.catify.processengine.core.nodes.eventdefinition.EventDefinitionParameter;
import com.catify.processengine.core.services.ActorReferenceService;

/**
 * Abstract class for all events.
 * 
 * @author christopher köster
 * 
 */
public abstract class Event extends FlowElement {

	static final Logger LOG = LoggerFactory.getLogger(Event.class);
	
	/** The EventDefinition parameter object of which an EventDefinition actor can be instantiated. */
	protected EventDefinitionParameter eventDefinitionParameter;

	protected DataObjectService dataObjectHandling;
	
	/** The timeout in seconds. Note: This value is only available after construction is completed. */
	@Value("${core.eventDefinitionTimeout}")
	protected long timeoutInSeconds;
	
	/**
	 * Creates an EventDefinition actor and <b>synchronously</b> calls its method associated to the given message type.
	 * After processing the message the created EventDefinition actor is stopped.
	 *
	 * @param message the message
	 */
	protected void createAndCallEventDefinitionActor(Message message) {
		
		final ActorRef eventDefinitionActor = createEventDefinitionActor(message);
		final Timeout eventDefinitionTimeout = new Timeout(Duration.create(timeoutInSeconds, "seconds"));
		
		// create an akka future which holds the commit message (if any) of the eventDefinitionActor
		Future<Object> future = Patterns.ask(eventDefinitionActor, message, eventDefinitionTimeout);

		try {
			// make a synchronous ('Await.result') request ('Patterns.ask') to the event definition actor 
			Await.result(future, eventDefinitionTimeout.duration());
		} catch (java.util.concurrent.TimeoutException timeout) {
			LOG.error(String.format("Unhandled timeout while processing %s at EventDefintition:%s. Timeout was set to %s", 
					message.getClass().getSimpleName(), eventDefinitionActor, eventDefinitionTimeout.duration()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// stop the event definition actor after processing message
		this.getContext().stop(eventDefinitionActor);
	}
	
	/**
	 * Creates the event definition actor from the eventDefinitionParameter field 
	 * and the process instance id of the message received.
	 *
	 * @param message the message
	 * @return the actor ref
	 */
	protected ActorRef createEventDefinitionActor(Message message) {
		ActorRef eventDefinitionActor = this.getContext().actorOf(new Props(new UntypedActorFactory() {
			private static final long serialVersionUID = 1L;

			public UntypedActor create() {
					return new EventDefinitionFactory().getEventDefinition(eventDefinitionParameter);
				}
		}), ActorReferenceService.getActorReferenceString(uniqueFlowNodeId+"-eventDefinition-"+message.getProcessInstanceId()));
		return eventDefinitionActor;
	}
	
	public DataObjectService getDataObjectService() {
		return dataObjectHandling;
	}

	public void setDataObjectHandling(DataObjectService dataObjectHandling) {
		this.dataObjectHandling = dataObjectHandling;
	}
	
	public EventDefinitionParameter getEventDefinitionParameter() {
		return eventDefinitionParameter;
	}

	public void setEventDefinitionParameter(
			EventDefinitionParameter eventDefinitionParameter) {
		this.eventDefinitionParameter = eventDefinitionParameter;
	}

}
