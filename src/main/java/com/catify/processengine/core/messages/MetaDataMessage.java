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
/**
 * 
 */
package com.catify.processengine.core.messages;

import java.util.Map;

import com.catify.processengine.core.integration.MessageIntegrationSPI;

/**
 * The meta data message carries the meta data generated in the message
 * integration ({@link MessageIntegrationSPI}.
 * 
 * @author chris
 * 
 */
public class MetaDataMessage extends Message {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The unique process id. */
	private String uniqueProcessId;
	
	/** The meta data. */
	private Map<String, Object> metaData;

	/**
	 * Instantiates a new meta data message.
	 *
	 * @param uniqueProcessId the unique process id
	 * @param processInstanceId the process instance id
	 * @param metaData the meta data
	 */
	public MetaDataMessage(String uniqueProcessId, String processInstanceId,
			Map<String, Object> metaData) {
		this.uniqueProcessId = uniqueProcessId;
		this.processInstanceId = processInstanceId;
		this.setMetaData(metaData);
	}

	/**
	 * Gets the meta data.
	 *
	 * @return the meta data
	 */
	public Map<String, Object> getMetaData() {
		return metaData;
	}

	/**
	 * Sets the meta data.
	 *
	 * @param metaData the meta data
	 */
	public final void setMetaData(Map<String, Object> metaData) {
		this.metaData = metaData;
	}

	/**
	 * Gets the unique process id.
	 *
	 * @return the unique process id
	 */
	public String getUniqueProcessID() {
		return uniqueProcessId;
	}

	/**
	 * Sets the unique process id.
	 *
	 * @param uniqueProcessID the new unique process id
	 */
	public void setUniqueProcessID(String uniqueProcessID) {
		this.uniqueProcessId = uniqueProcessID;
	}
}
