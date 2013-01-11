/**
 * -------------------------------------------------------
 * Copyright (C) 2013 catify <info@catify.com>
 * -------------------------------------------------------
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
public final class EventDefinitionFactory {

	private EventDefinitionFactory() {
		
	}
	
	/**
	 * Object factory method for creating the implementation of a event
	 * definition.
	 * 
	 * @param flowNodeJaxb
	 * @param eventDefinitionJaxb
	 * @return new EventDefinition
	 */
	public static EventDefinition getEventDefinition(String clientId, TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb,
			TFlowNode flowNodeJaxb) {
	
		TEventDefinition eventDefinitionJaxb = null;
		EventDefinition eventDefinition = null;
	
		// get the event definition (if any)
		if (flowNodeJaxb instanceof TCatchEvent) {
			if (((TCatchEvent) flowNodeJaxb).getEventDefinition().size() > 0) {
				eventDefinitionJaxb = ((TCatchEvent) flowNodeJaxb)
						.getEventDefinition().get(0).getValue();
			} else {
				return new EmptyEventDefinition();
			}
		} else if (flowNodeJaxb instanceof TThrowEvent) {
			if (((TThrowEvent) flowNodeJaxb).getEventDefinition().size() > 0) {
				eventDefinitionJaxb = ((TThrowEvent) flowNodeJaxb)
						.getEventDefinition().get(0).getValue();
			} else {
				return new EmptyEventDefinition();
			}
		}
	
		// *** create a message event ***
		if (eventDefinitionJaxb.getClass().equals(
				TMessageEventDefinition.class)) {
			
			eventDefinition = createMessageEventDefinition(clientId, processJaxb, subProcessesJaxb, 
			flowNodeJaxb, (TMessageEventDefinition) eventDefinitionJaxb);
	
		// *** create a terminate event ***
		} else if (eventDefinitionJaxb.getClass().equals(
				TTerminateEventDefinition.class)) {
			eventDefinition = createTerminateEventDefinition(clientId, processJaxb, subProcessesJaxb, 
					flowNodeJaxb, (TTerminateEventDefinition) eventDefinitionJaxb);
		}
	
		return eventDefinition;
	}

	/**
	 * Creates a new MessageEventDefinition object.
	 *
	 * @param processJaxb the processJaxb
	 * @param flowNodeJaxb the flow nodeJaxb
	 * @param messageEventDefinitionJaxb the event definitionJaxb
	 * @return the event definition
	 */
	static EventDefinition createMessageEventDefinition(String clientId, TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb,
			TFlowNode flowNodeJaxb, TMessageEventDefinition messageEventDefinitionJaxb) {
		
		EventDefinition eventDefinition = null;
		TMessageIntegration messageIntegration = ExtensionService.getTMessageIntegration(messageEventDefinitionJaxb);
		
		// message event is catching
		if (flowNodeJaxb.getClass().getSuperclass()
				.equals(TCatchEvent.class)) {
			
			// create catching message event definition to be used in akka actor	
			eventDefinition = new MessageEventDefinition_Catch(
					IdService.getUniqueProcessId(clientId, processJaxb),
					IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb,
							flowNodeJaxb), 
							ActorReferenceService.getActorReferenceString(
									IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb,
											flowNodeJaxb)),  
							messageIntegration);
	
		// message event is throwing
		} else if (flowNodeJaxb.getClass().getSuperclass()
				.equals(TThrowEvent.class)) {
			
			// create throwing message event definition to be used in akka actor
			eventDefinition = new MessageEventDefinition_Throw(
					IdService.getUniqueProcessId(clientId, processJaxb),
					IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb,
							flowNodeJaxb), 
							ActorReferenceService.getActorReferenceString(
									IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb,
											flowNodeJaxb)),  
							messageIntegration);
		}
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
	public static EventDefinition createTerminateEventDefinition(String clientId, TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb,
			TFlowNode flowNodeJaxb, TTerminateEventDefinition terminateEventDefinitionJaxb) {
				
		EventDefinition eventDefinition = null;
		
		eventDefinition = new TerminateEventDefinition(
				IdService.getUniqueProcessId(clientId, processJaxb),
				IdService
						.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb, flowNodeJaxb),
				new ActorReferenceService().getActorReference(
						IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb,
								flowNodeJaxb)));
		
		return eventDefinition;
	}

}
