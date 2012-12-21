package com.catify.processengine.core.nodes;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.UntypedActor;

import com.catify.processengine.core.data.model.NodeInstaceStates;
import com.catify.processengine.core.messages.ActivationMessage;
import com.catify.processengine.core.messages.DeactivationMessage;
import com.catify.processengine.core.messages.Message;
import com.catify.processengine.core.messages.TriggerMessage;
import com.catify.processengine.core.services.NodeInstanceMediatorService;

/**
 * 
 * Base class for all execution elements in a process (events, activities and
 * gateways). Provides the akka onReceive() method as a neo4jTemplate. New elements
 * therefore only need to implement the abstract methods activate(),
 * deactivate() and fire() in order to be valid flow elements. See GoF 'neo4jTemplate
 * method pattern'. There are three main message types:
 * {@link ActivationMessage}, {@link TriggerMessage},
 * {@link DeactivationMessage}.
 * 
 * @author chris
 * 
 */
@Configurable
public abstract class FlowElement extends UntypedActor {

	static final Logger LOG = LoggerFactory.getLogger(FlowElement.class);
	
	@Autowired
	private ActorSystem actorSystem;

	/** The unique client id. */
	private String uniqueClientId;
	
	/** The unique process id. 
	 * @see com.catify.processengine.core.data.model.entities.ProcessNode#uniqueProcessId */
	private String uniqueProcessId;
	
	/** The unique flow node id. 
	 * @see com.catify.processengine.core.data.model.entities.FlowNode#uniqueFlowNodeId */
	private String uniqueFlowNodeId;

	/** The incoming node references. */
	private List<ActorRef> incomingNodes;
	
	/** The outgoing node references. */
	private List<ActorRef> outgoingNodes;

	/** The node instance mediator service. */
	private NodeInstanceMediatorService nodeInstanceMediatorService;

	/**
	 * neo4jTemplate method for reacting to the three possible message types:.
	 *
	 * @param message the message
	 * {@link ActivationMessage}, {@link DeactivationMessage},
	 * {@link TriggerMessage}. This method should not be overridden by
	 * implementing classes.
	 */
	public void onReceive(Object message) {
		LOG.debug(String.format("%s received %s", this.getSelf(), message
				.getClass().getSimpleName()));
		if (this.isProcessableInstance((Message) message)) {
			if (message instanceof ActivationMessage) {
				activate((ActivationMessage) message);
			} else if (message instanceof DeactivationMessage) {
				deactivate((DeactivationMessage) message);
			} else if (message instanceof TriggerMessage) {
				trigger((TriggerMessage) message);
			} else {
				unhandled(message);
			}
		}
	}

	/**
	 * Implements reaction to an {@link ActivationMessage}.
	 *
	 * @param message the message
	 */
	protected abstract void activate(ActivationMessage message) ;

	/**
	 * Implements reaction to a {@link DeactivationMessage}.
	 *
	 * @param message the message
	 */
	protected abstract void deactivate(DeactivationMessage message);

	/**
	 * Implements reaction to a {@link TriggerMessage}.
	 *
	 * @param message the message
	 */
	protected abstract void trigger(TriggerMessage message);

	/**
	 * Checks if this node instance is a processable instance. Instances that have
	 * been set to other states than {@link NodeInstaceStates.INACTIVE_STATE} or
	 *
	 * @param message the message received
	 * @return true, if this is an active instance, false if not
	 * {@link NodeInstaceStates.ACTIVE_STATE} can not be altered afterwards.
	 */
	protected boolean isProcessableInstance(Message message) {
		// if the node checked is an uninitialized (start) node, consider it processable (as it has no saved state yet)
		if (!this.nodeInstanceMediatorService.isInitialized() || message.getProcessInstanceId()==null) {
			return true;} 
		else {
			String nodeInstanceState = this.nodeInstanceMediatorService
					.getState(message.getProcessInstanceId());
			// if the node checked is in an inactive or active state consider it processable
			if (nodeInstanceState.equals(NodeInstaceStates.INACTIVE_STATE)
					|| nodeInstanceState.equals(NodeInstaceStates.ACTIVE_STATE)) {
				return true;
			// if the node checked is in any other state (like deactivated or passed) consider it done.
			} else {
				LOG.debug(String
						.format("isActiveInstance-sanity-check failed: %s with instance id %s is already at state %s, not processing %s",
								this.getClass().getSimpleName(), message.getProcessInstanceId(),
								nodeInstanceState, message.getClass().getSimpleName()));
				return false;
			}
		}
	}
	
