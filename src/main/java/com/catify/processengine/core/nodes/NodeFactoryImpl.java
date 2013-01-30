/**
 * *******************************************************
 * Copyright (C) 2013 catify <info@catify.com>
 * *******************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 */
package com.catify.processengine.core.nodes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.bind.JAXBElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import akka.actor.ActorRef;

import com.catify.processengine.core.data.dataobjects.DataObjectIdService;
import com.catify.processengine.core.data.dataobjects.DataObjectService;
import com.catify.processengine.core.data.dataobjects.NoDataObjectSP;
import com.catify.processengine.core.data.services.impl.IdService;
import com.catify.processengine.core.nodes.eventdefinition.EventDefinitionParameter;
import com.catify.processengine.core.processdefinition.jaxb.TCatchEvent;
import com.catify.processengine.core.processdefinition.jaxb.TComplexGateway;
import com.catify.processengine.core.processdefinition.jaxb.TEndEvent;
import com.catify.processengine.core.processdefinition.jaxb.TEventBasedGateway;
import com.catify.processengine.core.processdefinition.jaxb.TExclusiveGateway;
import com.catify.processengine.core.processdefinition.jaxb.TExpression;
import com.catify.processengine.core.processdefinition.jaxb.TFlowElement;
import com.catify.processengine.core.processdefinition.jaxb.TFlowNode;
import com.catify.processengine.core.processdefinition.jaxb.TIntermediateCatchEvent;
import com.catify.processengine.core.processdefinition.jaxb.TIntermediateThrowEvent;
import com.catify.processengine.core.processdefinition.jaxb.TMessageIntegration;
import com.catify.processengine.core.processdefinition.jaxb.TParallelGateway;
import com.catify.processengine.core.processdefinition.jaxb.TProcess;
import com.catify.processengine.core.processdefinition.jaxb.TReceiveTask;
import com.catify.processengine.core.processdefinition.jaxb.TSendTask;
import com.catify.processengine.core.processdefinition.jaxb.TSequenceFlow;
import com.catify.processengine.core.processdefinition.jaxb.TServiceTask;
import com.catify.processengine.core.processdefinition.jaxb.TStartEvent;
import com.catify.processengine.core.processdefinition.jaxb.TSubProcess;
import com.catify.processengine.core.processdefinition.jaxb.services.ExtensionService;
import com.catify.processengine.core.services.ActorReferenceService;

/**
 * A factory for creating akka node objects.
 * 
 * @author christopher köster
 * 
 */
@Repository
public class NodeFactoryImpl implements NodeFactory {

	public static final Logger LOG = LoggerFactory
			.getLogger(NodeFactoryImpl.class);
	
	@Override
	public synchronized FlowElement createServiceNode(String clientId, TProcess processJaxb,  ArrayList<TSubProcess> subProcessesJaxb,
			TFlowNode flowNodeJaxb, List<TSequenceFlow> sequenceFlowsJaxb) {
		// event nodes
		if (flowNodeJaxb.getClass().equals(
				TStartEvent.class)) {
			return this.createStartEventNode(clientId, processJaxb, subProcessesJaxb,
					flowNodeJaxb, sequenceFlowsJaxb);
		} else if (flowNodeJaxb.getClass().equals(
				TIntermediateCatchEvent.class)) {
			return this.createIntermediateCatchEventNode(clientId, 
					processJaxb, subProcessesJaxb, flowNodeJaxb,
					sequenceFlowsJaxb);
		} else if (flowNodeJaxb.getClass().equals(
				TIntermediateThrowEvent.class)) {
			return this.createIntermediateThrowEventNode(clientId, 
					processJaxb, subProcessesJaxb, flowNodeJaxb,
					sequenceFlowsJaxb);
		} else if (flowNodeJaxb.getClass().equals(
				TEndEvent.class)) {
			return this.createEndEventNode(clientId, processJaxb, subProcessesJaxb,
					flowNodeJaxb, sequenceFlowsJaxb);

		// gateways
		} else if (flowNodeJaxb.getClass().equals(
				TComplexGateway.class)) {
			return this.createComplexGatewayNode(clientId, processJaxb, subProcessesJaxb,
					flowNodeJaxb, sequenceFlowsJaxb);
		} else if (flowNodeJaxb.getClass().equals(
				TEventBasedGateway.class)) {
			return this.createEventBasedGatewayNode(clientId, 
					processJaxb, subProcessesJaxb, flowNodeJaxb,
					sequenceFlowsJaxb);
		} else if (flowNodeJaxb.getClass().equals(
				TParallelGateway.class)) {
			return this.createParallelGatewayNode(clientId, processJaxb, subProcessesJaxb,
					flowNodeJaxb, sequenceFlowsJaxb);
		} else if (flowNodeJaxb.getClass().equals(
				TExclusiveGateway.class)) {
			return this.createExclusiveGatewayNode(clientId, processJaxb, subProcessesJaxb,
					flowNodeJaxb, sequenceFlowsJaxb);

		// activities
		} else if (flowNodeJaxb.getClass().equals(
				TReceiveTask.class)) {
			return this.createReceiveTaskNode(clientId, processJaxb, subProcessesJaxb,
					flowNodeJaxb, sequenceFlowsJaxb);
		} else if (flowNodeJaxb.getClass().equals(
				TSendTask.class)) {
			return this.createSendTaskNode(clientId, processJaxb, subProcessesJaxb,
					flowNodeJaxb, sequenceFlowsJaxb);
		} else if (flowNodeJaxb.getClass().equals(
				TServiceTask.class)) {
			return this.createServiceTaskNode(clientId, processJaxb, subProcessesJaxb,
					flowNodeJaxb, sequenceFlowsJaxb);
			
		// sub process nodes (without the included flow nodes)
		} else if (flowNodeJaxb.getClass().equals(
				TSubProcess.class)) {
			return this.createSubProcessNode(clientId, processJaxb, subProcessesJaxb,
					flowNodeJaxb, sequenceFlowsJaxb);

		// nodes that can not be matched
		} else {
			LOG.error(String
					.format("Unimplemented node >>>%s<<< catched while registering akka services",
							flowNodeJaxb.getClass()));
			return null;
		}
	}
	
