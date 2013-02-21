package com.catify.processengine.core.nodes.loops;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;

import akka.actor.ActorRef;

import com.catify.processengine.core.data.dataobjects.DataObjectHandling;
import com.catify.processengine.core.messages.ActivationMessage;
import com.catify.processengine.core.messages.DeactivationMessage;
import com.catify.processengine.core.messages.LoopEndMessage;
import com.catify.processengine.core.messages.TriggerMessage;
import com.catify.processengine.core.nodes.NodeParameter;
import com.catify.processengine.core.services.ExpressionService;

public class MultiInstanceLoopCharacteristicsStrategy extends LoopStrategy {
	
	boolean isSequential;
	
	private Integer loopCardinality;
	
	private Expression completionCondition;
	
	private int _awaitedMessageCount_;
	
	/**
	 * We need the data object ids, to feed the JEXL context with object
	 * instances.
	 */
	protected Set<String> usedDataObjectIds;

	private boolean catching;
	
	public MultiInstanceLoopCharacteristicsStrategy(ActorRef taskWrapper, NodeParameter nodeParameter,
			DataObjectHandling dataObjectHandling, boolean isSequential, String loopCardinality,
			String completionCondition, Set<String> allDataObjectIds) {
		// FIXME: taskWrapper might be redundant because it should be the sender anyway
		super(taskWrapper, nodeParameter.getUniqueProcessId(), nodeParameter.getUniqueFlowNodeId(), dataObjectHandling);
		
		this.isSequential = isSequential;
		
		// create all needed object ids (needed by jexl)
		this.usedDataObjectIds = ExpressionService.evaluateUsedObjects(completionCondition, allDataObjectIds);
		
		// create the loop condition
		this.completionCondition = ExpressionService.createJexlExpression(completionCondition);
		
		// get the loop cardinality (if any)
		Expression cardinalityExpression = ExpressionService.createJexlExpression(loopCardinality);
		this.loopCardinality = this.evaluateCardinality(null, cardinalityExpression);
	}

	@Override
	public void activate(ActivationMessage message) {

		// true if loop should continue
		if (!this.evaluateCompletionCondition(message.getProcessInstanceId())) {
			
			Object dataObject = LoopBehaviorService.loadPayloadFromDataObject(message.getProcessInstanceId(), uniqueProcessId, dataObjectHandling);
			
			if (dataObject instanceof Collection<?>) {
				Collection<?> collection = (Collection<?>) dataObject;
				int i = 0;
				for (Object object : collection) {
					// for each do something
					message.setPayload(object);
					taskAction.tell(message, this.getSelf());
					i++;
				}
				// FIXME: provide implementation to save awaited message count in db
				// if loopCardinality isNotNull then take that instead of collection size
				_awaitedMessageCount_ = i;
			}
			
		// else end loop and activate next nodes via the taskWrapper
		} else {
			if (!catching) {
				taskWrapper.tell(new LoopEndMessage(message.getProcessInstanceId()), this.getSelf());
			}
		}

	}

	@Override
	public void deactivate(DeactivationMessage message) {
		taskAction.tell(message, this.getSelf());
	}

	@Override
	public void trigger(TriggerMessage message) {
		
		Integer loopCounter = new Integer(42); // FIXME: provide method for loop counter retrieval from db
		loopCounter++;
		
		// true if loop should continue
		if (!this.evaluateCompletionCondition(message.getProcessInstanceId())) {
			
			Object dataObject = LoopBehaviorService.loadPayloadFromDataObject(message.getProcessInstanceId(), uniqueProcessId, dataObjectHandling);
			
			if (dataObject instanceof Collection<?>) {
				// create collection from data object
				Collection<?> dataCollection = (Collection<?>) dataObject;
				List<Object> data = new ArrayList<Object>(dataCollection);
				
				// set the payload to the index of the message in process
				data.set(loopCounter, message.getPayload());
			}
			
			// persist changes to the data object
			LoopBehaviorService.savePayloadToDataObject(message.getProcessInstanceId(), message.getPayload(), uniqueProcessId, dataObjectHandling);
			taskAction.tell(message, this.getSelf());
			
			// FIXME: check if this message is the last awaited message for this instance (and its loop)
			// if so, trigger loop end in taskWrapper
			if (loopCounter >= _awaitedMessageCount_) {
				taskWrapper.tell(new LoopEndMessage(message.getProcessInstanceId()), this.getSelf());
			}

		// else end loop and activate next nodes via the taskWrapper
		} else {
			taskWrapper.tell(new LoopEndMessage(message.getProcessInstanceId()), this.getSelf());
		}
	}
	

	/**
	 * Evaluate loop condition.
	 *
	 * @param processInstanceId the process instance id
	 * @param loopCounter the loop counter
	 * @return true, if expression is true (loop should end)
	 */
	private boolean evaluateCompletionCondition(String processInstanceId) {
		// fill the context once and use it for every expression
		JexlContext context = ExpressionService.fillContext(this.usedDataObjectIds, super.dataObjectHandling, this.uniqueProcessId, processInstanceId);

		return ExpressionService.evaluateToBoolean(this.completionCondition, context);
	}
	
	/**
	 * Evaluate loop condition.
	 *
	 * @param processInstanceId the process instance id
	 * @param loopCounter the loop counter
	 * @return true, if expression is true (loop should continue)
	 */
	private Integer evaluateCardinality(String processInstanceId, Expression cardinalityExpression) {
			Object cardinality = ExpressionService.evaluate(cardinalityExpression);
			
			if (cardinality instanceof Integer) {
				return (Integer) cardinality;
			} else {
				return null;
			}
	}

}
