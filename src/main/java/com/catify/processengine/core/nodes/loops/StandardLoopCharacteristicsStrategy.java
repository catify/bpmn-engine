package com.catify.processengine.core.nodes.loops;

import java.math.BigInteger;
import java.util.Set;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;

import akka.actor.ActorRef;

import com.catify.processengine.core.data.dataobjects.DataObjectHandling;
import com.catify.processengine.core.messages.ActivationMessage;
import com.catify.processengine.core.messages.DeactivationMessage;
import com.catify.processengine.core.messages.LoopMessage;
import com.catify.processengine.core.messages.TriggerMessage;
import com.catify.processengine.core.nodes.NodeParameter;
import com.catify.processengine.core.services.ExpressionService;

public class StandardLoopCharacteristicsStrategy extends LoopStrategy {
	
	boolean testBefore;
	
	private Long loopMaximum;
	
	private Expression loopCondition;

	/**
	 * We need the data object ids, to feed the JEXL context with object
	 * instances.
	 */
	protected Set<String> usedDataObjectIds;
	
	public StandardLoopCharacteristicsStrategy(ActorRef taskWrapper, NodeParameter nodeParameter,
			DataObjectHandling dataObjectHandling, boolean testBefore, BigInteger loopMaximum, String loopCondition, Set<String> allDataObjectIds, boolean catching) {	
		// FIXME: taskWrapper might be redundant because it should be the sender anyway
		super(taskWrapper, nodeParameter.getUniqueProcessId(), nodeParameter.getUniqueFlowNodeId(), dataObjectHandling, catching);

		this.testBefore = testBefore; 
		
		if (loopMaximum != null) {
			this.loopMaximum = loopMaximum.longValue();
		}
		
		// create all needed object ids
		this.usedDataObjectIds = ExpressionService.evaluateUsedObjects(loopCondition, allDataObjectIds);
		
		// create JEXL expressions from strings
		if (loopCondition != null) {
			this.loopCondition = ExpressionService.createJexlExpression(loopCondition);
		}
		
		this.activityAction = super.createTaskActionActor(this.getContext(), nodeParameter);
	}

	@Override
	public void activate(ActivationMessage message) {

		if (testBefore) {

			int loopCounter = this.getLoopCount(message);
			
			// true if loop should continue
			if (this.evaluateLoopCondition(message.getProcessInstanceId(), loopCounter)) {
				message.setPayload(LoopBehaviorService.loadPayloadFromDataObject(message.getProcessInstanceId(), uniqueProcessId, dataObjectHandling));
				this.sendMessageToNodeActor(message, this.activityAction);
			// else end loop and activate next nodes via the taskWrapper
			} else {
				if (!this.catching) {
					// this is a throwing task: end the loop to go on end the process
					// (for catching tasks, ending the loop will only be done for trigger or deactivation messages)
					this.sendMessageToNodeActor(new LoopMessage(message.getProcessInstanceId()), this.activityWrapper);
				}
			}
		} else {
			message.setPayload(LoopBehaviorService.loadPayloadFromDataObject(message.getProcessInstanceId(), uniqueProcessId, dataObjectHandling));
			this.sendMessageToNodeActor(message, this.activityAction);
		}

	}

	@Override
	public void deactivate(DeactivationMessage message) {
		this.sendMessageToNodeActor(message, this.activityAction);
	}

	@Override
	public void trigger(TriggerMessage message) {

		if (testBefore) {
			int loopCounter = this.getLoopCount(message);
			
			// true if loop should continue
			if (this.evaluateLoopCondition(message.getProcessInstanceId(), loopCounter)) {
				LoopBehaviorService.savePayloadToDataObject(message.getProcessInstanceId(), message.getPayload(), uniqueProcessId, dataObjectHandling);
				this.sendMessageToNodeActor(message, this.activityAction);
			// else end loop and activate next nodes via the taskWrapper
			} else {
				LoopBehaviorService.savePayloadToDataObject(message.getProcessInstanceId(), message.getPayload(), uniqueProcessId, dataObjectHandling);
				this.sendMessageToNodeActor(new LoopMessage(message.getProcessInstanceId()), this.activityWrapper);
			}
			
		} else {
			LoopBehaviorService.savePayloadToDataObject(message.getProcessInstanceId(), message.getPayload(), uniqueProcessId, dataObjectHandling);
			this.sendMessageToNodeActor(message, this.activityAction);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.catify.processengine.core.nodes.loops.LoopStrategy#loop(com.catify.processengine.core.messages.LoopMessage)
	 */
	@Override
	protected void loop(LoopMessage message) {
		if (!testBefore) {
			int loopCounter = this.getLoopCount(message);
			if (this.evaluateLoopCondition(message.getProcessInstanceId(), loopCounter)) {
				this.sendMessageToNodeActor(new ActivationMessage(message.getProcessInstanceId()), this.getSelf());
			} else {
				this.sendMessageToNodeActor(new LoopMessage(message.getProcessInstanceId()), this.activityWrapper);
			}
		} else {
			this.sendMessageToNodeActor(new ActivationMessage(message.getProcessInstanceId()), this.getSelf());
		}
	}

	/**
	 * Evaluate loop condition.
	 *
	 * @param processInstanceId the process instance id
	 * @param loopCounter the loop counter
	 * @return true, if expression is true (loop should continue)
	 */
	private boolean evaluateLoopCondition(String processInstanceId, int loopCounter) {
		if ((this.loopMaximum != null && loopCounter >= this.loopMaximum) || (this.loopMaximum == null && this.loopCondition == null)) {
			return false;
		} else if (this.loopCondition != null){

				JexlContext context = ExpressionService.fillContext(this.usedDataObjectIds, super.dataObjectHandling, this.uniqueProcessId, processInstanceId, loopCounter);

				return ExpressionService.evaluateToBoolean(this.loopCondition, context);
		} else if (loopCounter < this.loopMaximum) {
			return true;
		} else {
			return false;
		}
	}

}
