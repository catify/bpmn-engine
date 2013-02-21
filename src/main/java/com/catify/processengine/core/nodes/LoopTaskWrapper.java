package com.catify.processengine.core.nodes;

import java.util.List;

import akka.actor.ActorRef;

import com.catify.processengine.core.messages.ActivationMessage;
import com.catify.processengine.core.messages.DeactivationMessage;
import com.catify.processengine.core.messages.LoopEndMessage;
import com.catify.processengine.core.messages.TriggerMessage;

/**
 * The Class LoopTaskWrapper uses a strategy pattern to wrap the looping behavior around the bpmn tasks. 
 * The {@link NodeFactory} will set the context of {@link LoopType} and {@link Task} based on the bpmn process definition.
 */
public class LoopTaskWrapper extends Task {

	private ActorRef loopStrategy;

	/**
	 * Instantiates a new loop task wrapper.
	 *
	 * @param uniqueProcessId the unique process id
	 * @param uniqueFlowNodeId the unique flow node id
	 * @param outgoingNodes the list of outgoing node actorRefs
	 * @param loopStrategy the strategy
	 */
	public LoopTaskWrapper(String uniqueProcessId, String uniqueFlowNodeId, List<ActorRef> outgoingNodes, ActorRef loopStrategy, List<ActorRef> boundaryEvent) {
		super(uniqueProcessId, uniqueFlowNodeId);

		this.setOutgoingNodes(outgoingNodes);
		this.setBoundaryEvents(boundaryEvent);
		
		this.loopStrategy = loopStrategy;
	}
	
	@Override
	protected void activate(ActivationMessage message) {
		this.activateBoundaryEvents(message);
		loopStrategy.tell(message, getSelf());
	}
	
	@Override
	protected void deactivate(DeactivationMessage message) {
		this.deactivateBoundaryEvents(message);
		loopStrategy.tell(message, getSelf());
	}
	
	@Override
	protected void trigger(TriggerMessage message) {
		this.deactivateBoundaryEvents(message);
		loopStrategy.tell(message, getSelf());
	}
	
	@Override
	protected void handleNonStandardMessage(Object message) {
		if (message instanceof LoopEndMessage) {
			endLoop((LoopEndMessage) message);
		} else {
			unhandled(message);
		}
	}

	private void endLoop(LoopEndMessage message) {
		this.deactivateBoundaryEvents(message);
		
		// send message to following actors (outside the loop)
		this.sendMessageToNodeActors(new ActivationMessage(message.getProcessInstanceId()), this.getOutgoingNodes());
	}

}
