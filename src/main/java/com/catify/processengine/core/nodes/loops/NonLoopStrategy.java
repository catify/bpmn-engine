package com.catify.processengine.core.nodes.loops;

import akka.actor.ActorRef;

import com.catify.processengine.core.data.dataobjects.DataObjectHandling;
import com.catify.processengine.core.messages.ActivationMessage;
import com.catify.processengine.core.messages.DeactivationMessage;
import com.catify.processengine.core.messages.LoopEndMessage;
import com.catify.processengine.core.messages.TriggerMessage;
import com.catify.processengine.core.nodes.NodeParameter;

public class NonLoopStrategy extends LoopStrategy {
	
	/** The task action actor reference to the task that implements
	 * the bpmn task behavior (service task, receive task etc.). */
	protected ActorRef taskAction;
	
	private boolean catching;

	public NonLoopStrategy(ActorRef taskWrapper, NodeParameter nodeParameter,
			DataObjectHandling dataObjectHandling) {
		super(taskWrapper, nodeParameter.getUniqueProcessId(), nodeParameter.getUniqueFlowNodeId(), dataObjectHandling);
		this.taskAction = super.createTaskActionActor(this.getContext(), nodeParameter);
	}

	@Override
	public void activate(ActivationMessage message) {
		message.setPayload(LoopBehaviorService.loadPayloadFromDataObject(message.getProcessInstanceId(), uniqueProcessId, dataObjectHandling));
				
		taskAction.tell(message, taskWrapper);
		
		if (!catching) {
			taskWrapper.tell(new LoopEndMessage(message.getProcessInstanceId()), this.getSelf());
		}
	}

	@Override
	public void deactivate(DeactivationMessage message) {
		taskAction.tell(message, taskWrapper);
	}

	@Override
	public void trigger(TriggerMessage message) {
		LoopBehaviorService.savePayloadToDataObject(message.getProcessInstanceId(), message.getPayload(), uniqueProcessId, dataObjectHandling);
		
		taskAction.tell(message, taskWrapper);
		
		taskWrapper.tell(new LoopEndMessage(message.getProcessInstanceId()), this.getSelf());
	}

}
