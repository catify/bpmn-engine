package com.catify.processengine.core.nodes.loops;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorContext;
import akka.actor.UntypedActorFactory;

import com.catify.processengine.core.data.dataobjects.DataObjectHandling;
import com.catify.processengine.core.messages.ActivationMessage;
import com.catify.processengine.core.messages.DeactivationMessage;
import com.catify.processengine.core.messages.LoopMessage;
import com.catify.processengine.core.messages.TriggerMessage;
import com.catify.processengine.core.nodes.ActivityActionFactory;
import com.catify.processengine.core.nodes.NodeParameter;
import com.catify.processengine.core.nodes.NodeUtils;
import com.catify.processengine.core.services.ActorReferenceService;

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
	
	/** True for catching/receiving nodes. */
	protected boolean catching;
	
	/**
	 * Instantiates a new loop strategy.
	 *
	 */
	public LoopStrategy(ActorRef taskWrapper, String uniqueProcessId, String uniqueFlowNodeId,
			DataObjectHandling dataObjectHandling, boolean catching) {
		super();
		this.taskWrapper = taskWrapper;
		this.uniqueProcessId = uniqueProcessId;
		this.uniqueFlowNodeId = uniqueFlowNodeId;
		this.dataObjectHandling = dataObjectHandling;
		this.catching = catching;
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
		} else if (message instanceof LoopMessage) {
			loop((LoopMessage) message);
		} else if (message instanceof DeactivationMessage) {
			// commit message for already passed nodes (which do not need deactivation)
			new NodeUtils().replySuccessfulCommit(((DeactivationMessage) message).getProcessInstanceId(), this.getSelf(), this.getSender());
		}
	}
	
	/**
	 * Creates the event definition actor from the eventDefinitionParameter as a child node to the given actor context.
	 *
	 * @param uniqueFlowNodeId the unique flow node id
	 * @param context the context
	 * @param nodeParameter the node parameter
	 * @param identifier the identifier of the actor to create
	 * @return the ActorRef of the EventDefinition
	 */
	public static ActorRef createTaskActionActor(UntypedActorContext context, final NodeParameter nodeParameter) {
		ActorRef eventDefinitionActor = context.actorOf(new Props(new UntypedActorFactory() {
			private static final long serialVersionUID = 1L;

			public UntypedActor create() {
					return new ActivityActionFactory().createServiceNode(nodeParameter.clientId, nodeParameter.processJaxb, nodeParameter.subProcessesJaxb, nodeParameter.flowNodeJaxb, nodeParameter.sequenceFlowsJaxb);
				}
		}), ActorReferenceService.getActorReferenceString(nodeParameter.getUniqueFlowNodeId() + "-taskAction-"));
		return eventDefinitionActor;
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
	
	/**
	 * Reaction to {@link LoopMessage} originated by a task action actor. This method inits another loop instance.
	 *
	 * @param message the message
	 */
	protected void loop(LoopMessage message) {
		this.getSelf().tell(new ActivationMessage(message.getProcessInstanceId()), this.getSelf());
	}
	
}
