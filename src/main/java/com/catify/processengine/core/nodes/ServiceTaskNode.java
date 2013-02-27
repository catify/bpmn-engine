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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;

import com.catify.processengine.core.data.dataobjects.DataObjectHandling;
import com.catify.processengine.core.data.model.NodeInstaceStates;
import com.catify.processengine.core.messages.ActivationMessage;
import com.catify.processengine.core.messages.DeactivationMessage;
import com.catify.processengine.core.messages.LoopMessage;
import com.catify.processengine.core.messages.Message;
import com.catify.processengine.core.messages.TriggerMessage;
import com.catify.processengine.core.nodes.eventdefinition.EventDefinitionParameter;
import com.catify.processengine.core.services.ActorReferenceService;

/**
 * The ServiceTaskNode is the supervisor for the {@link ServiceTaskInstance}, which implements the actual bpmn service task. 
 * The ServiceTaskNode instantiates the synchronous ServiceTaskInstances. 
 * 
 * @author christopher köster
 * 
 */
@Configurable
public class ServiceTaskNode extends Task {

	/** The node factory. */
	@Autowired
	private NodeFactory nodeFactory;
	
	private EventDefinitionParameter eventDefinitionParameter;
	
	/**
	 * Instantiates a new service task node.
	 *
	 * @param uniqueProcessId the process id
	 * @param uniqueFlowNodeId the unique flow node id
	 * @param eventDefinitionParameter the event definition parameter
	 * @param dataObjectHandling the data object handling
	 * @param taskWrapper 
	 */
	public ServiceTaskNode(String uniqueProcessId, String uniqueFlowNodeId,
			EventDefinitionParameter eventDefinitionParameter, DataObjectHandling dataObjectHandling) {
		super(uniqueProcessId, uniqueFlowNodeId);
		// all service task instances share this service object, it might therefore need some kind of synchronization
		this.setDataObjectHandling(dataObjectHandling);
		this.eventDefinitionParameter = eventDefinitionParameter;
	}
	
	@Override
	protected void activate(ActivationMessage message) {
		
		ActorRef serviceTaskInstance = this.getContext().actorOf(new Props(
				new UntypedActorFactory() {
					private static final long serialVersionUID = 1L;

					// create an instance of a (synchronous) service worker
					public UntypedActor create() {
							return new ServiceTaskInstance(getUniqueProcessId(), getUniqueFlowNodeId(), 
									eventDefinitionParameter, getDataObjectHandling());
					}
				}), this.getTaskInstanceActorRef(message));
		LOG.debug(String.format("Service task instance craeted %s --> resulting akka object: %s", this.getClass(),
				serviceTaskInstance.toString()));
		
		this.sendMessageToNodeActor(message, serviceTaskInstance);
	}

	@Override
	protected void deactivate(DeactivationMessage message) {		
		// if a service task instance is still active and waiting, stop it (invokes akka stop hooks) 
		this.getContext().stop(this.getContext().actorFor(this.getTaskInstanceActorRef(message)));
		
		this.getNodeInstanceMediatorService().setNodeInstanceEndTime(message.getProcessInstanceId(), new Date());
		
		this.getNodeInstanceMediatorService().setState(
				message.getProcessInstanceId(),
				NodeInstaceStates.DEACTIVATED_STATE);
		
		this.deactivateBoundaryEvents(message);
		
		this.getNodeInstanceMediatorService().persistChanges();
	}

	@Override
	protected void trigger(TriggerMessage message) {
		LOG.warn(String.format("Reaction to %s not implemented in %s. Please check your process.", message.getClass().getSimpleName(), this.getSelf()));
	}
	
	/* (non-Javadoc)
	 * @see com.catify.processengine.core.nodes.FlowElement#handleNonStandardMessage(java.lang.Object)
	 */
	protected void handleNonStandardMessage(Object message) {
		if (message instanceof LoopMessage) {
			this.forwardLoopMessageToParent(message);
		} else {
			unhandled(message);
		}
	}

	/**
	 * Forward {@link LoopMessage}s to the parent actor, which should be a loop strategy. The LoopMessages are generated in the {@link ServiceTaskInstance}s. 
	 *
	 * @param message the message
	 */
	private void forwardLoopMessageToParent(Object message) {
		LoopMessage loopMessage = (LoopMessage) message;
		this.sendMessageToNodeActor(loopMessage, this.getContext().parent());
	}
	
	/**
	 * Gets the task instance ActorRef which is a concatenation of the uniqueFlowNodeId and the processInstanceId of a message.
	 *
	 * @param message the message
	 * @return the task instance actor ref
	 */
	private String getTaskInstanceActorRef(Message message) {
		return ActorReferenceService.getAkkaComplientString(this.getUniqueFlowNodeId()+message.getProcessInstanceId());
	}

}
