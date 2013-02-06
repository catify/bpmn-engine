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

import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import akka.actor.ActorRef;
import akka.event.EventBus;

import com.catify.processengine.core.data.model.NodeInstaceStates;
import com.catify.processengine.core.data.repositories.FlowNodeInstanceRepository;
import com.catify.processengine.core.messages.ActivationMessage;
import com.catify.processengine.core.messages.CommitMessage;
import com.catify.processengine.core.messages.DeactivationMessage;
import com.catify.processengine.core.messages.SignalEventMessage;
import com.catify.processengine.core.messages.TriggerMessage;
import com.catify.processengine.core.nodes.NodeUtils;

/**
 * Event definition for BPMN 2.0 signal events (catching and throwing).
 * 
 * @author claus straube
 *
 */
public class SignalEventDefinition extends EventDefinition {

	static final Logger LOG = LoggerFactory.getLogger(SignalEventDefinition.class);
	
	private boolean isStart;
	private boolean isThrow;
	private String signalRef;
	private ActorRef eventActorRef;
	private EventDefinitionParameter params;
	
	@Autowired
	private FlowNodeInstanceRepository repo;
	
	/**
	 * Creates a signal event definition.
	 * 
	 * @param eventNodeActorRef {@link ActorRef} to the base event node
	 * @param startEvent true if this is a start event node
	 * @param isThrow 
	 * @param signalRef the signal reference id
	 * @param params the {@link EventDefinitionParameter} bean
	 */
	public SignalEventDefinition(ActorRef eventNodeActorRef, boolean isStart, boolean isThrow, String signalRef, EventDefinitionParameter params) {
		this.isStart = isStart;
		this.isThrow = isThrow;
		this.signalRef = signalRef;
		this.eventActorRef = eventNodeActorRef;
		this.params = params;
	}
	
	/**
	 * Register node to the akka {@link EventBus}.
	 */
	@PostConstruct
	void init() {
		// register to event bus
		actorSystem.eventStream().subscribe(this.getSelf(), SignalEventMessage.class);
	}
	
	
	/* (non-Javadoc)
	 * @see com.catify.processengine.core.nodes.eventdefinition.EventDefinition#acitivate(com.catify.processengine.core.messages.ActivationMessage)
	 */
	@Override
	protected CommitMessage<?> activate(ActivationMessage message) {
		// throw part - push the signal message into the event bus
		if(isThrow) {
			LOG.debug(String.format("Received activate message. Sending now signal '%s'.", signalRef));
			actorSystem.eventStream().publish(new SignalEventMessage(signalRef));
		}
		return createSuccessfullCommitMessage(message.getProcessInstanceId());
	}
	/* (non-Javadoc)
	 * @see com.catify.processengine.core.nodes.eventdefinition.EventDefinition#deactivate(com.catify.processengine.core.messages.DeactivationMessage)
	 */
	@Override
	protected CommitMessage<?> deactivate(DeactivationMessage message) {
		// nothing to do
		return createSuccessfullCommitMessage(message.getProcessInstanceId());
	}
	/* (non-Javadoc)
	 * @see com.catify.processengine.core.nodes.eventdefinition.EventDefinition#trigger(com.catify.processengine.core.messages.TriggerMessage)
	 */
	@Override
	protected CommitMessage<?> trigger(TriggerMessage message) {
		// the event node does the work
		return createSuccessfullCommitMessage(message.getProcessInstanceId());
	}
	
	/**
	 * 	Mailbox for all {@link SignalEventMessage}s.
	 * 
	 * @param message
	 */
	protected void listen(SignalEventMessage message) {
		LOG.debug(String.format("Received signal '%s'.", message.getSignalRef()));
		/*
		 * filter out the correct signal reference.
		 */
		if(message.getSignalRef().equals(signalRef)) {
			LOG.debug(String.format("Signal '%s' equals awaited signal.", message.getSignalRef()));
			// catch part
			if(isStart) {
				/*
				 * Create exactly one new process instance by sending a
				 * trigger message to the base event node.
				 */
				LOG.debug("Starting new process instance.");
				eventActorRef.tell(new TriggerMessage(), getSender());
			} else {
				/*
				 * Load for all active signal catch events the
				 * process instance id from the database. Send
				 * for every entry a trigger message to the base event,
				 * to signal the node after the signal catch event. 
				 */
				LOG.debug("Find and trigger all waiting instances.");
				Set<String> activeInstanceIds = repo.findAllFlowNodeInstancesAtState(
						params.getUniqueProcessId(), 
						params.getUniqueFlowNodeId(), 
						NodeInstaceStates.ACTIVE_STATE);
				for (String iid : activeInstanceIds) {
					eventActorRef.tell(new TriggerMessage(iid, null), getSender());
				}
			}
		} else {
			LOG.debug(String.format("Received signal '%s' but awaited '%s'.", message.getSignalRef(), signalRef));
		}
	}
	
	@Override
	public void onReceive(Object message) throws Exception {
		LOG.debug(String.format("%s received %s", this.getSelf(), message
				.getClass().getSimpleName()));
		
		// process message and reply with a commit message to the underlying node event
		if (message instanceof ActivationMessage) {
			new NodeUtils().replyCommitMessage(activate((ActivationMessage) message), getSelf(), getSender());
		} else if (message instanceof DeactivationMessage) {
			new NodeUtils().replyCommitMessage(deactivate((DeactivationMessage) message), getSelf(), getSender());
		} else if (message instanceof TriggerMessage) {
			new NodeUtils().replyCommitMessage(trigger((TriggerMessage) message), getSelf(), getSender());
		} else if (message instanceof SignalEventMessage) {
			this.listen((SignalEventMessage) message);
		} else {
			LOG.warn("Unhandled message received: " + message.getClass());
			unhandled(message);
		}
	}

}
