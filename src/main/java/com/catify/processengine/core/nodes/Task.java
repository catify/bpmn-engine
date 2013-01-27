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

import akka.actor.ActorRef;
import akka.dispatch.Await;
import akka.pattern.Patterns;
import akka.util.Duration;
import akka.util.Timeout;

import com.catify.processengine.core.data.dataobjects.DataObjectService;
import com.catify.processengine.core.messages.Message;
import com.catify.processengine.core.nodes.eventdefinition.EventDefinition;


public abstract class Task extends Activity {

	/**
	 * Holds the event definition implementation.
	 * 
	 * @see EventDefinition
	 */
	protected ActorRef eventDefinition;
	
	/** The default timeout for a synchronous call to an EventDefinition. */
	protected Timeout eventDefinitionTimeout = new Timeout(Duration.create(60, "seconds"));

	protected DataObjectService dataObjectHandling;
	
	/**
	 * Creates an EventDefinition actor and <b>synchronously</b> calls its method associated to the given message type.
	 *
	 * @param message the message
	 */
	protected void createAndCallEventDefinition(Message message) {
		try {
			// make a synchronous ('Await.result') request ('Patterns.ask') to the event definition actor 
			Await.result(Patterns.ask(this.eventDefinition, message, this.eventDefinitionTimeout), this.eventDefinitionTimeout.duration());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets the event definition timeout used for synchronous calls.
	 *
	 * @return the event definition timeout
	 */
	public Timeout getEventDefinitionTimeout() {
		return eventDefinitionTimeout;
	}

	/**
	 * Sets the event definition timeout used for synchronous calls.
	 *
	 * @param eventDefinitionTimeout the new event definition timeout
	 */
	public void setEventDefinitionTimeout(Timeout eventDefinitionTimeout) {
		this.eventDefinitionTimeout = eventDefinitionTimeout;
	}
	
	public DataObjectService getDataObjectService() {
		return dataObjectHandling;
	}
	
	public ActorRef getEventDefinition() {
		return eventDefinition;
	}

	public void setEventDefinition(ActorRef eventDefinition) {
		this.eventDefinition = eventDefinition;
	}

	public void setDataObjectHandling(DataObjectService dataObjectHandling) {
		this.dataObjectHandling = dataObjectHandling;
	}

}