	/**
	 * Send a message object to a node actor.
	 * 
	 * @param message
	 *            the message to send
	 * @param targetNode
	 *            the target nodes actor reference
	 */
	protected void sendMessageToNodeActor(Message message, ActorRef targetNode) {
		LOG.debug(String.format("Sending %s from %s to %s", message.getClass()
				.getSimpleName(), this.getSelf().toString(), targetNode
				.toString()));
		
		// FIXME: this is a bugfix, because messages are not properly sent without
		// prior calling of the actorSystem
		String ars = targetNode.path().toString();
		ActorRef ar = actorSystem.actorFor(ars);
		ar.tell(message, this.getSelf());
		
//		targetNode.tell(message, this.getSelf());
	}
	
	/**
	 * Send a message object to a list of node actors.
	 * 
	 * @param message
	 *            the message to send
	 * @param targetNodes
	 *            the target nodes as a list of actor references
	 */
	protected void sendMessageToNodeActors(Message message,
			List<ActorRef> targetNodes) {
		for (ActorRef actorRef : targetNodes) {
			LOG.debug(String.format("Sending %s from %s to %s", message
					.getClass().getSimpleName(), this.getSelf().toString(),
					actorRef.toString()));
			
			// FIXME: this is a bugfix, because messages are not properly sent without
			// prior calling of the actorSystem
			String ars = actorRef.path().toString();
			ActorRef ar = actorSystem.actorFor(ars);
			ar.tell(message, this.getSelf());
			
//			actorRef.tell(message, this.getSelf());
		}
	}
	
	/**
	 * Gets the unique client id.
	 *
	 * @return the unique client id
	 */
	protected String getUniqueClientId() {
		return uniqueClientId;
	}

	/**
	 * Sets the unique client id.
	 *
	 * @param uniqueClientId the new unique client id
	 */
	protected void setUniqueClientId(String uniqueClientId) {
		this.uniqueClientId = uniqueClientId;
	}

	/**
	 * Gets the unique process id.
	 *
	 * @return the unique process id
	 */
	protected String getUniqueProcessId() {
		return uniqueProcessId;
	}

	/**
	 * Sets the unique process id.
	 *
	 * @param uniqueProcessId the new unique process id
	 */
	protected void setUniqueProcessId(String uniqueProcessId) {
		this.uniqueProcessId = uniqueProcessId;
	}

	/**
	 * Gets the unique flow node id.
	 *
	 * @return the unique flow node id
	 */
	protected String getUniqueFlowNodeId() {
		return uniqueFlowNodeId;
	}

	/**
	 * Sets the unique flow node id.
	 *
	 * @param uniqueFlowNodeId the new unique flow node id
	 */
	protected void setUniqueFlowNodeId(String uniqueFlowNodeId) {
		this.uniqueFlowNodeId = uniqueFlowNodeId;
	}

	/**
	 * Gets the incoming nodes.
	 *
	 * @return the incoming nodes
	 */
	protected List<ActorRef> getIncomingNodes() {
		return incomingNodes;
	}

	/**
	 * Sets the incoming nodes.
	 *
	 * @param incomingNodes the new incoming nodes
	 */
	protected void setIncomingNodes(List<ActorRef> incomingNodes) {
		this.incomingNodes = incomingNodes;
	}

	/**
	 * Gets the outgoing nodes.
	 *
	 * @return the outgoing nodes
	 */
	protected List<ActorRef> getOutgoingNodes() {
		return outgoingNodes;
	}

	/**
	 * Sets the outgoing nodes.
	 *
	 * @param outgoingNodes the new outgoing nodes
	 */
	protected void setOutgoingNodes(List<ActorRef> outgoingNodes) {
		this.outgoingNodes = outgoingNodes;
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
	

	protected ActorSystem getActorSystem() {
		return actorSystem;
	}

	protected void setActorSystem(ActorSystem actorSystem) {
		this.actorSystem = actorSystem;
	}
}
