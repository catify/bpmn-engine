package com.catify.processengine.core.nodes;

import java.util.Date;
import java.util.List;

import akka.actor.ActorRef;

import com.catify.processengine.core.data.dataobjects.DataObjectService;
import com.catify.processengine.core.data.model.NodeInstaceStates;
import com.catify.processengine.core.messages.ActivationMessage;
import com.catify.processengine.core.messages.DeactivationMessage;
import com.catify.processengine.core.messages.Message;
import com.catify.processengine.core.messages.TriggerMessage;
import com.catify.processengine.core.messages.WinningMessage;
import com.catify.processengine.core.nodes.eventdefinition.EventDefinition;
import com.catify.processengine.core.services.NodeInstanceMediatorService;

/**
 * An EbgConnectedCatchEvent is a specialization of the standard catch event
 * node. This is a catch event node which is following an event based gateway
 * node and therefore implements a different behavior compared to the standard
 * catch event:
 * 
 * If a catch event receives a fire message it and waits for the go from the
 * event based gateway until they proceed in the process.
 * 
 * @author chris
 * 
 */
public class EbgConnectedCatchEventNode extends CatchEvent {

	private ActorRef activatingGatewayNode;

	public EbgConnectedCatchEventNode() {
	}

	/**
	 * Instantiates a new catch event node.
	 * 
	 * @param uniqueProcessId
	 *            the process id
	 * @param uniqueFlowNodeId
	 *            the unique flow node id
	 * @param outgoingNodes
	 *            the outgoing nodes
	 * @param nodeInstanceMediatorService
	 *            the node instance service
	 */
	public EbgConnectedCatchEventNode(String uniqueProcessId,
			String uniqueFlowNodeId, EventDefinition eventDefinition,
			List<ActorRef> outgoingNodes, DataObjectService dataObjectHandling) {
		this.setUniqueProcessId(uniqueProcessId);
		this.setUniqueFlowNodeId(uniqueFlowNodeId);
		this.setEventDefinition(eventDefinition);
		this.setOutgoingNodes(outgoingNodes);
		this.setNodeInstanceMediatorService(new NodeInstanceMediatorService(
				uniqueProcessId, uniqueFlowNodeId));
		this.setDataObjectHandling(dataObjectHandling);
	}

	/**
	 * onReceive method of {@linkplain FLowNode} needs to be overridden because
	 * we need to implement a winning message reaction not used in any of the
	 * other event nodes.
	 */
	@Override
	public void onReceive(Object message) {
		if (this.isProcessableInstance((Message) message)) {
			if (message instanceof ActivationMessage) {
				activate((ActivationMessage) message);
			} else if (message instanceof DeactivationMessage) {
				deactivate((DeactivationMessage) message);
			} else if (message instanceof TriggerMessage) {
				trigger((TriggerMessage) message);
			} else if (message instanceof WinningMessage) {
				winning((WinningMessage) message);
			} else {
				unhandled(message);
			}
		}
	}

	@Override
	protected void activate(ActivationMessage message) {
		this.getNodeInstanceMediatorService().setState(
				message.getProcessInstanceId(), NodeInstaceStates.ACTIVE_STATE);
		
		this.getNodeInstanceMediatorService().setNodeInstanceStartTime(message.getProcessInstanceId(), new Date());
		
		this.setActivatingGatewayNode(this.getSender());
		
		this.getNodeInstanceMediatorService().persistChanges();
		
		eventDefinition.acitivate(message);
	}

	@Override
	protected void deactivate(DeactivationMessage message) {
		eventDefinition.deactivate(message);
		
		this.getNodeInstanceMediatorService().setNodeInstanceEndTime(message.getProcessInstanceId(), new Date());
		
		this.getNodeInstanceMediatorService().setState(
				message.getProcessInstanceId(),
				NodeInstaceStates.DEACTIVATED_STATE);
		
		this.getNodeInstanceMediatorService().persistChanges();
	}

	/**
	 * Implements reaction to a TriggerMessage. The node will send a fire
	 * message to the gateway connected to (backwards) and wait for the approval
	 * in form of a winning message.
	 */
	@Override
	protected void trigger(TriggerMessage message) {
		this.getDataObjectHandling().saveObject(this.getUniqueProcessId(), message.getProcessInstanceId(), message.getPayload());
		
		eventDefinition.trigger(message);
		
		this.getNodeInstanceMediatorService().setNodeInstanceEndTime(message.getProcessInstanceId(), new Date());
		
		this.getNodeInstanceMediatorService().setState(
				message.getProcessInstanceId(),
				NodeInstaceStates.PASSED_STATE);
		
		this.getNodeInstanceMediatorService().persistChanges();
		
		this.sendMessageToNodeActor(message, this.getActivatingGatewayNode());
	}

	/**
	 * Reaction to a winning message. If the gateway this node is connected to
	 * reacts with a winning message, the element is allowed to trigger and
	 * activate its following nodes.
	 * 
	 * @param message
	 */
	protected void winning(WinningMessage message) {
		this.getNodeInstanceMediatorService().setState(
				message.getProcessInstanceId(), NodeInstaceStates.PASSED_STATE);
		
		this.getNodeInstanceMediatorService().persistChanges();
		
		this.sendMessageToNodeActors(
				new ActivationMessage(message.getProcessInstanceId()),
				this.getOutgoingNodes());
	}
	
	private ActorRef getActivatingGatewayNode() {
		return activatingGatewayNode;
	}

	private void setActivatingGatewayNode(ActorRef activatingGatewayNode) {
		this.activatingGatewayNode = activatingGatewayNode;
	}
}