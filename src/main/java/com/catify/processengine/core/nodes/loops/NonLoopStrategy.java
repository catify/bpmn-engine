package com.catify.processengine.core.nodes.loops;

import akka.actor.ActorRef;

import com.catify.processengine.core.data.dataobjects.DataObjectHandling;
import com.catify.processengine.core.messages.ActivationMessage;
import com.catify.processengine.core.messages.DeactivationMessage;
import com.catify.processengine.core.messages.TriggerMessage;

public class NonLoopStrategy extends LoopStrategy {

	public NonLoopStrategy(ActorRef taskWrapper,
			ActorRef taskAction, String uniqueProcessId, String uniqueFlowNodeId,
			DataObjectHandling dataObjectHandling) {
		super(taskWrapper, taskAction, uniqueProcessId, uniqueFlowNodeId, dataObjectHandling);
	}

	@Override
	public void activate(ActivationMessage message) {
		message.setPayload(LoopBehaviorService.loadPayloadFromDataObject(message.getProcessInstanceId(), uniqueProcessId, dataObjectHandling));
				
		taskAction.tell(message, taskWrapper);
	}

	@Override
	public void deactivate(DeactivationMessage message) {
		taskAction.tell(message, taskWrapper);
	}

	@Override
	public void trigger(TriggerMessage message) {
		LoopBehaviorService.savePayloadToDataObject(message.getProcessInstanceId(), message.getPayload(), uniqueProcessId, dataObjectHandling);
		
		taskAction.tell(message, taskWrapper);
	}

}
