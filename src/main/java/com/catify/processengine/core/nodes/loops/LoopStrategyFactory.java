package com.catify.processengine.core.nodes.loops;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;

import com.catify.processengine.core.data.services.IdService;
import com.catify.processengine.core.nodes.NodeFactoryImpl;
import com.catify.processengine.core.processdefinition.jaxb.TLoopCharacteristics;
import com.catify.processengine.core.processdefinition.jaxb.TMultiInstanceLoopCharacteristics;
import com.catify.processengine.core.processdefinition.jaxb.TProcess;
import com.catify.processengine.core.processdefinition.jaxb.TStandardLoopCharacteristics;
import com.catify.processengine.core.processdefinition.jaxb.TSubProcess;
import com.catify.processengine.core.processdefinition.jaxb.TTask;
import com.catify.processengine.core.services.ActorReferenceService;

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
	public LoopStrategy createLoopStrategy(ActorRef taskWrapper, ActorRef taskAction, String clientId, TProcess processJaxb,
			ArrayList<TSubProcess> subProcessesJaxb, TTask taskJaxb) {

		if (taskJaxb.getLoopCharacteristics().isNil()) {
			return this.getNonLoopStrategy(taskWrapper, taskAction, clientId, processJaxb, subProcessesJaxb, taskJaxb);
		} else if (taskJaxb.getLoopCharacteristics().getValue() instanceof TStandardLoopCharacteristics) {
			return this.getStandardLoopCharacteristicsStrategy(taskWrapper, taskAction, clientId, processJaxb, subProcessesJaxb, taskJaxb);
		} else if (taskJaxb.getLoopCharacteristics().getValue() instanceof TMultiInstanceLoopCharacteristics) {
			return this.getMultiInstanceLoopCharacteristicsStrategy(taskWrapper, taskAction, clientId, processJaxb, subProcessesJaxb, taskJaxb);
		}
		
		else {
			LOG.error("Unsupported loop type detected!");
			return null;
		}
	}
	
	/**
	 * Gets the non loop strategy.
	 *
	 * @param taskWrapper the task wrapper actorRef
	 * @param taskAction the task action actorRef
	 * @param clientId the client id
	 * @param processJaxb the jaxb process
	 * @param subProcessesJaxb the jaxb sub processes
	 * @param taskJaxb the jaxb task
	 * @return the non loop strategy
	 */
	private LoopStrategy getNonLoopStrategy(ActorRef taskWrapper, ActorRef taskAction, String clientId, TProcess processJaxb,
			ArrayList<TSubProcess> subProcessesJaxb, TTask taskJaxb) {
		return new NonLoopStrategy(
				taskWrapper, 
				taskAction, 
				IdService.getUniqueProcessId(clientId, processJaxb), 
				IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb, taskJaxb),
				getDataObjectHandling(taskJaxb));

	}
	
	/**
	 * Gets the standard loop characteristics strategy.
	 *
	 * @param clientId the client id
	 * @param processJaxb the jaxb process 
	 * @param subProcessesJaxb the jaxb sub processes 
	 * @param taskJaxb the jaxb task 
	 * @param taskAction the task action actorRef
	 * @return the standard loop characteristics strategy
	 */
	private LoopStrategy getStandardLoopCharacteristicsStrategy(ActorRef taskWrapper, ActorRef taskAction, String clientId, TProcess processJaxb,
			ArrayList<TSubProcess> subProcessesJaxb, TTask taskJaxb) {

		TStandardLoopCharacteristics loopCharacteristics = (TStandardLoopCharacteristics) taskJaxb.getLoopCharacteristics().getValue();

		return new StandardLoopCharacteristicsStrategy(
				taskWrapper, 
				taskAction, 
				IdService.getUniqueProcessId(clientId, processJaxb), 
				IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb, taskJaxb),
				getDataObjectHandling(taskJaxb),
				loopCharacteristics.isTestBefore(), 
				loopCharacteristics.getLoopMaximum(), 
				loopCharacteristics.getLoopCondition().getContent().get(0).toString(), 
				super.getAllDataObjectIds(processJaxb, subProcessesJaxb));
	}
	
	/**
	 * Gets the multi instance loop characteristics strategy.
	 *
	 * @param clientId the client id
	 * @param processJaxb the jaxb process 
	 * @param subProcessesJaxb the jaxb sub processes 
	 * @param taskJaxb the jaxb task 
	 * @param taskAction the task action actorRef
	 * @return the multi instance loop characteristics strategy
	 */
	private LoopStrategy getMultiInstanceLoopCharacteristicsStrategy(ActorRef taskWrapper, ActorRef taskAction, String clientId, TProcess processJaxb,
			ArrayList<TSubProcess> subProcessesJaxb, TTask taskJaxb) {
		return null;
	}
}
