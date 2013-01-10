package com.catify.processengine.core.integration;

import com.catify.processengine.core.messages.Message;

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
