package com.catify.processengine.core.messages;

/**
 * The activation message usually initializes a process instance on a given node
 * and triggers further actions based on the implemented message reaction.
 * 
 * @author chris
 * 
 */
public class ActivationMessage extends Message {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	private Object payload;

	/**
	 * Instantiates a new activation message.
	 */
	public ActivationMessage() {
	}

	/**
	 * Instantiates a new activation message.
	 *
	 * @param processInstanceId the process instance id
	 */
	public ActivationMessage(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	/**
	 * Gets the payload.
	 *
	 * @return the payload
	 */
	public Object getPayload() {
		return payload;
	}

	/**
	 * Sets the payload.
	 *
	 * @param payload the new payload
	 */
	public void setPayload(Object payload) {
		this.payload = payload;
	}

}
