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

import scala.concurrent.Future;

import com.catify.processengine.core.data.dataobjects.DataObjectHandling;
import com.catify.processengine.core.data.model.NodeInstaceStates;
import com.catify.processengine.core.messages.ActivationMessage;
import com.catify.processengine.core.messages.CommitMessage;
import com.catify.processengine.core.messages.DeactivationMessage;
import com.catify.processengine.core.messages.LoopMessage;
import com.catify.processengine.core.messages.TriggerMessage;
import com.catify.processengine.core.nodes.eventdefinition.EventDefinitionParameter;

/**
 * The ServiceTaskInstance is a synchronous node. It will load a value from a data object (if specified), 
 * issue a request to the provided message integration and stay active until it gets a reply (or runs in a timeout specified 
 * in the message integration implementation). The reply received is then saved to a data object (if specified).
 * 
 * @author christopher köster
 * 
 */
public class ServiceTaskInstance extends Task {

	/**
	 * Instantiates a new service task node.
	 *
	 * @param uniqueProcessId the process id
	 * @param uniqueFlowNodeId the unique flow node id
	 * @param eventDefinitionParameter the event definition parameter
	 * @param dataObjectHandling the data object handling
	 */	
	public ServiceTaskInstance(String uniqueProcessId, String uniqueFlowNodeId,
			EventDefinitionParameter eventDefinitionParameter, DataObjectHandling dataObjectHandling) {
		super(uniqueProcessId, uniqueFlowNodeId);
		this.setDataObjectHandling(dataObjectHandling);
		
		this.eventDefinitionActor = this.createEventDefinitionActor(eventDefinitionParameter);
	}
	
	@Override
	protected void activate(ActivationMessage message) {
		this.getNodeInstanceMediatorService().setState(
				message.getProcessInstanceId(), NodeInstaceStates.ACTIVE_STATE);
		
		this.getNodeInstanceMediatorService().setNodeInstanceStartTime(message.getProcessInstanceId(), new Date());
		
		Future<Object> repliedFuture = this.callEventDefinitionActor(message);
		
		Object repliedPayload = this.getPayload(repliedFuture);
		
		this.getDataObjectHandling().saveObject(this.getUniqueProcessId(), message.getProcessInstanceId(), repliedPayload);
		
		this.getNodeInstanceMediatorService().setNodeInstanceEndTime(message.getProcessInstanceId(), new Date());
		
		this.getNodeInstanceMediatorService().setState(
				message.getProcessInstanceId(), NodeInstaceStates.PASSED_STATE);
		
		this.getNodeInstanceMediatorService().persistChanges();
		
		this.sendMessageToNodeActor(new LoopMessage(message.getProcessInstanceId()), this.getContext().parent());
		
		// stop this instance node
		this.getContext().stop(this.getSelf());
	}

	/**
	 * Gets the payload from a future which holds a CommitMessage.
	 *
	 * @param repliedFuture the replied future
	 * @return the payload
	 */
	@SuppressWarnings("unchecked")
	private Object getPayload(Future<Object> repliedFuture) {
		Object repliedCommitMessage = repliedFuture.value().get().get();
		if (repliedCommitMessage instanceof CommitMessage) {
			return ((CommitMessage<Object>) repliedCommitMessage).getPayload();
		} else {
			LOG.error(String.format("Unexpected message type received: expected %s, but was %s", CommitMessage.class, repliedCommitMessage.getClass()));
			return null;
		}
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
