package com.catify.processengine.core.messages;

/**
 * Message for BPMN 2.0 link events.
 * 
 * @author claus straube
 *
 */
public class LinkEventMessage extends Message {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1779040828585017752L;

	private String target;
	private String uniqueProcessId;
	
	public LinkEventMessage(String target, String uniqueProcessId, String uniqueInstanceId) {
		this.target = target;
		this.uniqueProcessId = uniqueProcessId;
		this.processInstanceId = uniqueInstanceId;
	}
	
	public String getTarget() {
		return target;
	}
	
	public void setTarget(String target) {
		this.target = target;
	}
	
	public String getUniqueProcessId() {
		return uniqueProcessId;
	}
	
	public void setUniqueProcessId(String uniqueProcessId) {
		this.uniqueProcessId = uniqueProcessId;
	}	
	
}
