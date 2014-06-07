package com.catify.processengine.core.nodes.loops;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorContext;
import akka.actor.UntypedActorFactory;

import com.catify.processengine.core.spi.DataObjectHandling;
import com.catify.processengine.core.messages.ActivationMessage;
import com.catify.processengine.core.messages.DeactivationMessage;
import com.catify.processengine.core.messages.LoopMessage;
import com.catify.processengine.core.messages.Message;
import com.catify.processengine.core.messages.TriggerMessage;
import com.catify.processengine.core.nodes.ActivityActionFactory;
import com.catify.processengine.core.nodes.NodeParameter;
import com.catify.processengine.core.nodes.NodeUtils;
import com.catify.processengine.core.services.ActorReferenceService;
import com.catify.processengine.core.services.NodeInstanceMediatorService;

/**
 * The LoopStrategy holds the {@link LoopType}s for each message type ({@link ActivationMessage}, {@link DeactivationMessage},
 * {@link TriggerMessage}).
 */
public abstract class LoopStrategy extends UntypedActor {
	
	static final Logger LOG = LoggerFactory.getLogger(LoopStrategy.class);
	
	/** The activity wrapper actor reference to the loop type strategy
	 * which binds the {@link LoopType}) implementation. */
	protected ActorRef activityWrapper;
	
	/** The activity action actor reference to the task that implements
	 * the bpmn task behavior (service task, receive task etc.). */
	protected ActorRef activityAction;

	/** The unique process id. 
	 * @see com.catify.processengine.core.data.model.entities.ProcessNode#uniqueProcessId */
	protected String uniqueProcessId;
	
	protected String uniqueFlowNodeId;

	/** The data object handling wraps the {@link DataObjectHandling}. */
	protected DataObjectHandling dataObjectHandling;
	
	/** True for catching/receiving nodes. */
	protected boolean catching;
	
	/** The node instance mediator service. */
	protected NodeInstanceMediatorService nodeInstanceMediatorService;
	
	/**
	 * Instantiates a new loop strategy.
	 *
	 */
	public LoopStrategy(ActorRef taskWrapper, String uniqueProcessId, String uniqueFlowNodeId,
			DataObjectHandling dataObjectHandling, boolean catching) {
		super();
		this.activityWrapper = taskWrapper;
		this.uniqueProcessId = uniqueProcessId;
		this.uniqueFlowNodeId = uniqueFlowNodeId;
		this.dataObjectHandling = dataObjectHandling;
		this.catching = catching;
		
		this.setNodeInstanceMediatorService(new NodeInstanceMediatorService(
				uniqueProcessId, uniqueFlowNodeId));
	}
	
	public final void onReceive(Object message) {
		LOG.debug(String.format("%s received %s", this.getSelf(), message
				.getClass().getSimpleName()));
		
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
	 * Reaction to {@link LoopMessage} originated by a task action actor. This method inits another loop instance.
	 *
	 * @param message the message
	 */
	protected void loop(LoopMessage message) {
		this.sendMessageToNodeActor(new ActivationMessage(message.getProcessInstanceId()), this.getSelf());
	}
	
	/**
	 * Gets the loop counter from the db (avoiding any caches). The loopCounter is the loopCount (how many loops have been
	 * done until now) + 1 (the current loop iteration).
	 *
	 * @param message the message
	 * @return the loop count
	 */
	protected int getLoopCounter(Message message) {
		nodeInstanceMediatorService.refreshFlowNodeInstance(uniqueProcessId, uniqueFlowNodeId, message.getProcessInstanceId());
		int loopCounter = nodeInstanceMediatorService.getLoopCount(message.getProcessInstanceId());
		return ++loopCounter;
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
	 * Send a message object to a node actor with getSelf()-sender.
	 * 
	 * @param message
	 *            the message to send
	 * @param targetNode
	 *            the target nodes actor reference
	 */
	protected void sendMessageToNodeActor(Message message, ActorRef targetNode) {
		if (targetNode != null) {
			LOG.debug(String.format("Sending %s from %s to %s", message.getClass()
					.getSimpleName(), this.getSelf().toString(), targetNode
					.toString()));
			
			targetNode.tell(message, this.getSelf());
		} else {
			LOG.error(String.format("Target actor was NULL (sender: %s, message: %s)", this.getSender(), message));
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
	protected ActorRef createTaskActionActor(UntypedActorContext context, final NodeParameter nodeParameter) {
		
		ActorRef eventDefinitionActor = context.actorOf(new Props(new UntypedActorFactory() {
			private static final long serialVersionUID = 1L;
			
			public UntypedActor create() {
					return new ActivityActionFactory().createServiceNode(nodeParameter.clientId, nodeParameter.processJaxb, nodeParameter.subProcessesJaxb, nodeParameter.flowNodeJaxb, nodeParameter.sequenceFlowsJaxb);
				}
		}), ActorReferenceService.getActorReferenceString(nodeParameter.getUniqueFlowNodeId() + "-activityAction"));
		return eventDefinitionActor;
	}
	
	/**
	 * Gets the node instance mediator service.
	 *
	 * @return the node instance mediator service
	 */
	protected NodeInstanceMediatorService getNodeInstanceMediatorService() {
		return nodeInstanceMediatorService;
	}

	/**
	 * Sets the node instance mediator service.
	 *
	 * @param nodeInstanceMediatorService the new node instance mediator service
	 */
	protected void setNodeInstanceMediatorService(
			NodeInstanceMediatorService nodeInstanceMediatorService) {
		this.nodeInstanceMediatorService = nodeInstanceMediatorService;
	}
	
}
