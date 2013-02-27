package com.catify.processengine.core.nodes.loops;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;

import com.catify.processengine.core.nodes.NodeFactoryImpl;
import com.catify.processengine.core.nodes.NodeParameter;
import com.catify.processengine.core.processdefinition.jaxb.TActivity;
import com.catify.processengine.core.processdefinition.jaxb.TFlowNode;
import com.catify.processengine.core.processdefinition.jaxb.TMultiInstanceLoopCharacteristics;
import com.catify.processengine.core.processdefinition.jaxb.TReceiveTask;
import com.catify.processengine.core.processdefinition.jaxb.TStandardLoopCharacteristics;
import com.catify.processengine.core.processdefinition.jaxb.TSubProcess;
import com.catify.processengine.core.processdefinition.jaxb.TTask;

/**
 * A factory for creating {@link LoopStrategy} objects.
 */
public class LoopStrategyFactory extends NodeFactoryImpl {
	
	public static final Logger LOG = LoggerFactory
			.getLogger(LoopStrategyFactory.class);

	/**
	 * Creates a new LoopStrategy object.
	 *
	 * @param clientId the client id
	 * @param processJaxb the jaxb process 
	 * @param subProcessesJaxb the jaxb sub processes 
	 * @param taskJaxb the jaxb task 
	 * @param taskAction the task action actorRef
	 * @return the loop strategy
	 */
	public LoopStrategy createLoopStrategy(ActorRef taskWrapper, NodeParameter nodeParameter) {
		
		TActivity taskJaxb = (TActivity) nodeParameter.flowNodeJaxb;

		if (taskJaxb.getLoopCharacteristics() == null || taskJaxb.getLoopCharacteristics().isNil()) {
			return this.getNonLoopStrategy(taskWrapper, nodeParameter);
		} else if (taskJaxb.getLoopCharacteristics().getValue() instanceof TStandardLoopCharacteristics) {
			return this.getStandardLoopCharacteristicsStrategy(taskWrapper, nodeParameter);
		} else if (taskJaxb.getLoopCharacteristics().getValue() instanceof TMultiInstanceLoopCharacteristics) {
			return this.getMultiInstanceLoopCharacteristicsStrategy(taskWrapper, nodeParameter);
		}
		
		else {
			LOG.error("Unsupported loop type detected!");
			return null;
		}
	}

	private LoopStrategy getNonLoopStrategy(ActorRef taskWrapper,
			NodeParameter nodeParameter) {
		
		return new NonLoopStrategy(
				taskWrapper, 
				nodeParameter,
				getDataObjectHandling(nodeParameter.flowNodeJaxb),
				this.checkCatching(nodeParameter.flowNodeJaxb));
	}
	
	private LoopStrategy getStandardLoopCharacteristicsStrategy(
			ActorRef taskWrapper, NodeParameter nodeParameter) {
		
		TTask taskJaxb = (TTask) nodeParameter.flowNodeJaxb;
		TStandardLoopCharacteristics loopCharacteristics = (TStandardLoopCharacteristics) taskJaxb.getLoopCharacteristics().getValue();
		
		String loopCondition = null;
		if (loopCharacteristics.getLoopCondition() != null) {
			loopCondition = loopCharacteristics.getLoopCondition().getContent().get(0).toString();
		}
		
		return new StandardLoopCharacteristicsStrategy(
				taskWrapper, 
				nodeParameter,
				getDataObjectHandling(taskJaxb),
				loopCharacteristics.isTestBefore(), 
				loopCharacteristics.getLoopMaximum(), 
				loopCondition, 
				super.getAllDataObjectIds(nodeParameter.processJaxb, nodeParameter.subProcessesJaxb),
				this.checkCatching(nodeParameter.flowNodeJaxb));
	}
	
	private LoopStrategy getMultiInstanceLoopCharacteristicsStrategy(
			ActorRef taskWrapper, NodeParameter nodeParameter) {
		
		TTask taskJaxb = (TTask) nodeParameter.flowNodeJaxb;
		TMultiInstanceLoopCharacteristics loopCharacteristics = (TMultiInstanceLoopCharacteristics) taskJaxb.getLoopCharacteristics().getValue();
		
		String loopCardinality = null;
		if (loopCharacteristics.getLoopCardinality() != null) {
			loopCardinality = loopCharacteristics.getLoopCardinality().getContent().get(0).toString();
		}
		
		String completionCondition = null;
		if (loopCharacteristics.getCompletionCondition() != null) {
			completionCondition = loopCharacteristics.getCompletionCondition().getContent().get(0).toString();
		}
		
		return new MultiInstanceLoopCharacteristicsStrategy(
				taskWrapper,
				nodeParameter,
				getDataObjectHandling(taskJaxb),
				loopCharacteristics.isIsSequential(),
				loopCardinality,
				completionCondition, 
				super.getAllDataObjectIds(nodeParameter.processJaxb, nodeParameter.subProcessesJaxb),
				this.checkCatching(nodeParameter.flowNodeJaxb));
	}

	/**
	 * Check if the task action is catching/receiving. 
	 *
	 * @param flowNodeJaxb the flow node jaxb
	 * @return true, if task action is catching
	 */
	private boolean checkCatching(TFlowNode flowNodeJaxb) {
		if (flowNodeJaxb instanceof TReceiveTask || flowNodeJaxb instanceof TSubProcess) {
			return true;
		} else {
			return false;
		}
	}
}