	/**
	 * Creates a start event node which can be used to create a start event
	 * actor.
	 *
	 * @param clientId the client id
	 * @param processJaxb the jaxb process
	 * @param flowNodeJaxb the flow nodeJaxb
	 * @param sequenceFlowsJaxb the list of jaxb sequence flows of that process
	 * @return the start event node
	 */
	private FlowElement createStartEventNode(String clientId, TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb,
			TFlowNode flowNodeJaxb, List<TSequenceFlow> sequenceFlowsJaxb) {

		final TStartEvent startEventJaxb = (TStartEvent) flowNodeJaxb;

		return new StartEventNode(
				IdService.getUniqueProcessId(clientId, processJaxb),
				IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb,
						startEventJaxb),
				new EventDefinitionParameter(clientId, processJaxb, subProcessesJaxb,flowNodeJaxb), 
				this.getOutgoingActorReferences(clientId, 
						processJaxb, subProcessesJaxb, startEventJaxb, sequenceFlowsJaxb),
				this.getOtherStartNodeActorReferences(clientId, processJaxb, subProcessesJaxb, startEventJaxb,
						sequenceFlowsJaxb),
				this.getParentSubProcessUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb, flowNodeJaxb),
				this.getDataObjectService(flowNodeJaxb));
	}

	/**
	 * Creates a intermediate catch event node which can be used to create an
	 * intermediate catch event actor.
	 *
	 * @param clientId the client id
	 * @param processJaxb the jaxb process
	 * @param flowNodeJaxb the flow nodeJaxb
	 * @param sequenceFlowsJaxb the list of jaxb sequence flows of that process
	 * @return the catch event node
	 */
	private FlowElement createIntermediateCatchEventNode(
			String clientId, TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb, TFlowNode flowNodeJaxb,
			List<TSequenceFlow> sequenceFlowsJaxb) {

		final TCatchEvent intermediateCatchEventJaxb = (TCatchEvent) flowNodeJaxb;

		// check if catch event is following an event based gateway
		boolean ebgConnected = false;
		for (TFlowNode incomingNode : getIncomingFlowNodes(clientId, processJaxb, subProcessesJaxb,
				flowNodeJaxb, sequenceFlowsJaxb)) {
			if (incomingNode instanceof TEventBasedGateway) {
				ebgConnected = true;
				break;
			}
		}
		// if it is following an event based gateway instantiate this special
		// node
		if (ebgConnected) {
			return new EbgConnectedCatchEventNode(
					IdService.getUniqueProcessId(clientId, processJaxb),
					IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb,
							intermediateCatchEventJaxb),
					new EventDefinitionParameter(clientId, processJaxb, subProcessesJaxb,flowNodeJaxb), 
					this.getOutgoingActorReferences(clientId, processJaxb, subProcessesJaxb,
							intermediateCatchEventJaxb, sequenceFlowsJaxb),
					this.getDataObjectService(flowNodeJaxb));
			// else instantiate the standard catch node
		} else {
			return new IntermediateCatchEventNode(
					IdService.getUniqueProcessId(clientId, processJaxb),
					IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb,
							intermediateCatchEventJaxb),
					new EventDefinitionParameter(clientId, processJaxb, subProcessesJaxb,flowNodeJaxb),
					this.getOutgoingActorReferences(clientId, processJaxb, subProcessesJaxb,
							intermediateCatchEventJaxb, sequenceFlowsJaxb),
					this.getDataObjectService(flowNodeJaxb));
		}
	}

	/**
	 * Creates a intermediate throw event node which can be used to create an
	 * intermediate throw event actor.
	 *
	 * @param clientId the client id
	 * @param processJaxb the jaxb process
	 * @param flowNodeJaxb the flow nodeJaxb
	 * @param sequenceFlowsJaxb the list of jaxb sequence flows of that process
	 * @return the throw event node
	 */
	private FlowElement createIntermediateThrowEventNode(
			String clientId, TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb, TFlowNode flowNodeJaxb,
			List<TSequenceFlow> sequenceFlowsJaxb) {

		final TIntermediateThrowEvent intermediateThrowEventJaxb = (TIntermediateThrowEvent) flowNodeJaxb;

		return new IntermediateThrowEventNode(
				IdService.getUniqueProcessId(clientId, processJaxb),
				IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb,
						intermediateThrowEventJaxb),
				new EventDefinitionParameter(clientId, processJaxb, subProcessesJaxb,flowNodeJaxb),
				this.getOutgoingActorReferences(clientId, processJaxb, subProcessesJaxb,
						intermediateThrowEventJaxb, sequenceFlowsJaxb),
				this.getDataObjectService(flowNodeJaxb));
	}

	/**
	 * Creates an end event node which can be used to create an end event actor.
	 *
	 * @param clientId the client id
	 * @param processJaxb the jaxb process
	 * @param flowNodeJaxb the flow nodeJaxb
	 * @param sequenceFlowsJaxb the list of jaxb sequence flows of that process
	 * @return the end event node
	 */
	private FlowElement createEndEventNode(String clientId, TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb,
			TFlowNode flowNodeJaxb, List<TSequenceFlow> sequenceFlowsJaxb) {

		TEndEvent endEventJaxb = (TEndEvent) flowNodeJaxb;
		
		return new EndEventNode(
				IdService.getUniqueProcessId(clientId, processJaxb),
				IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb,
						endEventJaxb),
				new EventDefinitionParameter(clientId, processJaxb, subProcessesJaxb,flowNodeJaxb),
				this.getParentSubProcessActorReference(clientId, 
						processJaxb, subProcessesJaxb, endEventJaxb, sequenceFlowsJaxb),
				this.getDataObjectService(flowNodeJaxb),
				this.getAllDataObjectIds(processJaxb, subProcessesJaxb));
	}
	
	/**
	 * Gets all data object ids of a process (including sub process object ids).
	 *
	 * @param processJaxb the jaxb process
	 * @param subProcessesJaxb the list of parent jaxb sub processes
	 * @return the data object ids
	 */
	private Set<String> getAllDataObjectIds(TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb) {
		
		// only top level end processes need info about the data objects, 
		// because only they will be allowed to delete them
		if (subProcessesJaxb.size() == 0) {
		
			// create a set to only hold unique dataObjectIds (an objectId can be referenced from multiple flow nodes)
			Set<String> dataObjetIds = new HashSet<String>();
			
			dataObjetIds.addAll(DataObjectIdService.getAllDataObjectIds(processJaxb.getFlowElement()));
			
			dataObjetIds.remove(null);
			return dataObjetIds;
		}
			
		return null;
	}

	/**
	 * Creates the complex gateway node.
	 *
	 * @param clientId the client id
	 * @param processJaxb the jaxb process
	 * @param flowNodeJaxb the jaxb complex gateway node
	 * @param sequenceFlowsJaxb the list of jaxb sequence flows of that process
	 * @return the complex gateway node
	 */
	private FlowElement createComplexGatewayNode(
			String clientId, TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb, TFlowNode flowNodeJaxb,
			List<TSequenceFlow> sequenceFlowsJaxb) {

		final TComplexGateway complexGatewayJaxb = (TComplexGateway) flowNodeJaxb;

		return new ComplexGatewayNode(
				IdService.getUniqueProcessId(clientId, processJaxb),
				IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb,
						complexGatewayJaxb), 
				this.getOutgoingActorReferences(clientId, 
						processJaxb, subProcessesJaxb, complexGatewayJaxb, sequenceFlowsJaxb));
	}

	/**
	 * Creates the event based gateway node.
	 *
	 * @param clientId the client id
	 * @param processJaxb the jaxb process
	 * @param flowNodeJaxb the jaxb event based gateway node
	 * @param sequenceFlowsJaxb the list of jaxb sequence flows of that process
	 * @return the event based gateway node
	 */
	private FlowElement createEventBasedGatewayNode(
			String clientId, TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb, TFlowNode flowNodeJaxb,
			List<TSequenceFlow> sequenceFlowsJaxb) {

		final TEventBasedGateway eventBasedGatewayJaxb = (TEventBasedGateway) flowNodeJaxb;

		return new EventBasedGatewayNode(
				IdService.getUniqueProcessId(clientId, processJaxb),
				IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb,
						eventBasedGatewayJaxb), 
				this.getOutgoingActorReferences(clientId, 
						processJaxb, subProcessesJaxb, eventBasedGatewayJaxb,
						sequenceFlowsJaxb));
	}

	/**
	 * Creates the parallel gateway node.
	 *
	 * @param clientId the client id
	 * @param processJaxb the jaxb process
	 * @param flowNodeJaxb the jaxb parallel gateway node
	 * @param sequenceFlowsJaxb the list of jaxb sequence flows of that process
	 * @return the parallel gateway node
	 */
	private FlowElement createParallelGatewayNode(
			String clientId, TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb, TFlowNode flowNodeJaxb,
			List<TSequenceFlow> sequenceFlowsJaxb) {

		final TParallelGateway parallelGatewayJaxb = (TParallelGateway) flowNodeJaxb;

		return new ParallelGatewayNode(
				IdService.getUniqueProcessId(clientId, processJaxb),
				IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb,
						parallelGatewayJaxb), 
				this.getOutgoingActorReferences(clientId, 
						processJaxb, subProcessesJaxb, parallelGatewayJaxb, sequenceFlowsJaxb));
	}
	
	/**
	 * Creates the exlusive fateway node.
	 * 
	 * @param clientId the client id
	 * @param processJaxb the jaxb process
	 * @param subProcessesJaxb the list of parent jaxb sub processes
	 * @param flowNodeJaxb the jaxb exclusive gateway node
	 * @param sequenceFlowsJaxb the list of jaxb sequence flows of that process
	 * @return
	 */
	private FlowElement createExclusiveGatewayNode(String clientId,
			TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb,
			TFlowNode flowNodeJaxb, List<TSequenceFlow> sequenceFlowsJaxb) {
		
		final TExclusiveGateway exclusiveGatewayJaxb = (TExclusiveGateway) flowNodeJaxb;
		
		this.getConditionalExpressionStrings(sequenceFlowsJaxb);
		
		return new ExclusiveGatewayNode(
				IdService.getUniqueProcessId(clientId, processJaxb), //process id
				IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb, exclusiveGatewayJaxb), // node id
				this.getOutgoingActorReferences(clientId, processJaxb, subProcessesJaxb, exclusiveGatewayJaxb, sequenceFlowsJaxb), // outgoing node references
				this.getAllDataObjectIds(processJaxb, subProcessesJaxb), // the used data object ids
				this.getOutgoingActorReferencesAndExpressions(clientId, processJaxb, subProcessesJaxb, flowNodeJaxb, sequenceFlowsJaxb), // all expressions including actor refs
				this.getDefaultOutgoingSequence(clientId, processJaxb, subProcessesJaxb, exclusiveGatewayJaxb), // the default actor ref
				this.getDataObjectService(flowNodeJaxb)); // the data object handler for the service
	}
	
	/**
	 * Gives back the {@link ActorRef} to the default sequence flow. If no default
	 * has been set, the the return value is null.
	 * 
	 * @param clientId
	 * @param processJaxb
	 * @param subProcessesJaxb
	 * @param exclusiveGatewayJaxb
	 * @return
	 */
	private ActorRef getDefaultOutgoingSequence(String clientId,  TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb,
			TExclusiveGateway exclusiveGatewayJaxb) {
		
		Object def = exclusiveGatewayJaxb.getDefault();
		
		if(def != null) {
			TSequenceFlow sequenceFlowJaxb = (TSequenceFlow) def;
			// actor reference
			return new ActorReferenceService().getActorReference(
					IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb,
							(TFlowNode) sequenceFlowJaxb.getTargetRef()));
		}
		return null;
	}

	/**
	 * get conditional expressions
	 * 
	 * @param sequenceFlowsJaxb
	 * @return
	 */
	private Set<String> getConditionalExpressionStrings(
			List<TSequenceFlow> sequenceFlowsJaxb) {

		Set<String> expressions = new TreeSet<String>();
		Iterator<TSequenceFlow> it = sequenceFlowsJaxb.iterator();

		while (it.hasNext()) {
			TSequenceFlow tSequenceFlow = (TSequenceFlow) it.next();
			String expression = this.getConditionalExpressionString(tSequenceFlow);
			if (expression != null) {
				expressions.add(expression);
			}
		}

		return expressions;
	}
	
	/**
	 * get conditional expression
	 * 
	 * @param sequenceFlow
	 * @return
	 */
	private String getConditionalExpressionString(TSequenceFlow sequenceFlow) {
		TExpression expression = sequenceFlow.getConditionExpression();
		if (expression != null) {
			// we assume, that there's only one expression per node
			return (String) expression.getContent().get(0);
		}

		return null;
	}

	/**
	 * Creates a new sub process node.
	 *
	 * @param clientId the client id
	 * @param processJaxb the jaxb process
	 * @param flowNodeJaxb the jaxb parallel gateway node
	 * @param sequenceFlowsJaxb the list of jaxb sequence flows of that process
	 * @return the sub process node
	 */
	private FlowElement createSubProcessNode(String clientId,
			TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb, TFlowNode flowNodeJaxb,
			List<TSequenceFlow> sequenceFlowsJaxb) {
		
		final TSubProcess subProcessJaxb = (TSubProcess) flowNodeJaxb;

		return new SubProcessNode(
				IdService.getUniqueProcessId(clientId, processJaxb),
				IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb,
						subProcessJaxb), 
				this.getOutgoingActorReferences(clientId, 
						processJaxb, subProcessesJaxb, subProcessJaxb, sequenceFlowsJaxb),
				this.getEmbeddedStartNodeActorReferences(clientId, processJaxb, subProcessesJaxb, flowNodeJaxb, sequenceFlowsJaxb),
				this.getEmbeddedNodeActorReferences(clientId, processJaxb, subProcessesJaxb, flowNodeJaxb, sequenceFlowsJaxb));
	}

	/**
	 * Creates the send task node.
	 *
	 * @param clientId the client id
	 * @param processJaxb the jaxb process
	 * @param flowNodeJaxb the jaxb parallel gateway node
	 * @param sequenceFlowsJaxb the list of jaxb sequence flows of that process
	 * @return the send task node
	 */
	private FlowElement createSendTaskNode(String clientId, TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb,
			TFlowNode flowNodeJaxb, List<TSequenceFlow> sequenceFlowsJaxb) {

		final TSendTask sendTaskJaxb = (TSendTask) flowNodeJaxb;

		return new SendTaskNode(
				IdService.getUniqueProcessId(clientId, processJaxb),
				IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb,
						sendTaskJaxb), 
				this.getOutgoingActorReferences(clientId, 
						processJaxb, subProcessesJaxb, sendTaskJaxb, sequenceFlowsJaxb),
				ActorReferenceService.getActorReferenceString(
								IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb,
										flowNodeJaxb)),  
				new EventDefinitionParameter(clientId, processJaxb, subProcessesJaxb,flowNodeJaxb), 
				this.getDataObjectService(flowNodeJaxb));
	}

	/**
	 * Creates the receive task node.
	 *
	 * @param clientId the client id
	 * @param processJaxb the jaxb process
	 * @param flowNodeJaxb the jaxb parallel gateway node
	 * @param sequenceFlowsJaxb the list of jaxb sequence flows of that process
	 * @return the send task node
	 */
	private FlowElement createReceiveTaskNode(String clientId, TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb,
			TFlowNode flowNodeJaxb, List<TSequenceFlow> sequenceFlowsJaxb) {

		final TReceiveTask receiveTaskJaxb = (TReceiveTask) flowNodeJaxb;

		return new ReceiveTaskNode(
				IdService.getUniqueProcessId(clientId, processJaxb),
				IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb,
						receiveTaskJaxb), 
				this.getOutgoingActorReferences(clientId, 
						processJaxb, subProcessesJaxb, receiveTaskJaxb, sequenceFlowsJaxb),
				ActorReferenceService.getActorReferenceString(
								IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb,
										flowNodeJaxb)), 
				new EventDefinitionParameter(clientId, processJaxb, subProcessesJaxb,flowNodeJaxb),
				this.getDataObjectService(flowNodeJaxb));
	}

	/**
	 * Creates the supervising service task node.
	 *
	 * @param clientId the client id
	 * @param processJaxb the jaxb process
	 * @param flowNodeJaxb the jaxb parallel gateway node
	 * @param sequenceFlowsJaxb the list of jaxb sequence flows of that process
	 * @return the service task node
	 */
	private FlowElement createServiceTaskNode(String clientId, TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb,
			TFlowNode flowNodeJaxb, List<TSequenceFlow> sequenceFlowsJaxb) {

		final TServiceTask serviceTaskJaxb = (TServiceTask) flowNodeJaxb;

		TMessageIntegration messageIntegration = ExtensionService.getTMessageIntegration(flowNodeJaxb);
		
		return new ServiceTaskNode(
				IdService.getUniqueProcessId(clientId, processJaxb),
				IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb,
						serviceTaskJaxb), 
				this.getOutgoingActorReferences(clientId, 
						processJaxb, subProcessesJaxb, serviceTaskJaxb, sequenceFlowsJaxb),
				messageIntegration,
				this.getDataObjectService(flowNodeJaxb));
	}

	/**
	 * Extracts the information which nodes are incoming to a given node and
	 * returns the actor references to that nodes. This is needed, because
	 * sequence flows are not modeled as actors at the moment (because there is
	 * no need for them).
	 *
	 * @param clientId the client id
	 * @param processJaxb the processJaxb
	 * @param flowNodeJaxb the flow nodeJaxb
	 * @param sequenceFlowsJaxb the sequence flowsJaxb
	 * @return a list of strings of the incoming actor references
	 */
	private List<TFlowNode> getIncomingFlowNodes(String clientId, TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb,
			TFlowNode flowNodeJaxb, List<TSequenceFlow> sequenceFlowsJaxb) {

		List<TFlowNode> incomingingNodes = new ArrayList<TFlowNode>();

		// check the sequence flows if they connect the given flow node to any
		// other flow node
		for (TSequenceFlow sequenceFlowJaxb : sequenceFlowsJaxb) {
			/*
			 * if the provided flow node equals the target of a sequence flow,
			 * add it to the incoming nodes list as a actor reference string
			 * (the actor reference object might not be initialized at this
			 * point)
			 */
			if (sequenceFlowJaxb.getTargetRef().equals(flowNodeJaxb)) {
				/*
				 * bpmn allows flow nodes to be connected to other objects (eg.
				 * messages) we do not want to connect these as they are
				 * handeled by other components of the engine
				 */
				if (sequenceFlowJaxb.getSourceRef() instanceof TFlowNode) {
					incomingingNodes.add((TFlowNode) sequenceFlowJaxb
							.getSourceRef());
				}
			}
		}
		return incomingingNodes;
	}

	/**
	 * Extracts the information which nodes follow to a given node and returns
	 * the actor references to that nodes. This is needed, because sequence
	 * flows are not modeled as actors at the moment (because there is no need
	 * for them).
	 *
	 * @param clientId the client id
	 * @param processJaxb the processJaxb
	 * @param flowNodeJaxb the flow nodeJaxb
	 * @param sequenceFlowsJaxb the sequence flowsJaxb
	 * @return a list of strings of the outgoing actor references
	 */
	private List<ActorRef> getOutgoingActorReferences( 
			String clientId, TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb, TFlowNode flowNodeJaxb,
			List<TSequenceFlow> sequenceFlowsJaxb) {

		List<ActorRef> outgoingNodes = new ArrayList<ActorRef>();
		// build the actorRef and expression pairs
		Map<ActorRef, String> outgoingActorReferencesAndExpressions = this.getOutgoingActorReferencesAndExpressions(clientId, processJaxb, subProcessesJaxb, flowNodeJaxb, sequenceFlowsJaxb);
		// we'll only need the keys (actor refs)
		outgoingNodes.addAll(outgoingActorReferencesAndExpressions.keySet());
		return outgoingNodes;
	}
	
	/**
	 * 
	 * 
	 * @param clientId
	 * @param processJaxb
	 * @param subProcessesJaxb
	 * @param flowNodeJaxb
	 * @param sequenceFlowsJaxb
	 * @return
	 */
	private Map<ActorRef, String> getOutgoingActorReferencesAndExpressions(String clientId, TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb, TFlowNode flowNodeJaxb,
			List<TSequenceFlow> sequenceFlowsJaxb) {
		Map<ActorRef, String> outRefs = new TreeMap<ActorRef, String>();
		
		// check the sequence flows if they connect the given flow node to any
		// other flow node
		for (TSequenceFlow sequenceFlowJaxb : sequenceFlowsJaxb) {
			/*
			 * if the provided flow node equals the source of a sequence flow,
			 * add it to the outgoing nodes list as a actor reference string
			 * (the actor reference object might not be initialized at this
			 * point)
			 */
			if (sequenceFlowJaxb.getSourceRef().equals(flowNodeJaxb)) {
				/*
				 * bpmn allows flow nodes to be connected to other objects (eg.
				 * messages) we do not want to connect these as they are
				 * handeled by other components of the engine
				 */
				if (sequenceFlowJaxb.getTargetRef() instanceof TFlowNode) {
					
					// actor reference
					ActorRef actorRef = new ActorReferenceService().getActorReference(
							IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb,
									(TFlowNode) sequenceFlowJaxb.getTargetRef()));
					
					// conditional expression
					String expressionString = this.getConditionalExpressionString(sequenceFlowJaxb);
					
					outRefs.put(actorRef, expressionString);
				}

			}
		}
		return outRefs;
	}
	
	/**
	 * Extracts the information of the embedding/parent sub process node's ActorRef. Used for Start and End Events which are logically connected to their
	 * embedding sub process nodes.
	 *
	 * @param clientId the client id
	 * @param processJaxb the processJaxb
	 * @param subProcessesJaxb the sub processes jaxb
	 * @param flowNodeJaxb the flow nodeJaxb
	 * @param sequenceFlowsJaxb the sequence flowsJaxb
	 * @return the ActorRef of the parent Sub Process or null if there is none
	 */
	private ActorRef getParentSubProcessActorReference( 
			String clientId, TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb, TFlowNode flowNodeJaxb,
			List<TSequenceFlow> sequenceFlowsJaxb) {

		String subProcessUinqueFlowNodeId = getParentSubProcessUniqueFlowNodeId(
				clientId, processJaxb, subProcessesJaxb, flowNodeJaxb);

		if (subProcessUinqueFlowNodeId != null) {
			return new ActorReferenceService().getActorReference(subProcessUinqueFlowNodeId);
		} else {
			return null;
		}
	}

	/**
	 * Extracts the information of the embedding/parent sub process node's uniqueFlowNodeId. Used for Start and End Events which are logically connected to their
	 * embedding sub process nodes.
	 *
	 * @param clientId the client id
	 * @param processJaxb the process jaxb
	 * @param subProcessesJaxb the sub processes jaxb
	 * @param flowNodeJaxb the flow node jaxb
	 * @return the unique flow node id of the parent sub process or null if there is none
	 */
	private String getParentSubProcessUniqueFlowNodeId(String clientId,
			TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb,
			TFlowNode flowNodeJaxb) {
		ArrayList<TSubProcess> localSubProcessesJaxb = new ArrayList<TSubProcess>(subProcessesJaxb);

		// check if the current end event node is a embedded in a sub process
		if (localSubProcessesJaxb.size() > 0) {
			// we only check the last sub process, because this can only be the sub process we are looking for
			TSubProcess subProcessJaxb = localSubProcessesJaxb.get(localSubProcessesJaxb.size()-1);
			for (JAXBElement<? extends TFlowElement> flowElementJaxb : subProcessJaxb.getFlowElement()) {
				// if the given end event node has been found in this sub process this is an embedded end event 
				if (flowElementJaxb.getValue().getId().equals(flowNodeJaxb.getId())) {
					// remove the current sub process from the list of sub processes to be able to get its actor reference
					localSubProcessesJaxb.remove(subProcessJaxb);
					return IdService.getUniqueFlowNodeId(clientId, processJaxb, localSubProcessesJaxb, subProcessJaxb);
				}
			}
		}
		
		// return null if there is no embedding/parent sub process
		return null;
	}

	/**
	 * Extracts the information which other start nodes are in a given process
	 * and returns the actor references to that nodes. This info is needed for
	 * process instantiation.
	 *
	 * @param clientId the client id
	 * @param processJaxb the processJaxb
	 * @param flowNodeJaxb the flow nodeJaxb
	 * @param sequenceFlowsJaxb the sequence flowsJaxb
	 * @return a list of strings of the other start node actor references
	 */
	private List<ActorRef> getOtherStartNodeActorReferences(
			String clientId, TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb, TFlowNode flowNodeJaxb,
			List<TSequenceFlow> sequenceFlowsJaxb) {

		List<ActorRef> otherStartNodes = new ArrayList<ActorRef>();

		// check the sequence flows for if there are any other start nodes in
		// this process
		for (TSequenceFlow sequenceFlowJaxb : sequenceFlowsJaxb) {

			// if this is a start event...
			if ((sequenceFlowJaxb.getSourceRef().getClass()
					.equals(TStartEvent.class))
			// ... but not the start event, that is initialized ...
					&& (!((TFlowNode) sequenceFlowJaxb.getSourceRef()).getId()
							.equals(flowNodeJaxb.getId()))) {

				/*
				 * ...add it to the outgoing nodes list as an actor reference
				 * string. (the actor reference object might not be initialized
				 * at this point)
				 */
				otherStartNodes.add(new ActorReferenceService().getActorReference(
						IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb,
								(TFlowNode) sequenceFlowJaxb.getSourceRef())));

			}
		}
		return otherStartNodes;
	}
	
	/**
	 * Extracts the information of the embedded start nodes that are in a given process (level)
	 * and returns the actor references to that nodes. This info is needed by the sub process.
	 *
	 * @param clientId the client id
	 * @param processJaxb the processJaxb
	 * @param flowNodeJaxb the flow nodeJaxb (which must be a TSubProcess)
	 * @param sequenceFlowsJaxb the sequence flowsJaxb
	 * @return a list of strings of the other start node actor references
	 */
	private List<ActorRef> getEmbeddedStartNodeActorReferences(
			String clientId, TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb, TFlowNode flowNodeJaxb,
			List<TSequenceFlow> sequenceFlowsJaxb) {

		List<ActorRef> embeddedStartNodes = new ArrayList<ActorRef>();
		
		// the current sub process is the parent process of the embedded nodes, 
		// so we need to add it to the list of parent sub processes in this methods scope
		ArrayList<TSubProcess> subProcesses = new ArrayList<TSubProcess>(subProcessesJaxb);
		subProcesses.add((TSubProcess) flowNodeJaxb);
		
		// extract StartEvents from SubProcess
		for (JAXBElement<? extends TFlowElement> flowElementJaxb : ((TSubProcess) flowNodeJaxb).getFlowElement()) {
				if (flowElementJaxb.getValue() instanceof TStartEvent) {
					TStartEvent startEventJaxb = (TStartEvent) flowElementJaxb.getValue();
					embeddedStartNodes.add(new ActorReferenceService()
						.getActorReference(IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcesses,
							startEventJaxb)));
				}
		}
		
		return embeddedStartNodes;
	}
	
	/**
	 * Extracts the information of the embedded start nodes that are in a given process (level)
	 * and returns the actor references to that nodes. This info is needed by the sub process.
	 *
	 * @param clientId the client id
	 * @param processJaxb the processJaxb
	 * @param flowNodeJaxb the flow nodeJaxb
	 * @param sequenceFlowsJaxb the sequence flowsJaxb
	 * @return a list of strings of the other start node actor references
	 */
	private List<ActorRef> getEmbeddedNodeActorReferences(
			String clientId, TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb, TFlowNode flowNodeJaxb,
			List<TSequenceFlow> sequenceFlowsJaxb) {

		List<ActorRef> embeddedFlowNodes = new ArrayList<ActorRef>();
			
		// extract StartEvents from SubProcess
		for (JAXBElement<? extends TFlowElement> flowElementJaxb : ((TSubProcess) flowNodeJaxb).getFlowElement()) {
				if (flowElementJaxb.getValue() instanceof TFlowNode) {
					TFlowNode embeddedFlowNodeJaxb = (TFlowNode) flowElementJaxb.getValue();
					embeddedFlowNodes.add(new ActorReferenceService()
						.getActorReference(IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb,
								embeddedFlowNodeJaxb)));
				}
		}
		
		return embeddedFlowNodes;
	}
	
	private DataObjectService getDataObjectService(TFlowNode flowNodeJaxb) {

		Map<String, String> dataObjectIds = DataObjectIdService.getDataObjectIds(flowNodeJaxb);
		
		// only create a DataObjectService with a data object service provider if there is at least on object id
		if (dataObjectIds.size() > 0) {
			LOG.debug("Node factory appending data object service provider");
			return new DataObjectService(dataObjectIds.get(DataObjectIdService.DATAINPUTOBJECTID), dataObjectIds.get(DataObjectIdService.DATAOUTPUTOBJECTID));
		} 
		// otherwise provide a dummy DataObjectService
		else {
			return new DataObjectService(new NoDataObjectSP());
		}
	}


	
