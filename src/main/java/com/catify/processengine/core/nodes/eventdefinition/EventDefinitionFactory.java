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

import java.text.Format;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;

import akka.actor.ActorRef;

import com.catify.processengine.core.data.services.IdService;
import com.catify.processengine.core.processdefinition.jaxb.TCatchEvent;
import com.catify.processengine.core.processdefinition.jaxb.TEventDefinition;
import com.catify.processengine.core.processdefinition.jaxb.TFlowElement;
import com.catify.processengine.core.processdefinition.jaxb.TFlowNode;
import com.catify.processengine.core.processdefinition.jaxb.TLinkEventDefinition;
import com.catify.processengine.core.processdefinition.jaxb.TMessageEventDefinition;
import com.catify.processengine.core.processdefinition.jaxb.TMessageIntegration;
import com.catify.processengine.core.processdefinition.jaxb.TProcess;
import com.catify.processengine.core.processdefinition.jaxb.TReceiveTask;
import com.catify.processengine.core.processdefinition.jaxb.TSendTask;
import com.catify.processengine.core.processdefinition.jaxb.TSignalEventDefinition;
import com.catify.processengine.core.processdefinition.jaxb.TStartEvent;
import com.catify.processengine.core.processdefinition.jaxb.TSubProcess;
import com.catify.processengine.core.processdefinition.jaxb.TTask;
import com.catify.processengine.core.processdefinition.jaxb.TTerminateEventDefinition;
import com.catify.processengine.core.processdefinition.jaxb.TThrowEvent;
import com.catify.processengine.core.processdefinition.jaxb.TTimerEventDefinition;
import com.catify.processengine.core.processdefinition.jaxb.services.ExtensionService;
import com.catify.processengine.core.services.ActorReferenceService;

/**
 * A factory for creating EventDefinition objects.
 * 
 * @author christopher köster
 * 
 */
@Configurable
public class EventDefinitionFactory {
	
	public static final Logger LOG = LoggerFactory
			.getLogger(EventDefinitionFactory.class);

	public EventDefinitionFactory() {
		
	}
	
	public EventDefinition getEventDefinition(EventDefinitionParameter eventDefinitionParameter) {
		// get the event definition (if any)
		TEventDefinition eventDefinitionJaxb = getTEventDefinition(eventDefinitionParameter.clientId, eventDefinitionParameter.processJaxb,
				eventDefinitionParameter.subProcessesJaxb, eventDefinitionParameter.flowNodeJaxb);
	
		// if there is no event definition, create an EmptyEventDefinition actor
		if (eventDefinitionJaxb == null) {
			
			// Tasks can have event definition-like behavior (eg receive task will bind a MessageIntegration)
			if (eventDefinitionParameter.flowNodeJaxb instanceof TTask) {
				TMessageIntegration messageIntegration = ExtensionService.getTMessageIntegration(eventDefinitionParameter.flowNodeJaxb);
				if (messageIntegration != null) {
					return createMessageEventDefinition(eventDefinitionParameter.clientId, eventDefinitionParameter.processJaxb, eventDefinitionParameter.subProcessesJaxb, 
						eventDefinitionParameter.flowNodeJaxb, messageIntegration);
				}
			}
			return new EmptyEventDefinition();
			// else create the implementing event definition actor
		} else {
			// *** create a message event actor ***
			if (eventDefinitionJaxb.getClass().equals(
					TMessageEventDefinition.class)) {
				return createMessageEventDefinition(eventDefinitionParameter.clientId, eventDefinitionParameter.processJaxb, eventDefinitionParameter.subProcessesJaxb, 
						eventDefinitionParameter.flowNodeJaxb, (TMessageEventDefinition) eventDefinitionJaxb);
		
			// *** create a terminate event actor ***
			} else if (eventDefinitionJaxb.getClass().equals(
					TTerminateEventDefinition.class)) {
				return createTerminateEventDefinition(eventDefinitionParameter.clientId, eventDefinitionParameter.processJaxb, eventDefinitionParameter.subProcessesJaxb, 
						eventDefinitionParameter.flowNodeJaxb, eventDefinitionJaxb);
			} else if (eventDefinitionJaxb.getClass().equals(
					TTimerEventDefinition.class)) {
				return createTimerEventDefinition(eventDefinitionParameter.clientId, eventDefinitionParameter.processJaxb, eventDefinitionParameter.subProcessesJaxb, 
						eventDefinitionParameter.flowNodeJaxb, eventDefinitionJaxb);
			// *** create a signal event actor ***
			} else if (eventDefinitionJaxb instanceof TSignalEventDefinition) {
				return createSignaleventDefinition(eventDefinitionParameter, (TSignalEventDefinition) eventDefinitionJaxb );
			// *** create a link event actor ***
			} else if (eventDefinitionJaxb instanceof TLinkEventDefinition) {
				return createLinkEventDefinition(eventDefinitionParameter, (TLinkEventDefinition) eventDefinitionJaxb );
			}
			// return empty event definition for unimplemented event definitions
			LOG.error(String.format("Unimplemented event definition %s found. Associated events will fail!", getTEventDefinition(eventDefinitionParameter.clientId, eventDefinitionParameter.processJaxb,
					eventDefinitionParameter.subProcessesJaxb, eventDefinitionParameter.flowNodeJaxb)));
			return null;
		}
	}

