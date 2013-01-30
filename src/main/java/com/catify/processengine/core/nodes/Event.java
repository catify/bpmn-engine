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

import akka.actor.ActorRef;

import com.catify.processengine.core.data.dataobjects.DataObjectService;
import com.catify.processengine.core.messages.Message;
import com.catify.processengine.core.nodes.eventdefinition.EventDefinitionHandling;
import com.catify.processengine.core.nodes.eventdefinition.EventDefinitionParameter;

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

	/** The event definition actor bound to this node. */
	protected ActorRef eventDefinitionActor;
	
	/**
	 * <b>Synchronously</b> calls an EventDefinition actor via sending a message to it and awaiting a result.
	 * Note: This is a blocking operation!
	 *
	 * @param message the message
	 */
	protected void callEventDefinitionActor(Message message) {
		EventDefinitionHandling.callEventDefinitionActor(
				this.eventDefinitionActor,
				this.uniqueFlowNodeId, message, this.timeoutInSeconds,
				this.eventDefinitionParameter);
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
