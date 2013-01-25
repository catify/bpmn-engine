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
package com.catify.processengine.core.nodes.eventdefinition;

import java.util.ArrayList;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActorFactory;

import com.catify.processengine.core.data.services.impl.IdService;
import com.catify.processengine.core.processdefinition.jaxb.TCatchEvent;
import com.catify.processengine.core.processdefinition.jaxb.TEventDefinition;
import com.catify.processengine.core.processdefinition.jaxb.TFlowNode;
import com.catify.processengine.core.processdefinition.jaxb.TMessageEventDefinition;
import com.catify.processengine.core.processdefinition.jaxb.TMessageIntegration;
import com.catify.processengine.core.processdefinition.jaxb.TProcess;
import com.catify.processengine.core.processdefinition.jaxb.TSubProcess;
import com.catify.processengine.core.processdefinition.jaxb.TTerminateEventDefinition;
import com.catify.processengine.core.processdefinition.jaxb.TThrowEvent;
import com.catify.processengine.core.processdefinition.jaxb.services.ExtensionService;
import com.catify.processengine.core.services.ActorReferenceService;

/**
 * A factory for creating EventDefinition objects.
 *
 * @author chris
 */
@Configurable
public class EventDefinitionFactory {
	
	public static final Logger LOG = LoggerFactory
			.getLogger(EventDefinitionFactory.class);

	@Autowired
	private ActorSystem actorSystem;

	public EventDefinitionFactory() {
		
	}
	
	/**
	 * Object factory method for creating the implementation of a event
	 * definition.
	 * 
	 * @param flowNodeJaxb
	 * @param eventDefinitionJaxb
	 * @return new EventDefinition ActorRef
	 */
	public ActorRef getEventDefinitionActor(String clientId, TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb,
			TFlowNode flowNodeJaxb) {
	
//		// get the event definition (if any)
		TEventDefinition eventDefinitionJaxb = getTEventDefinition(clientId, processJaxb,
				subProcessesJaxb, flowNodeJaxb);
	
		// if there is no event definition, create an EmptyEventDefinition actor
		if (eventDefinitionJaxb == null) {
			return this.createEventDefinitionActor(clientId, processJaxb, subProcessesJaxb, 
					flowNodeJaxb, new EmptyEventDefinition(), UUID.randomUUID().toString());
			// else create the implementing event definition actor
		} else {
			// *** create a message event actor ***
			if (eventDefinitionJaxb.getClass().equals(
					TMessageEventDefinition.class)) {
				return createMessageEventDefinitionActor(clientId, processJaxb, subProcessesJaxb, 
						flowNodeJaxb, (TMessageEventDefinition) eventDefinitionJaxb);
		
			// *** create a terminate event actor ***
			} else if (eventDefinitionJaxb.getClass().equals(
					TTerminateEventDefinition.class)) {
				return createTerminateEventDefinitionActor(clientId, processJaxb, subProcessesJaxb, 
						flowNodeJaxb, eventDefinitionJaxb);
			}
			// return empty event definition for unimplemented event definitions
			LOG.error(String.format("Unimplemented event definition %s found. Associated events will fail!", getTEventDefinition(clientId, processJaxb,
					subProcessesJaxb, flowNodeJaxb)));
			return null;
		}
	}

	/**
	 * Creates a new MessageEventDefinition object.
	 *
	 * @param processJaxb the processJaxb
	 * @param flowNodeJaxb the flow nodeJaxb
	 * @param messageEventDefinitionJaxb the event definitionJaxb
	 * @return the event definition
	 */
	private ActorRef createMessageEventDefinitionActor(String clientId, TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb,
			TFlowNode flowNodeJaxb, TEventDefinition eventDefinitionJaxb) {
		
		TMessageIntegration messageIntegration = ExtensionService.getTMessageIntegration((TMessageEventDefinition) eventDefinitionJaxb);
		EventDefinition eventDefinition = null;
		
		// message event is catching
		if (flowNodeJaxb.getClass().getSuperclass()
				.equals(TCatchEvent.class)) {
			
			// create catching message event definition to be used in akka actor	
			eventDefinition = getMessageEventDefinitionCatch(clientId,
					processJaxb, subProcessesJaxb, flowNodeJaxb,
					messageIntegration);
	
		// message event is throwing
		} else if (flowNodeJaxb.getClass().getSuperclass()
				.equals(TThrowEvent.class)) {
			
			eventDefinition = getMessageEventDefinitionThrow(clientId,
					processJaxb, subProcessesJaxb, flowNodeJaxb,
					messageIntegration);
		}
		
		return 	this.createEventDefinitionActor(clientId, processJaxb, subProcessesJaxb,
				flowNodeJaxb, eventDefinition, eventDefinitionJaxb.getId());
	}

