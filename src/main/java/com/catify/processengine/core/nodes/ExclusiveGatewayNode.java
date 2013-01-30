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
package com.catify.processengine.core.nodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;

import com.catify.processengine.core.data.dataobjects.DataObjectService;
import com.catify.processengine.core.messages.ActivationMessage;
import com.catify.processengine.core.messages.DeactivationMessage;
import com.catify.processengine.core.messages.Message;
import com.catify.processengine.core.messages.TriggerMessage;
import com.catify.processengine.core.services.ExpressionService;
import com.catify.processengine.core.services.NodeInstanceMediatorService;
import com.catify.processengine.core.util.GatewayUtil;

/**
 * Implements the exclusive gateway. See 13.3.2 of BPMN 2.0 specification.
 * 
 * @author christopher köster
 * 
 */
public class ExclusiveGatewayNode extends FlowElement implements NOfMService {

	public static final Logger LOG = LoggerFactory.getLogger(ExclusiveGatewayNode.class);
	
	/**
	 * We need the data object ids, to feed the JEXL context with object
	 * instances.
	 */
	protected Set<String> usedDataObjectIds;
	
	/**
	 * We need a ordered map of conditional expression strings to fire each
	 * expression. The first one, that returnes 'true' wins.
	 */
	protected Map<ActorRef, Expression> conditionalExpressionStrings;
	
	/**
	 * Holds all data objects.
	 */
	protected DataObjectService dataObjectHandler;
	
	/**
	 * Default outgoing sequence, if no expression returns true.
	 */
	protected ActorRef defaultOutgoingSequence;

	/**
	 * Instantiate Exclusive Gateway
	 * 
	 * @param uniqueProcessId
	 *            id of the process context
	 * @param outgoingNodes
	 *            all references {@link ActorRef} to the following nodes
	 * @param uniqueFlowNodeId
	 *            id of the node inside the process context
	 * @param allDataObjectIds
	 *            all ids of data objects within the process scope
	 * @param conditionalExpressionStrings
	 *            outgoingNodes all references {@link ActorRef} to the following
	 *            nodes with their conditional expressions as JEXL String (see
	 *            http://http://commons.apache.org/jexl/index.html)
	 * @param defaultNode
	 *            reference ({@link ActorRef}) to the default exit (default
	 *            sequence flow)
	 * @param dataObjectHandler 
	 * 			  handles the data object inside the process (instance)
	 */
	public ExclusiveGatewayNode(String uniqueProcessId,
			String uniqueFlowNodeId, 
			List<ActorRef> outgoingNodes,
			Set<String> allDataObjectIds,
			Map<ActorRef, String> conditionalExpressionStrings,
			ActorRef defaultNode,
			DataObjectService dataObjectHandler) {

		super.setUniqueProcessId(uniqueProcessId);
		super.setOutgoingNodes(outgoingNodes);
		super.setUniqueFlowNodeId(uniqueFlowNodeId);
		super.setNodeInstanceMediatorService(new NodeInstanceMediatorService(
				uniqueProcessId, uniqueFlowNodeId));
		
		this.defaultOutgoingSequence = defaultNode;
		
		// create all needed object ids
		this.usedDataObjectIds = ExpressionService.evaluateAllUsedObjects(createList(conditionalExpressionStrings.values()), allDataObjectIds);
		// create JEXL expressions from strings
		this.conditionalExpressionStrings = ExpressionService.createJexlExpressions(conditionalExpressionStrings);
		this.dataObjectHandler = dataObjectHandler;
		
		LOG.info(String.format("Registered Exclusive Gateway (%s) with %s expressions and %s default sequences. ", uniqueFlowNodeId, conditionalExpressionStrings.size(), defaultNode));
	}

	@Override
	protected void activate(ActivationMessage message) {
		
		String iid = message.getProcessInstanceId();
		
		// first call, then set state to active
		if (nodeInstanceMediatorService.getSequenceFlowsFired(iid) == 0) {
			this.getNodeInstanceMediatorService().setActive(iid);
			this.getNodeInstanceMediatorService().setNodeInstanceStartTime(
					message.getProcessInstanceId(), new Date());
		}
		
		// check, if we have to change the state
		int flowsFired = GatewayUtil.setFiredPlusOne(getNodeInstanceMediatorService(), iid);
		if(checkNOfMCondition(iid, flowsFired)) {
			this.getNodeInstanceMediatorService().setNodeInstanceEndTime(iid, new Date());
			this.getNodeInstanceMediatorService().setPassed(iid);
			LOG.debug(String.format("Setting exclusive gateway '%s' to passed.", super.getUniqueFlowNodeId()));
		}
		
		// write changes to db
		this.getNodeInstanceMediatorService().persistChanges();
		
		// fire for every incoming sequence
		ActorRef outgoingSequence = this.evaluateOutGoingSequence(iid);
		if(outgoingSequence != null) {
			super.sendMessageToNodeActor(new ActivationMessage(iid), outgoingSequence);
		} else if (this.defaultOutgoingSequence != null) {
			super.sendMessageToNodeActor(new ActivationMessage(iid), this.defaultOutgoingSequence);
		} else {
			// TODO --> no default node, no true expression, so throw BPMN error here
		}
	}

	@Override
	public int incrementSequenceFlowsFired(Message message, int flowsFired) {
		// TODO refactor this
		return 0;
	}

	@Override
	protected void deactivate(DeactivationMessage message) {
		String iid = message.getProcessInstanceId();
		
		this.getNodeInstanceMediatorService().setNodeInstanceEndTime(iid, new Date());
		this.getNodeInstanceMediatorService().setDeactivated(iid);
		this.getNodeInstanceMediatorService().persistChanges();
	}

	@Override
	protected void trigger(TriggerMessage message) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean checkNOfMCondition(String iid, int flowsFired) {
		return (this.getNodeInstanceMediatorService().getIncomingFiredFlowsNeeded(iid) == flowsFired);
	}
	
	public ActorRef evaluateOutGoingSequence(String iid) {
		Iterator<ActorRef> it = this.conditionalExpressionStrings.keySet().iterator();
		// fill the context once and use it for every expression
		JexlContext context = ExpressionService.fillContext(this.usedDataObjectIds, this.dataObjectHandler, this.uniqueProcessId, iid);
		
		while (it.hasNext()) {
			ActorRef actorRef = it.next();
			Expression expression = this.conditionalExpressionStrings.get(actorRef);
			if(ExpressionService.evaluateToBoolean(expression, context)) {
				return actorRef;
			}
		}
		
		return null;	
	}
	
	public List<String> createList(Collection<String> values) {
		List<String> result = new ArrayList<String>();
		for (String value: values) {
			result.add(value);
		}
		return result;
	}
	
	

}
