/**
 * -------------------------------------------------------
 * Copyright (C) 2013 catify <info@catify.com>
 * -------------------------------------------------------
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

/**
 * The deactivation message stops a process instance on a given node and
 * triggers further actions based on the implemented message reaction.
 * 
 * @author chris
 * 
 */
public class DeactivationMessage extends Message {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Instantiates a new deactivation message.
	 */
	public DeactivationMessage() {
	}

	/**
	 * Instantiates a new deactivation message.
	 *
	 * @param processInstanceId the process instance id
	 */
	public DeactivationMessage(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

}
