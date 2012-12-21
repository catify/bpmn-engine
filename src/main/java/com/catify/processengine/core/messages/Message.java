package com.catify.processengine.core.messages;

import java.io.Serializable;

/**
 * Base class for akka message exchanges.
 * 
 * @author chris
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
