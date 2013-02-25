package com.catify.processengine.core.nodes.loops;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;

import com.catify.processengine.core.data.dataobjects.DataObjectHandling;
import com.catify.processengine.core.messages.ActivationMessage;
import com.catify.processengine.core.messages.DeactivationMessage;
import com.catify.processengine.core.messages.LoopMessage;
import com.catify.processengine.core.messages.TriggerMessage;
import com.catify.processengine.core.nodes.NodeParameter;

public class NonLoopStrategy extends LoopStrategy {
	
	static final Logger LOG = LoggerFactory.getLogger(NonLoopStrategy.class);
	
	/** The task action actor reference to the task that implements
	 * the bpmn task behavior (service task, receive task etc.). */
	protected ActorRef taskAction;

	public NonLoopStrategy(ActorRef taskWrapper, NodeParameter nodeParameter,
			DataObjectHandling dataObjectHandling, boolean catching) {
		super(taskWrapper, nodeParameter.getUniqueProcessId(), nodeParameter.getUniqueFlowNodeId(), dataObjectHandling, catching);
		this.taskAction = super.createTaskActionActor(this.getContext(), nodeParameter);
	}

	@Override
	public void activate(ActivationMessage message) {
		message.setPayload(LoopBehaviorService.loadPayloadFromDataObject(message.getProcessInstanceId(), uniqueProcessId, dataObjectHandling));
				
		this.sendMessageToNodeActor(message, this.taskAction);
		
		if (!catching) {
			this.sendMessageToNodeActor(new LoopMessage(message.getProcessInstanceId()), this.taskWrapper);
		}
	}

	@Override
	public void deactivate(DeactivationMessage message) {
		this.sendMessageToNodeActor(message, this.taskAction);
	}

	@Override
	public void trigger(TriggerMessage message) {
		LoopBehaviorService.savePayloadToDataObject(message.getProcessInstanceId(), message.getPayload(), uniqueProcessId, dataObjectHandling);
		
		this.sendMessageToNodeActor(message, this.taskAction);
		
		this.sendMessageToNodeActor(new LoopMessage(message.getProcessInstanceId()), this.taskWrapper);
	}

	@Override
	protected void loop(LoopMessage message) {
		LOG.warn(String.format("Unexpected %s received by %s", message.getClass(), this.getSelf()));
	}

}
