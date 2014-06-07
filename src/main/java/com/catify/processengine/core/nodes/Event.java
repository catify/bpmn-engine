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

import com.catify.processengine.core.spi.DataObjectHandling;

/**
 * Abstract class for all events.
 * 
 * @author christopher köster
 * 
 */
public abstract class Event extends Task {

	public Event(String uniqueProcessId, String uniqueFlowNodeId) {
		super(uniqueProcessId, uniqueFlowNodeId);
	}

	static final Logger LOG = LoggerFactory.getLogger(Event.class);

	protected DataObjectHandling dataObjectHandling;
	
	public DataObjectHandling getDataObjectHandling() {
		return dataObjectHandling;
	}

	public void setDataObjectHandling(DataObjectHandling dataObjectHandling) {
		this.dataObjectHandling = dataObjectHandling;
	}

}