	public EventDefinition getMessageEventDefinitionCatch(String clientId,
			TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb,
			TFlowNode flowNodeJaxb, TMessageIntegration messageIntegration) {
		EventDefinition eventDefinition;
		eventDefinition = new MessageEventDefinition_Catch(
				IdService.getUniqueProcessId(clientId, processJaxb),
				IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb,
						flowNodeJaxb), 
						ActorReferenceService.getActorReferenceString(
								IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb,
										flowNodeJaxb)),  
						messageIntegration);
		return eventDefinition;
	}

	public EventDefinition getMessageEventDefinitionThrow(String clientId,
			TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb,
			TFlowNode flowNodeJaxb, TMessageIntegration messageIntegration) {
		EventDefinition eventDefinition;
		// create throwing message event definition to be used in akka actor
		eventDefinition = new MessageEventDefinition_Throw(
				IdService.getUniqueProcessId(clientId, processJaxb),
				IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb,
						flowNodeJaxb), 
						ActorReferenceService.getActorReferenceString(
								IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb,
										flowNodeJaxb)),  
						messageIntegration);
		return eventDefinition;
	}

	/**
	 * Creates a new TerminateEventDefinition object.
	 *
	 * @param processJaxb the processJaxb
	 * @param flowNodeJaxb the flow nodeJaxb
	 * @param terminateEventDefinitionJaxb the event definitionJaxb
	 * @return the event definition
	 */
	private ActorRef createTerminateEventDefinitionActor(String clientId, TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb,
			TFlowNode flowNodeJaxb, TEventDefinition eventDefinitionJaxb) {

		EventDefinition eventDefinition = new TerminateEventDefinition(
				IdService.getUniqueProcessId(clientId, processJaxb),
				IdService
						.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb, flowNodeJaxb),
				new ActorReferenceService().getActorReference(
						IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb,
								flowNodeJaxb)));
		
		return 	this.createEventDefinitionActor(clientId, processJaxb, subProcessesJaxb,
				flowNodeJaxb, eventDefinition, eventDefinitionJaxb.getId());
	}

	/**
	 * Creates a new EventDefinition object.
	 *
	 * @param clientId the client id
	 * @param processJaxb the process jaxb
	 * @param subProcessesJaxb the sub processes jaxb
	 * @param flowNodeJaxb the flow node jaxb
	 * @param eventDefinition the event definition
	 * @param eventDefinitionId the event definition id
	 * @return the actor ref
	 */
	public ActorRef createEventDefinitionActor(String clientId,
			TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb,
			TFlowNode flowNodeJaxb, EventDefinition eventDefinition,
			String eventDefinitionId) {
		
		return actorSystem.actorOf(new Props(
					this.new EventDefinitionBridge(eventDefinition)
				).withDispatcher("file-mailbox-dispatcher"), ActorReferenceService.getActorReferenceString(
						IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb, flowNodeJaxb)) 
						+ eventDefinitionId);
	}
	
	/**
	 * Gets the jaxb event definition.
	 *
	 * @param clientId the client id
	 * @param processJaxb the jaxb process 
	 * @param subProcessesJaxb the jaxb sub processes
	 * @param flowNodeJaxb the jaxb flow node 
	 * @return the jaxb event definition or null if none is found
	 */
	private TEventDefinition getTEventDefinition(String clientId,
			TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb,
			TFlowNode flowNodeJaxb) {
		if (flowNodeJaxb instanceof TCatchEvent) {
			if (((TCatchEvent) flowNodeJaxb).getEventDefinition().size() > 0) {
				return ((TCatchEvent) flowNodeJaxb).getEventDefinition().get(0).getValue();
			} else {
				return 	null;
			}
		} else if (flowNodeJaxb instanceof TThrowEvent) {
			if (((TThrowEvent) flowNodeJaxb).getEventDefinition().size() > 0) {
				return ((TThrowEvent) flowNodeJaxb).getEventDefinition().get(0).getValue();
			} else {
				return 	null;
			}
		} else return null;
	}

	private class EventDefinitionBridge implements UntypedActorFactory {

		private static final long serialVersionUID = 779471794057385901L;
		
		private EventDefinition eventDefinition;
		
		/**
		 * Instantiates a new EventDefinitionBridge to easily create a configured actor.
		 *
		 * @param eventDefinition the event definition
		 */
		public EventDefinitionBridge(EventDefinition eventDefinition) {
			super();
			this.eventDefinition = eventDefinition;
		}

		@Override
		public synchronized Actor create() {
			return this.eventDefinition;
		}
	}
}
