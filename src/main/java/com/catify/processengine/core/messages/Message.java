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
package com.catify.processengine.core.messages;

import java.io.Serializable;

/**
 * Base class for akka message exchanges.
 * 
 * @author christopher köster
 * 
 */
public abstract class Message implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The process instance id. */
	protected String processInstanceId;

	/**
	 * Gets the process instance id.
	 *
	 * @return the process instance id
	 */
	public String getProcessInstanceId() {
		return processInstanceId;
	}

	/**
	 * Sets the process instance id.
	 *
	 * @param processInstanceId the new process instance id
	 */
	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

}
