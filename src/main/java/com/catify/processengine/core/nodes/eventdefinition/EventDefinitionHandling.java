package com.catify.processengine.core.nodes.eventdefinition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorContext;
import akka.actor.UntypedActorFactory;
import akka.pattern.Patterns;
import akka.util.Timeout;

import com.catify.processengine.core.messages.Message;
import com.catify.processengine.core.services.ActorReferenceService;

public class EventDefinitionHandling {

	static final Logger LOG = LoggerFactory.getLogger(EventDefinitionHandling.class);

	private EventDefinitionHandling() {
	}
	
	/**
	 * <b>Synchronously</b> calls an EventDefinition actor via sending a message to it and awaiting a result.
	 * Note: This is a blocking operation!
	 *
	 * @param eventDefinitionActor the event definition actor
	 * @param uniqueFlowNodeId the unique flow node id
	 * @param message the message
	 * @param timeoutInSeconds the timeout in seconds
	 * @param eventDefinitionParameter the event definition parameter to instantiate the EventDefinition actor
	 */
	public static void callEventDefinitionActor(ActorRef eventDefinitionActor, String uniqueFlowNodeId, Message message, long timeoutInSeconds) {
		
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
	public static ActorRef createEventDefinitionActor(String uniqueFlowNodeId, UntypedActorContext context, final EventDefinitionParameter eventDefinitionParameter) {
		ActorRef eventDefinitionActor = context.actorOf(new Props(new UntypedActorFactory() {
			private static final long serialVersionUID = 1L;

			public UntypedActor create() {
					return new EventDefinitionFactory().getEventDefinition(eventDefinitionParameter);
				}
		}), ActorReferenceService.getActorReferenceString(uniqueFlowNodeId+"-eventDefinition-"));
		return eventDefinitionActor;
	}
}