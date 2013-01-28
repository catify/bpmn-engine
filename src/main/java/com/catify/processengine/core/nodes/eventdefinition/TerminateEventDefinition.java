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
package com.catify.processengine.core.nodes.eventdefinition;

import static akka.dispatch.Futures.sequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import scala.concurrent.Await;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;

import com.catify.processengine.core.messages.ActivationMessage;
import com.catify.processengine.core.messages.DeactivationMessage;
import com.catify.processengine.core.messages.TriggerMessage;

public class TerminateEventDefinition extends EventDefinition {

	private String uniqueProcessId;
	private String uniqueFlowNodeId;
	private ActorRef actorRef;
	
	/** The actor references of all other node services (including sub process nodes). */
	private Set<ActorRef> otherActorReferences;
	
	/**
	 * Instantiates a new terminate event definition.
	 *
	 * @param uniqueProcessId the unique process id
	 * @param uniqueFlowNodeId the unique flow node id
	 * @param actorRef the actor ref
	 */
	public TerminateEventDefinition(
			String uniqueProcessId, String uniqueFlowNodeId, ActorRef actorRef, Set<ActorRef> otherActorReferences) {
		super();
		this.uniqueProcessId = uniqueProcessId;
		this.uniqueFlowNodeId = uniqueFlowNodeId;
		this.actorRef = actorRef;
		this.otherActorReferences = otherActorReferences;
	}
	
	/* (non-Javadoc)
	 * @see com.catify.processengine.core.nodes.eventdefinition.EventDefinition#acitivate(com.catify.processengine.core.messages.ActivationMessage)
	 */
	@Override
	protected void activate(ActivationMessage message) {
		final ExecutionContext ec = actorSystem.dispatcher();

		List<Future<Object>> listOfFutureActorRefs = new ArrayList<Future<Object>>();
		
		/** The timeout for collecting the deactivation commits (which is slightly shorter than the timeout 
		 * for this event definition, to be able to collect better timeout data) */
		Timeout deactivationTimeout = new Timeout(Duration.create((long) (timeoutInSeconds * 0.9), "seconds"));
		
		for (ActorRef actor : otherActorReferences) {
			try {
				// make an asynchronous request ('Patterns.ask') to the event definition actor 
				listOfFutureActorRefs.add(Patterns.ask(actor, new DeactivationMessage(message.getProcessInstanceId()), deactivationTimeout));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// compose the sequence of the received commit futures
        Future<Iterable<Object>> futuresSequence = sequence(listOfFutureActorRefs, ec);
 
        // block until all futures came back
		try {
			Await.result(futuresSequence, deactivationTimeout.duration());
		} catch (java.util.concurrent.TimeoutException timeout) {
			LOG.error(String.format("Timeout while processing %s at EventDefintition:%s. Timeout was set to %s", 
			message.getClass().getSimpleName(), this.getClass().getSimpleName(), deactivationTimeout.duration()));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see com.catify.processengine.core.nodes.eventdefinition.EventDefinition#deactivate(com.catify.processengine.core.messages.DeactivationMessage)
	 */
	@Override
	protected void deactivate(DeactivationMessage message) {
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see com.catify.processengine.core.nodes.eventdefinition.EventDefinition#trigger(com.catify.processengine.core.messages.TriggerMessage)
	 */
	@Override
	protected void trigger(TriggerMessage message) {
		// nothing to do
	}

	public String getUniqueProcessId() {
		return uniqueProcessId;
	}

	public void setUniqueProcessId(String uniqueProcessId) {
		this.uniqueProcessId = uniqueProcessId;
	}

	public String getUniqueFlowNodeId() {
		return uniqueFlowNodeId;
	}

	public void setUniqueFlowNodeId(String uniqueFlowNodeId) {
		this.uniqueFlowNodeId = uniqueFlowNodeId;
	}

	public ActorRef getActorRef() {
		return actorRef;
	}

	public void setActorRef(ActorRef actorRef) {
		this.actorRef = actorRef;
	}
}