	/**
	 * Creates a new {@link SignalEventDefinition}.
	 * 
	 * @param eventDefinitionParameter
	 * @param eventDefinitionJaxb
	 * @return
	 */
	private EventDefinition createSignaleventDefinition(EventDefinitionParameter params, TSignalEventDefinition signal) {
		ActorRef actorRef = new ActorReferenceService().getActorReference(params.getUniqueFlowNodeId());
		boolean isStart = Boolean.FALSE;
		if(params.flowNodeJaxb instanceof TStartEvent) {
			isStart = Boolean.TRUE;
		}
		boolean isThrow = Boolean.FALSE;
		if(params.flowNodeJaxb instanceof TThrowEvent) {
			isThrow = Boolean.TRUE;
		}
		String signalRef = signal.getSignalRef().getLocalPart();
		return new SignalEventDefinition(actorRef, isStart, isThrow, signalRef, params);
	}
	
	/**
	 * Creates a new {@link LinkEventDefinition}.
	 * 
	 * @param params
	 * @param link
	 * @return
	 */
	private EventDefinition createLinkEventDefinition(EventDefinitionParameter params, TLinkEventDefinition link) {
		ActorRef actorRef = new ActorReferenceService().getActorReference(params.getUniqueFlowNodeId());
	
		if(params.flowNodeJaxb instanceof TThrowEvent) {
			/*
			 * if it is a throwing event we need the 
			 * target (where to go). 
			 */
			String target = "";
			if(link.getTarget() != null) {
				target = link.getTarget().getLocalPart();
			} else {
				LOG.warn(String.format("The throwing signal event '%s' has no target element. This will not work.", params.getUniqueFlowNodeId()));
			}
			
			return new LinkEventDefinition(target, params.getUniqueProcessId(), actorRef);
		} else {
			/*
			 * if it is a catching event we need the 
			 * sources (where the link comes from)
			 */
			List<String> sources = new ArrayList<String>();
			if(link.getSource() != null) {
				List<QName> source = link.getSource();
				for (QName qName : source) {
					sources.add(qName.getLocalPart());
				}
			} else {
				LOG.warn(String.format("The catching signal event '%s' has no source element. This will not work.", params.getUniqueFlowNodeId()));
			}
			return new LinkEventDefinition(sources, params.getUniqueProcessId(), actorRef);
		}
		
	}

