package com.catify.processengine.core.messages;

public class LoopMessage extends Message {

	private static final long serialVersionUID = 1L;

	public LoopMessage(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}
	
}
