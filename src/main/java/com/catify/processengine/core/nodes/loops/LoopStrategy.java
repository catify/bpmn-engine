package com.catify.processengine.core.nodes.loops;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

import com.catify.processengine.core.data.dataobjects.DataObjectHandling;
import com.catify.processengine.core.messages.ActivationMessage;
import com.catify.processengine.core.messages.DeactivationMessage;
import com.catify.processengine.core.messages.TriggerMessage;
import com.catify.processengine.core.nodes.NodeUtils;

/**
 * The LoopStrategy holds the {@link LoopType}s for each message type ({@link ActivationMessage}, {@link DeactivationMessage},
 * {@link TriggerMessage}).
 */
public abstract class LoopStrategy extends UntypedActor {
	
	/** The task wrapper actor reference to the loop type strategy
	 * which binds the {@link LoopType}) implementation. */
	protected ActorRef taskWrapper;
	
	/** The task action actor reference to the task that implements
	 * the bpmn task behavior (service task, receive task etc.). */
	protected ActorRef taskAction;
	
	/** The unique process id. 
	 * @see com.catify.processengine.core.data.model.entities.ProcessNode#uniqueProcessId */
	protected String uniqueProcessId;
	
	protected String uniqueFlowNodeId;

	/** The data object handling wraps the {@link DataObjectHandling}. */
	protected DataObjectHandling dataObjectHandling;
	
	/**
	 * Instantiates a new loop strategy.
	 *
	 */
	public LoopStrategy(ActorRef taskWrapper,
			ActorRef taskAction, String uniqueProcessId, String uniqueFlowNodeId,
			DataObjectHandling dataObjectHandling) {
		super();
		this.taskWrapper = taskWrapper;
		this.taskAction = taskAction;
		this.uniqueProcessId = uniqueProcessId;
		this.uniqueFlowNodeId = uniqueFlowNodeId;
		this.dataObjectHandling = dataObjectHandling;
	}
	
	public final void onReceive(Object message) {
		if (message instanceof ActivationMessage) {
			activate((ActivationMessage) message);
		} else if (message instanceof TriggerMessage) {
			trigger((TriggerMessage) message);
		} else if (message instanceof DeactivationMessage) {
			deactivate((DeactivationMessage) message);
			// commit message after deactivation
			new NodeUtils().replySuccessfulCommit(((DeactivationMessage) message).getProcessInstanceId(), this.getSelf(), this.getSender());
		}
	}
	
	/**
	 * Reaction to {@link ActivationMessage}.
	 *
	 * @param message the message
	 */
	protected abstract void activate(ActivationMessage message); 
	
	/**
	 * Reaction to {@link DeactivationMessage}.
	 *
	 * @param message the message
	 */
	protected abstract void deactivate(DeactivationMessage message); 
	
	/**
	 * Reaction to {@link TriggerMessage}.
	 *
	 * @param message the message
	 */
	protected abstract void trigger(TriggerMessage message); 
	
}
