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

import com.catify.processengine.core.data.dataobjects.DataObjectService;
import com.catify.processengine.core.messages.Message;
import com.catify.processengine.core.nodes.eventdefinition.EventDefinitionParameter;
import com.catify.processengine.core.nodes.eventdefinition.EventDefinitionHandling;


/**
 * The Class Task.
 * 
 * @author christopher köster
 * 
 */
public abstract class Task extends Activity {
	
	static final Logger LOG = LoggerFactory.getLogger(Task.class);

	protected EventDefinitionHandling data = new EventDefinitionHandling();
	
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
		new EventDefinitionHandling().createAndCallEventDefinitionActor(
				this.uniqueFlowNodeId, message, this.timeoutInSeconds,
				this.getContext(), this.eventDefinitionParameter);
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
