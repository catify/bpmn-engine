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
package com.catify.processengine.core.spi;

import com.catify.processengine.core.messages.Message;

/**
 * The IntegrationMessage that is used by the integration spi.
 * 
 * @author christopher köster
 * 
 */
public class IntegrationMessage  extends Message {

	private static final long serialVersionUID = 1L;

	private String uniqueProcessId;

	private String uniqueFlowNodeId;

	private Object payload;

	/**
	 * Instantiates a new integration message.
	 *
	 * @param uniqueProcessId the unique process id
	 * @param uniqueFlowNodeId the unique flow node id
	 * @param correlationKey the correlation key
	 * @param payload the data
	 */
	public IntegrationMessage(String uniqueProcessId, String uniqueFlowNodeId, String processInstanceId, Object payload) {
		super();
		this.uniqueProcessId = uniqueProcessId;
		this.uniqueFlowNodeId = uniqueFlowNodeId;
		this.payload = payload;
		this.processInstanceId = processInstanceId;
	}
	
	public String getProcessId() {
		return uniqueProcessId;
	}

	public void setProcessId(String processId) {
		this.uniqueProcessId = processId;
	}

	public String getUniqueFlowNodeId() {
		return uniqueFlowNodeId;
	}

	public void setUniqueFlowNodeId(String nodeId) {
		this.uniqueFlowNodeId = nodeId;
	}
	
	public Object getPayload() {
		return payload;
	}

	public void setPayload(Object payload) {
		this.payload = payload;
	}
}
