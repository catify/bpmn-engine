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
import com.catify.processengine.core.processdefinition.jaxb.TServiceTask;
import com.catify.processengine.core.processdefinition.jaxb.TSignalEventDefinition;
import com.catify.processengine.core.processdefinition.jaxb.TStartEvent;
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
 * @author claus straube
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
		TEventDefinition eventDefinitionJaxb = getTEventDefinition(eventDefinitionParameter.flowNodeJaxb);
	
		// if there is no event definition, create an EmptyEventDefinition actor
		if (eventDefinitionJaxb == null) {
			
			// Tasks can have event definition-like behavior (eg receive task will bind a MessageIntegration)
			if (eventDefinitionParameter.flowNodeJaxb instanceof TTask) {
				TMessageIntegration messageIntegration = ExtensionService.getTMessageIntegration(eventDefinitionParameter.flowNodeJaxb);
				if (messageIntegration != null) {
					return createMessageEventDefinition(eventDefinitionParameter, messageIntegration);
				}
			}
			return new EmptyEventDefinition();
			// else create the implementing event definition actor
		} else {
			// *** create a message event actor ***
			if (eventDefinitionJaxb instanceof TMessageEventDefinition) {
				return createMessageEventDefinition(eventDefinitionParameter, (TMessageEventDefinition) eventDefinitionJaxb);		
			// *** create a terminate event actor ***
			} else if (eventDefinitionJaxb instanceof TTerminateEventDefinition) {
				return createTerminateEventDefinition(eventDefinitionParameter, eventDefinitionJaxb);
			// *** create a timer event definition ***
			} else if (eventDefinitionJaxb instanceof TTimerEventDefinition) {
				return createTimerEventDefinition(eventDefinitionParameter, (TTimerEventDefinition) eventDefinitionJaxb);
			// *** create a signal event actor ***
			} else if (eventDefinitionJaxb instanceof TSignalEventDefinition) {
				return createSignaleventDefinition(eventDefinitionParameter, (TSignalEventDefinition) eventDefinitionJaxb );
			// *** create a link event actor ***
			} else if (eventDefinitionJaxb instanceof TLinkEventDefinition) {
				return createLinkEventDefinition(eventDefinitionParameter, (TLinkEventDefinition) eventDefinitionJaxb );
			}
			// return empty event definition for unimplemented event definitions
			LOG.error(String.format("Unimplemented event definition %s found. Associated events will fail!", getTEventDefinition(eventDefinitionParameter.flowNodeJaxb)));
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

	/**
	 * Creates a new {@link TimerEventDefinition}.
	 * 
	 * @param params
	 * @param timer
	 * @return
	 */
	private EventDefinition createTimerEventDefinition(EventDefinitionParameter params, TTimerEventDefinition timer) {
		
		return new TimerEventDefinition(
				new ActorReferenceService().getActorReference(
						IdService.getUniqueFlowNodeId(params.clientId, params.processJaxb, params.subProcessesJaxb,
								params.flowNodeJaxb)), 
				getTimerType(timer), 
				getIsoDate(timer));

	}

	/**
	 * Get timer type (date, duration or cycle). 
	 * 
	 * @param timer
	 * @return
	 */
	private TimerTypes getTimerType(TTimerEventDefinition timer) {
		if (timer.getTimeDate() != null) {
			return TimerTypes.TIMEDATE;
		} else if (timer.getTimeDuration() != null) {
			return TimerTypes.TIMEDURATION;
		} else if (timer.getTimeCycle() != null) {
			return TimerTypes.TIMECYCLE;
		}
		return null;
	}

	/**
	 * Gets the ISO 8601 date String from the timer definition.
	 * 
	 * @param timer
	 * @return
	 */
	private String  getIsoDate(TTimerEventDefinition timer) {
		if (timer.getTimeDate() != null) {
			return timer.getTimeDate().getContent().get(0).toString();
		} else if (timer.getTimeDuration() != null) {
			return timer.getTimeDuration().getContent().get(0).toString();
		} else if (timer.getTimeCycle() != null) {
			return timer.getTimeCycle().getContent().get(0).toString();
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
	private EventDefinition createMessageEventDefinition(EventDefinitionParameter params, TMessageEventDefinition message) {
		
		TMessageIntegration messageIntegration = ExtensionService.getTMessageIntegration((TMessageEventDefinition) message);
		
		return getMessageEventDefinition(params, messageIntegration);
	}
	
	/**
	 * Creates a new MessageEventDefinition object.
	 *
	 * @param processJaxb the processJaxb
	 * @param flowNodeJaxb the flow nodeJaxb
	 * @param messageEventDefinitionJaxb the event definitionJaxb
	 * @return the event definition actor
	 */
	private EventDefinition createMessageEventDefinition(EventDefinitionParameter params, TMessageIntegration messageIntegration) {
		return getMessageEventDefinition(params, messageIntegration);
	}

	/**
	 * Create message {@link EventDefinition} actor based on the underlying node type.
	 * 
	 * @param params EventDefinitionParameter
	 * @param messageIntegration jaxb TMessageIntegration
	 * @return the event definition actor
	 */
	private EventDefinition getMessageEventDefinition(EventDefinitionParameter params, TMessageIntegration messageIntegration) {
		// message event is catching
		if (params.flowNodeJaxb instanceof TCatchEvent || params.flowNodeJaxb instanceof TReceiveTask) {	
			return getMessageEventDefinitionCatch(params, messageIntegration);
	
		// message event is throwing
		} else if (params.flowNodeJaxb instanceof TThrowEvent || params.flowNodeJaxb instanceof TSendTask) {
			return getMessageEventDefinitionThrow(params, messageIntegration);
			
		// message event is request/reply
		} else if (params.flowNodeJaxb instanceof TServiceTask) {
			return getMessageEventDefinitionRequestReply(params, messageIntegration);
		}
		return null;
	}

	/**
	 * Create a message {@link EventDefinition} actor for catching nodes (eg. catch event, receive task).
	 * 
	 * @param params EventDefinitionParameter
	 * @param messageIntegration jaxb TMessageIntegration
	 * @return the event definition actor
	 */
	public EventDefinition getMessageEventDefinitionCatch(EventDefinitionParameter params, TMessageIntegration messageIntegration) {
		EventDefinition eventDefinition;
		eventDefinition = new MessageEventDefinitionCatch(
				IdService.getUniqueProcessId(params),
				IdService.getUniqueFlowNodeId(params), 
				ActorReferenceService.getActorReferenceString(
						IdService.getUniqueFlowNodeId(params)),  
				messageIntegration);
		return eventDefinition;
	}

	/**
	 * Create a message {@link EventDefinition} actor for throwing nodes (eg. throw event, send task).
	 * 
	 * @param params EventDefinitionParameter
	 * @param messageIntegration jaxb TMessageIntegration
	 * @return the event definition actor
	 */
	public EventDefinition getMessageEventDefinitionThrow(EventDefinitionParameter params, TMessageIntegration messageIntegration) {
		EventDefinition eventDefinition;
		// create throwing message event definition to be used in akka actor
		eventDefinition = new MessageEventDefinitionThrow(
				IdService.getUniqueProcessId(params),
				IdService.getUniqueFlowNodeId(params), 
				ActorReferenceService.getActorReferenceString(
						IdService.getUniqueFlowNodeId(params)),  
				messageIntegration);
		return eventDefinition;
	}
	
	/**
	 * Create a message {@link EventDefinition} actor for request/reply nodes (eg. service task).
	 * 
	 * @param params EventDefinitionParameter
	 * @param messageIntegration jaxb TMessageIntegration
	 * @return the event definition actor
	 */
	public EventDefinition getMessageEventDefinitionRequestReply(EventDefinitionParameter params, TMessageIntegration messageIntegration) {
		EventDefinition eventDefinition;
		// create throwing message event definition to be used in akka actor
		eventDefinition = new MessageEventDefinitionRequestReply(
				IdService.getUniqueProcessId(params),
				IdService.getUniqueFlowNodeId(params), 
				ActorReferenceService.getActorReferenceString(
						IdService.getUniqueFlowNodeId(params)),  
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
	private EventDefinition createTerminateEventDefinition(EventDefinitionParameter params, TEventDefinition eventDefinitionJaxb) {

		EventDefinition eventDefinition = new TerminateEventDefinition(
				IdService.getUniqueProcessId(params),
				IdService.getUniqueFlowNodeId(params),
				new ActorReferenceService().getActorReference(
						IdService.getUniqueFlowNodeId(params)),
				getTopLevelActorReferences(params)	
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
	private Set<ActorRef> getTopLevelActorReferences(EventDefinitionParameter params) {
		Set<ActorRef> actorReferences = getTopLevelActorReferences(params.clientId, params.processJaxb);
		
		// remove actor reference of given flow node
		actorReferences.remove(new ActorReferenceService().getActorReference(
						IdService.getUniqueFlowNodeId(params)));

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
	private Set<ActorRef> getTopLevelActorReferences(String clientId, TProcess processJaxb) {
		Set<ActorRef> actorReferences = new HashSet<ActorRef>();
		
		List<JAXBElement<? extends TFlowElement>> flowElements = new ArrayList<JAXBElement<? extends TFlowElement>>();
		
		flowElements = processJaxb.getFlowElement();
		
		for (JAXBElement<? extends TFlowElement> flowElement : flowElements) {
			if (flowElement.getValue() instanceof TFlowNode) {
				actorReferences.add(new ActorReferenceService().getActorReference(
						IdService.getUniqueFlowNodeId(clientId, processJaxb, null,
								(TFlowNode) flowElement.getValue())));
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
	public TEventDefinition getTEventDefinition(TFlowNode flowNodeJaxb) {
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
