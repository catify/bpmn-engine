package com.catify.processengine.core.nodes;

import com.catify.processengine.core.messages.ActivationMessage;
import com.catify.processengine.core.messages.DeactivationMessage;
import com.catify.processengine.core.messages.TriggerMessage;
import com.catify.processengine.core.nodes.loops.LoopTypeStrategy;
import com.catify.processengine.core.services.NodeInstanceMediatorService;

/**
 * The Class LoopTaskWrapper uses a strategy pattern to wrap the looping behavior around the bpmn tasks. 
 * The {@link NodeFactory} will set the context of {@link LoopTypeStrategy} and {@link Task} based on the bpmn process definition.
 */
public class LoopTaskWrapper extends Task {

/** The loop type strategy which binds the {@link LoopTypeStrategy}) implementation for {@link ActivationMessage}. */
private LoopTypeStrategy typeStrategyActivate;

/** The loop type strategy which binds the {@link LoopTypeStrategy}) implementation for {@link DeactivationMessage}. */
private LoopTypeStrategy typeStrategyDeactivate;

/** The loop type strategy which binds the {@link LoopTypeStrategy}) implementation for {@link TriggerMessage}. */
private LoopTypeStrategy typeStrategyTrigger;

/**
 * Instantiates a new loop task wrapper.
 *
 * @param typeStrategyActivate the type strategy for {@link ActivationMessage}
 * @param typeStrategyDeactivate the type strategy for {@link DeactivationMessage}
 * @param typeStrategyTrigger the type strategy for {@link TriggerMessage}
 */
public LoopTaskWrapper(String uniqueProcessId, String uniqueFlowNodeId, LoopTypeStrategy typeStrategyActivate,
		LoopTypeStrategy typeStrategyDeactivate,
		LoopTypeStrategy typeStrategyTrigger) {
	super();
	
	this.setUniqueProcessId(uniqueProcessId);
	this.setUniqueFlowNodeId(uniqueFlowNodeId);
	this.setNodeInstanceMediatorService(new NodeInstanceMediatorService(
			uniqueProcessId, uniqueFlowNodeId));
	
	this.typeStrategyActivate = typeStrategyActivate;
	this.typeStrategyDeactivate = typeStrategyDeactivate;
	this.typeStrategyTrigger = typeStrategyTrigger;
}

@Override
protected void activate(ActivationMessage message) {
	typeStrategyActivate.activate(message);
}

@Override
protected void deactivate(DeactivationMessage message) {
	typeStrategyDeactivate.deactivate(message);
}

@Override
protected void trigger(TriggerMessage message) {
	typeStrategyTrigger.trigger(message);
}

/**
 * Gets the type strategy.
 *
 * @return the type strategy
 */
public LoopTypeStrategy getTypeStrategy() {
	return typeStrategyActivate;
}

/**
 * Sets the type strategy.
 *
 * @param typeStrategy the new type strategy
 */
public void setTypeStrategy(LoopTypeStrategy typeStrategy) {
	this.typeStrategyActivate = typeStrategy;
}

}
