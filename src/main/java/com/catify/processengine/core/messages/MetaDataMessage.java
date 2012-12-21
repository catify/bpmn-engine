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