	private EventDefinition createTimerEventDefinition(String clientId,
			TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb,
			TFlowNode flowNodeJaxb, TEventDefinition eventDefinitionJaxb) {
		
		return new TimerEventDefinition(
				new ActorReferenceService().getActorReference(
						IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb,
								flowNodeJaxb)), 
				getTimerType((TTimerEventDefinition) eventDefinitionJaxb), 
				getIsoDate((TTimerEventDefinition) eventDefinitionJaxb));

	}

	private TimerTypes getTimerType(TTimerEventDefinition timerEventDefinitionJaxb) {
		if (timerEventDefinitionJaxb.getTimeDate() != null) {
			return TimerTypes.TIMEDATE;
		} else if (timerEventDefinitionJaxb.getTimeDuration() != null) {
			return TimerTypes.TIMEDURATION;
		} else if (timerEventDefinitionJaxb.getTimeCycle() != null) {
			return TimerTypes.TIMECYCLE;
		}
		return null;
	}

	private String  getIsoDate(TTimerEventDefinition timerEventDefinitionJaxb) {
		if (timerEventDefinitionJaxb.getTimeDate() != null) {
			return timerEventDefinitionJaxb.getTimeDate().getContent().get(0).toString();
		} else if (timerEventDefinitionJaxb.getTimeDuration() != null) {
			return timerEventDefinitionJaxb.getTimeDuration().getContent().get(0).toString();
		} else if (timerEventDefinitionJaxb.getTimeCycle() != null) {
			return timerEventDefinitionJaxb.getTimeCycle().getContent().get(0).toString();
		}
		return null;
	}
	
	/**
	 * Creates a new MessageEventDefinition object.
	 *
	 * @param processJaxb the processJaxb
	 * @param flowNodeJaxb the flow nodeJaxb
	 * @param messageEventDefinitionJaxb the event definitionJaxb
	 * @return the event definition
	 */
	private EventDefinition createMessageEventDefinition(String clientId, TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb,
			TFlowNode flowNodeJaxb, TEventDefinition eventDefinitionJaxb) {
		
		TMessageIntegration messageIntegration = ExtensionService.getTMessageIntegration((TMessageEventDefinition) eventDefinitionJaxb);
		
		return getMessageEventDefinition(clientId, processJaxb,
				subProcessesJaxb, flowNodeJaxb, messageIntegration);
	}
	
	/**
	 * Creates a new MessageEventDefinition object.
	 *
	 * @param processJaxb the processJaxb
	 * @param flowNodeJaxb the flow nodeJaxb
	 * @param messageEventDefinitionJaxb the event definitionJaxb
	 * @return the event definition
	 */
	private EventDefinition createMessageEventDefinition(String clientId, TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb,
			TFlowNode flowNodeJaxb, TMessageIntegration messageIntegration) {
		return getMessageEventDefinition(clientId, processJaxb,
				subProcessesJaxb, flowNodeJaxb, messageIntegration);
	}

	private EventDefinition getMessageEventDefinition(String clientId,
			TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb,
			TFlowNode flowNodeJaxb, TMessageIntegration messageIntegration) {
		// message event is catching
		if (flowNodeJaxb.getClass().getSuperclass()
				.equals(TCatchEvent.class) || flowNodeJaxb instanceof TReceiveTask) {
			
			// create catching message event definition to be used in akka actor	
			return getMessageEventDefinitionCatch(clientId,
					processJaxb, subProcessesJaxb, flowNodeJaxb,
					messageIntegration);
	
		// message event is throwing
		} else if (flowNodeJaxb.getClass().getSuperclass()
				.equals(TThrowEvent.class) || flowNodeJaxb instanceof TSendTask) {
			
			return getMessageEventDefinitionThrow(clientId,
					processJaxb, subProcessesJaxb, flowNodeJaxb,
					messageIntegration);
		}
		return null;
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
	private EventDefinition createTerminateEventDefinition(String clientId, TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb,
			TFlowNode flowNodeJaxb, TEventDefinition eventDefinitionJaxb) {

		EventDefinition eventDefinition = new TerminateEventDefinition(
				IdService.getUniqueProcessId(clientId, processJaxb),
				IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb, flowNodeJaxb),
				new ActorReferenceService().getActorReference(
						IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb,
								flowNodeJaxb)),
				getOtherActorReferences(clientId, processJaxb, subProcessesJaxb, flowNodeJaxb)				
				);
		
		return eventDefinition;
	}
	
	/**
	 * Gets all actor references of the process (including sub processes) 
	 * but excluding the actor reference of a given flow node.
	 *
	 * @param clientId the client id
	 * @param processJaxb the process jaxb
	 * @param subProcessesJaxb the sub processes jaxb
	 * @param flowNodeJaxb the jaxb flow node to be excluded
	 * @return the other actor references
	 */
	private Set<ActorRef> getOtherActorReferences(String clientId, TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb,
			TFlowNode flowNodeJaxb) {
		Set<ActorRef> actorReferences = getAllActorReferences(clientId, processJaxb, subProcessesJaxb, null);
		
		// remove actor reference of given flow node
		actorReferences.remove(new ActorReferenceService().getActorReference(
						IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb,
								flowNodeJaxb)));

		return actorReferences;
	}
	
	/**
	 * Gets all actor references of the process (including sub processes).
	 *
	 * @param clientId the client id
	 * @param processJaxb the process jaxb
	 * @param subProcessesJaxb the sub processes jaxb
	 * @param flowNodeJaxb the flow node jaxb
	 * @return the all actor references
	 */
	private Set<ActorRef> getAllActorReferences(String clientId, TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb,
			TSubProcess subProcessJaxb) {
		Set<ActorRef> actorReferences = new HashSet<ActorRef>();
		
		List<JAXBElement<? extends TFlowElement>> flowElements = new ArrayList<JAXBElement<? extends TFlowElement>>();
		if (subProcessJaxb == null) {
			flowElements = processJaxb.getFlowElement();
		} else {
			flowElements = subProcessJaxb.getFlowElement();
		}
		
		for (JAXBElement<? extends TFlowElement> flowElement : flowElements) {
			if (flowElement.getValue() instanceof TFlowNode) {
				actorReferences.add(new ActorReferenceService().getActorReference(
						IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb,
								(TFlowNode) flowElement.getValue())));
			}
			if (flowElement.getValue() instanceof TSubProcess) {
				// create local copies for recursion
				ArrayList<TSubProcess> recursiveSubProcessesJaxb = new ArrayList<TSubProcess>(subProcessesJaxb);
				recursiveSubProcessesJaxb.add((TSubProcess) flowElement.getValue());
				TSubProcess newSubProcessJaxb = (TSubProcess) flowElement.getValue();
				
				actorReferences.addAll(getAllActorReferences(clientId, processJaxb, recursiveSubProcessesJaxb, newSubProcessJaxb));
			}
		}
		
		return actorReferences;
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

}
