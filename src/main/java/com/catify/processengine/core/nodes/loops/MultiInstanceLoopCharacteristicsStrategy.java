package com.catify.processengine.core.nodes.loops;

import java.util.Collection;
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
	
	public MultiInstanceLoopCharacteristicsStrategy(ActorRef taskWrapper, NodeParameter nodeParameter,
			DataObjectHandling dataObjectHandling, boolean isSequential, String loopCardinality,
			String completionCondition, Set<String> allDataObjectIds, boolean catching) {
		// FIXME: taskWrapper might be redundant because it should be the sender anyway
		super(taskWrapper, nodeParameter.getUniqueProcessId(), nodeParameter.getUniqueFlowNodeId(), dataObjectHandling, catching);
		
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
			
			Object dataObject = LoopBehaviorService.loadPayloadFromDataObject(message.getProcessInstanceId(), this.uniqueProcessId, this.dataObjectHandling);
			
			if (dataObject instanceof Collection<?>) {
				Collection<?> collection = (Collection<?>) dataObject;
				int i = 0;
				for (Object object : collection) {
					// for each do something
					message.setPayload(object);
					this.sendMessageToNodeActor(message, this.activityAction);
					i++;
				}
				// FIXME: provide implementation to save awaited message count in db
				// if loopCardinality isNotNull then take that instead of collection size
				_awaitedMessageCount_ = i;
			}
			
		// else end loop and activate next nodes via the taskWrapper
		} else {
			if (this.catching) {
				this.sendMessageToNodeActor(new LoopMessage(message.getProcessInstanceId()), this.activityWrapper);
			}
		}

	}

	@Override
	public void deactivate(DeactivationMessage message) {
		this.sendMessageToNodeActor(message, this.activityAction);
	}

	@Override
	public void trigger(TriggerMessage message) {
		
		int loopCounter = this.getLoopCounter(message);
		
		// true if loop should continue
		if (!this.evaluateCompletionCondition(message.getProcessInstanceId())) {
			
			if (loopCounter == 0) {
				
			}
			
			Object dataObject = LoopBehaviorService.loadPayloadFromDataObject(message.getProcessInstanceId(), this.uniqueProcessId, this.dataObjectHandling);
			
			if (dataObject instanceof Collection) {
				// create collection from data object
				@SuppressWarnings("unchecked")
				Collection<Object> dataCollection = (Collection<Object>) dataObject;

				dataCollection.add(message.getPayload());
			}
			
			// persist changes to the data object
			LoopBehaviorService.savePayloadToDataObject(message.getProcessInstanceId(), message.getPayload(), this.uniqueProcessId, this.dataObjectHandling);
			this.sendMessageToNodeActor(message, this.activityAction);
			
			// FIXME: check if this message is the last awaited message for this instance (and its loop)
			// if so, trigger loop end in taskWrapper
			if (loopCounter >= _awaitedMessageCount_) {
				this.sendMessageToNodeActor(new LoopMessage(message.getProcessInstanceId()), this.activityWrapper);
			}

		// else end loop and activate next nodes via the taskWrapper
		} else {
			this.sendMessageToNodeActor(new LoopMessage(message.getProcessInstanceId()), this.activityWrapper);
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
