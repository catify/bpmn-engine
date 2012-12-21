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
