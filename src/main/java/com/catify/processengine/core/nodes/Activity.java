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
import java.util.List;

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

import com.catify.processengine.core.spi.DataObjectHandling;
import com.catify.processengine.core.messages.ActivationMessage;
import com.catify.processengine.core.messages.DeactivationMessage;
import com.catify.processengine.core.messages.Message;
import com.catify.processengine.core.nodes.eventdefinition.EventDefinitionFactory;
import com.catify.processengine.core.nodes.eventdefinition.EventDefinitionParameter;
import com.catify.processengine.core.services.ActorReferenceService;

/**
 * The abstract class Activity is a base class for all bpmn activities.
 * 
 * @author christopher köster
 * 
 */
public abstract class Activity extends FlowElement {
	
	public Activity(String uniqueProcessId, String uniqueFlowNodeId) {
		super(uniqueProcessId, uniqueFlowNodeId);
	}

	/** The boundary event connected to this activity. */
	protected List<ActorRef> boundaryEvents;

	/** The data object handling hides implementation details of the data spi. */
	protected DataObjectHandling dataObjectHandling;
	
	/** The timeout in seconds. Note: This value is only available after construction is completed. */
	@Value("${core.eventDefinitionTimeout}")
	protected long timeoutInSeconds;
	
	/** The event definition actor bound to this node. */
	protected ActorRef eventDefinitionActor;
	
	/**
	 * <b>Synchronously</b> calls an EventDefinition actor via sending a message to it and awaiting a result.
	 * Note: This is a blocking operation!
	 *
	 * @param message the message
	 */
	protected Future<Object> callEventDefinitionActor(Message message) {
		return this.callSynchronousActor(
				this.eventDefinitionActor, message, this.timeoutInSeconds);
	}
	
	/**
	 * Activate the attached boundary events.
	 *
	 * @param message the message
	 */
	protected void activateBoundaryEvents(ActivationMessage message) {
		this.sendMessageToNodeActors(
				new ActivationMessage(message.getProcessInstanceId()),
				this.getBoundaryEvents());
	}
	
	/**
	 * Deactivate the attached boundary events.
	 *
	 * @param message the message received
	 */
	protected void deactivateBoundaryEvents(Message message) {
		if (this.getBoundaryEvents() != null) {
		
			List<ActorRef> otherBoundaryEvents = this.getCopyOfBoundaryEvents();
			
			// if DeactivationMessage originates from an (interrupting) boundary event,
			// only deactivate the other boundary events
			if (otherBoundaryEvents.contains(this.getSender())) {
				otherBoundaryEvents.remove(this.getSender());
			}
				
			this.sendMessageToNodeActors(
			new DeactivationMessage(message.getProcessInstanceId()), otherBoundaryEvents);
		}
	}

	/**
	 * <b>Synchronously</b> calls an actor via sending a message to it and awaiting a result.
	 * Note: This is a blocking operation!
	 *
	 * @param synchonousActor the synchronous actor
	 * @param message the message
	 * @param timeoutInSeconds the timeout in seconds
	 * @return the awaited (synchronous) future 
	 */
	public Future<Object> callSynchronousActor(ActorRef synchonousActor, Message message, long timeoutInSeconds) {
		
		final Timeout synchronousTimeout = new Timeout(Duration.create(timeoutInSeconds, "seconds"));
		
		// create an akka future which holds the commit message (if any) of the eventDefinitionActor
		Future<Object> future = Patterns.ask(synchonousActor, message, synchronousTimeout);
	
		try {
			// make a synchronous ('Await.result') request ('Patterns.ask') to the event definition actor 
			Await.result(future, synchronousTimeout.duration());
		} catch (java.util.concurrent.TimeoutException timeout) {
			LOG.error(String.format("Unhandled timeout while processing %s at EventDefintition:%s. Timeout was set to %s", 
					message.getClass().getSimpleName(), synchonousActor, synchronousTimeout.duration()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return future;
	}

	/**
	 * Creates the event definition actor from the eventDefinitionParameter as a child node to the given actor context.
	 *
	 * @param uniqueFlowNodeId the unique flow node id
	 * @param message the message
	 * @param context the context
	 * @param eventDefinitionParameter the event definition parameter
	 * @return the ActorRef of the EventDefinition
	 */
	public ActorRef createEventDefinitionActor(final EventDefinitionParameter eventDefinitionParameter) {
		ActorRef eventDefinitionActor = this.context().actorOf(new Props(new UntypedActorFactory() {
			private static final long serialVersionUID = 1L;
	
			public UntypedActor create() {
					return new EventDefinitionFactory().getEventDefinition(eventDefinitionParameter);
				}
		}), ActorReferenceService.getActorReferenceString(this.context()+"-eventDefinition"));
		return eventDefinitionActor;
	}
	
	public List<ActorRef> getBoundaryEvents() {
		return boundaryEvents;
	}

	public void setBoundaryEvents(List<ActorRef> boundaryEvents) {
		this.boundaryEvents = boundaryEvents;
	}
	
	/**
	 * Get a copy of the boundary event list.
	 * 
	 * @return the copy of outgoing nodes
	 */
	private List<ActorRef> getCopyOfBoundaryEvents() {
		List<ActorRef> copy = new ArrayList<ActorRef>(this.getBoundaryEvents());
		return copy;
	}
	
	protected DataObjectHandling getDataObjectHandling() {
		return dataObjectHandling;
	}

	protected void setDataObjectHandling(DataObjectHandling dataObjectHandling) {
		this.dataObjectHandling = dataObjectHandling;
	}
	public long getTimeoutInSeconds() {
		return timeoutInSeconds;
	}

	public void setTimeoutInSeconds(long timeoutInSeconds) {
		this.timeoutInSeconds = timeoutInSeconds;
	}

	public ActorRef getEventDefinitionActor() {
		return eventDefinitionActor;
	}

	public void setEventDefinitionActor(ActorRef eventDefinitionActor) {
		this.eventDefinitionActor = eventDefinitionActor;
	}
}
