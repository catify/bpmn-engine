package com.catify.processengine.core.nodes.loops;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;

import com.catify.processengine.core.nodes.NodeFactoryImpl;
import com.catify.processengine.core.nodes.NodeParameter;
import com.catify.processengine.core.processdefinition.jaxb.TMultiInstanceLoopCharacteristics;
import com.catify.processengine.core.processdefinition.jaxb.TStandardLoopCharacteristics;
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
		
		TTask taskJaxb = (TTask) nodeParameter.flowNodeJaxb;

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
				getDataObjectHandling(nodeParameter.flowNodeJaxb));
	}
	
	private LoopStrategy getStandardLoopCharacteristicsStrategy(
			ActorRef taskWrapper, NodeParameter nodeParameter) {
		
		TTask taskJaxb = (TTask) nodeParameter.flowNodeJaxb;
		TStandardLoopCharacteristics loopCharacteristics = (TStandardLoopCharacteristics) taskJaxb.getLoopCharacteristics().getValue();
		
		return new StandardLoopCharacteristicsStrategy(
				taskWrapper, 
				nodeParameter,
				getDataObjectHandling(taskJaxb),
				loopCharacteristics.isTestBefore(), 
				loopCharacteristics.getLoopMaximum(), 
				loopCharacteristics.getLoopCondition().getContent().get(0).toString(), 
				super.getAllDataObjectIds(nodeParameter.processJaxb, nodeParameter.subProcessesJaxb));
	}
	
	private LoopStrategy getMultiInstanceLoopCharacteristicsStrategy(
			ActorRef taskWrapper, NodeParameter nodeParameter) {
		
		TTask taskJaxb = (TTask) nodeParameter.flowNodeJaxb;
		TMultiInstanceLoopCharacteristics loopCharacteristics = (TMultiInstanceLoopCharacteristics) taskJaxb.getLoopCharacteristics().getValue();
		
		return new MultiInstanceLoopCharacteristicsStrategy(
				taskWrapper,
				nodeParameter,
				getDataObjectHandling(taskJaxb),
				loopCharacteristics.isIsSequential(),
				loopCharacteristics.getLoopCardinality().getContent().get(0).toString(),
				loopCharacteristics.getCompletionCondition().getContent().get(0).toString(), 
				super.getAllDataObjectIds(nodeParameter.processJaxb, nodeParameter.subProcessesJaxb));
	}


}
