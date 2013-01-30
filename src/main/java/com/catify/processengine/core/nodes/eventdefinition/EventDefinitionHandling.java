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

	/**
	 * Creates an EventDefinition actor and <b>synchronously</b> calls its method associated to the given message type.
	 * After processing the message the created EventDefinition actor is stopped.
	 *
	 * @param uniqueFlowNodeId the unique flow node id
	 * @param message the message
	 * @param timeoutInSeconds the timeout in seconds
	 * @param context the akka actor context (of the calling actor)
	 * @param eventDefinitionParameter the event definition parameter to instantiate the EventDefinition actor
	 */
	public void createAndCallEventDefinitionActor(String uniqueFlowNodeId, Message message, long timeoutInSeconds, UntypedActorContext context, EventDefinitionParameter eventDefinitionParameter) {
		
		final ActorRef eventDefinitionActor = createEventDefinitionActor(uniqueFlowNodeId, message, context, eventDefinitionParameter);
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
		context.stop(eventDefinitionActor);
	}
	
	/**
	 * Creates the event definition actor from the eventDefinitionParameter field 
	 * and the process instance id of the message received.
	 *
	 * @param message the message
	 * @return the actor ref
	 */
	private ActorRef createEventDefinitionActor(String uniqueFlowNodeId, Message message, UntypedActorContext context, final EventDefinitionParameter eventDefinitionParameter) {
		ActorRef eventDefinitionActor = context.actorOf(new Props(new UntypedActorFactory() {
			private static final long serialVersionUID = 1L;

			public UntypedActor create() {
					return new EventDefinitionFactory().getEventDefinition(eventDefinitionParameter);
				}
		}), ActorReferenceService.getActorReferenceString(uniqueFlowNodeId+"-eventDefinition-"+message.getProcessInstanceId()));
		return eventDefinitionActor;
	}
}