// currently unsused methods (left if needed later)
	
//	/**
//	 * Extracts the information which nodes are incoming to a given node and
//	 * returns the actor references to that nodes. This is needed, because
//	 * sequence flows are not modeled as actors at the moment (because there is
//	 * no need for them).
//	 *
//	 * @param clientId the client id
//	 * @param processJaxb the processJaxb
//	 * @param flowNodeJaxb the flow nodeJaxb
//	 * @param sequenceFlowsJaxb the sequence flowsJaxb
//	 * @return a list of strings of the incoming actor references
//	 */
//	private List<ActorRef> getIncomingActorReferences(
//			String clientId, TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb, TFlowNode flowNodeJaxb,
//			List<TSequenceFlow> sequenceFlowsJaxb) {
//
//		List<ActorRef> incomingingNodes = new ArrayList<ActorRef>();
//
//		// check the sequence flows if they connect the given flow node to any
//		// other flow node
//		for (TSequenceFlow sequenceFlowJaxb : sequenceFlowsJaxb) {
//			/*
//			 * if the provided flow node equals the target of a sequence flow,
//			 * add it to the incoming nodes list as a actor reference string
//			 * (the actor reference object might not be initialized at this
//			 * point)
//			 */
//			if (sequenceFlowJaxb.getTargetRef().equals(flowNodeJaxb)) {
//				/*
//				 * bpmn allows flow nodes to be connected to other objects (eg.
//				 * messages) we do not want to connect these as they are
//				 * handeled by other components of the engine
//				 */
//				if (sequenceFlowJaxb.getSourceRef() instanceof TFlowNode) {
//					incomingingNodes.add(new ActorReferenceService()
//							.getActorReference(
//									IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb,
//											(TFlowNode) sequenceFlowJaxb.getSourceRef())));
//				}
//			}
//		}
//		return incomingingNodes;
//	}
}
