package com.catify.processengine.core.messages;

public class LoopEndMessage extends Message {

	private static final long serialVersionUID = 1L;

	public LoopEndMessage(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}
	
}
