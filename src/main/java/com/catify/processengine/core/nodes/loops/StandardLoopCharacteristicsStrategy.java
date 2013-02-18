package com.catify.processengine.core.nodes.loops;

import java.math.BigInteger;
import java.util.Set;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;

import com.catify.processengine.core.data.dataobjects.DataObjectHandling;
import com.catify.processengine.core.messages.ActivationMessage;
import com.catify.processengine.core.messages.DeactivationMessage;
import com.catify.processengine.core.messages.LoopEndMessage;
import com.catify.processengine.core.messages.TriggerMessage;
import com.catify.processengine.core.services.ExpressionService;

import akka.actor.ActorRef;

public class StandardLoopCharacteristicsStrategy extends LoopStrategy {
	
	boolean testBefore;
	
	private Long loopMaximum;
	
	private Expression loopCondition;
	
	/**
	 * We need the data object ids, to feed the JEXL context with object
	 * instances.
	 */
	protected Set<String> usedDataObjectIds;

	private boolean catching;
	
	public StandardLoopCharacteristicsStrategy(ActorRef taskWrapper, ActorRef taskAction, String uniqueProcessId,
			String uniqueFlowNodeId, DataObjectHandling dataObjectHandling, boolean testBefore, BigInteger loopMaximum, String loopCondition, Set<String> allDataObjectIds) {
		// FIXME: taskWrapper might be redundant because it should be the sender anyway
		super(taskWrapper, taskAction, uniqueProcessId, uniqueFlowNodeId, dataObjectHandling);
		
		this.testBefore = testBefore; 
		this.loopMaximum = loopMaximum.longValue();
		
		// create all needed object ids
		this.usedDataObjectIds = ExpressionService.evaluateUsedObjects(loopCondition, allDataObjectIds);
		
		// create JEXL expressions from strings
		this.loopCondition = ExpressionService.createJexlExpression(loopCondition);
	}

	@Override
	public void activate(ActivationMessage message) {
		
		Long loopCounter = new Long(42); // FIXME: provide method for loop counter retrieval from db
		
		if (testBefore) {

			// true if loop should continue
			if (this.evaluateLoopCondition(message.getProcessInstanceId(), loopCounter)) {

				message.setPayload(LoopBehaviorService.loadPayloadFromDataObject(message.getProcessInstanceId(), uniqueProcessId, dataObjectHandling));
				
				taskAction.tell(message, this.getSelf());
			// else end loop and activate next nodes via the taskWrapper
			} else {
				
				if (catching) {
					// this is a sending task, ending the loop will only be done for trigger or deactivation messages
				} else {
					// this is a receiving task, end the loop to go on end the process
					taskWrapper.tell(new LoopEndMessage(message.getProcessInstanceId()), this.getSelf());
				}
			}
			
		} else {
			message.setPayload(LoopBehaviorService.loadPayloadFromDataObject(message.getProcessInstanceId(), uniqueProcessId, dataObjectHandling));
			
			taskAction.tell(message, this.getSelf());
			
			if (!this.evaluateLoopCondition(message.getProcessInstanceId(), loopCounter)) {
				if (catching) {
					// this is a sending task, ending the loop will only be done for trigger or deactivation messages
				} else {
					// this is a receiving task, end the loop
					taskWrapper.tell(new LoopEndMessage(message.getProcessInstanceId()), this.getSelf());
				}
			}
		}
	}

	@Override
	public void deactivate(DeactivationMessage message) {
		// FIXME: synchronous invocation might be safer
		taskAction.tell(message, this.getSelf());
		taskWrapper.tell(new LoopEndMessage(message.getProcessInstanceId()), this.getSelf());
	}

	@Override
	public void trigger(TriggerMessage message) {
		
		Long loopCounter = new Long(42); // FIXME: provide method for loop counter retrieval from db
		
		if (testBefore) {

			// true if loop should continue
			if (this.evaluateLoopCondition(message.getProcessInstanceId(), loopCounter)) {

				LoopBehaviorService.savePayloadToDataObject(message.getProcessInstanceId(), message.getPayload(), uniqueProcessId, dataObjectHandling);
				
				taskAction.tell(message, this.getSelf());
			// else end loop and activate next nodes via the taskWrapper
			} else {
				LoopBehaviorService.savePayloadToDataObject(message.getProcessInstanceId(), message.getPayload(), uniqueProcessId, dataObjectHandling);
			}
			
		} else {
			LoopBehaviorService.savePayloadToDataObject(message.getProcessInstanceId(), message.getPayload(), uniqueProcessId, dataObjectHandling);
			
			taskAction.tell(message, this.getSelf());
			
			if (!this.evaluateLoopCondition(message.getProcessInstanceId(), loopCounter)) {
				taskWrapper.tell(new LoopEndMessage(message.getProcessInstanceId()), this.getSelf());
			}
		}
	}
	

	/**
	 * Evaluate loop condition.
	 *
	 * @param processInstanceId the process instance id
	 * @param loopCounter the loop counter
	 * @return true, if expression is true (loop should continue)
	 */
	private boolean evaluateLoopCondition(String processInstanceId, Long loopCounter) {
		if (loopMaximum != null && loopMaximum > loopCounter) {
			return false;
		} else {
			// fill the context once and use it for every expression
			JexlContext context = ExpressionService.fillContext(this.usedDataObjectIds, super.dataObjectHandling, this.uniqueProcessId, processInstanceId);

			return ExpressionService.evaluateToBoolean(this.loopCondition, context);
		}
	}

}
