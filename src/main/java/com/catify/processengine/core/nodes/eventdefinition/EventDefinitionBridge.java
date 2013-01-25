package com.catify.processengine.core.nodes.eventdefinition;

import java.util.ArrayList;
import java.util.List;

import akka.actor.Actor;
import akka.actor.UntypedActorFactory;

import com.catify.processengine.core.processdefinition.jaxb.TFlowNode;
import com.catify.processengine.core.processdefinition.jaxb.TProcess;
import com.catify.processengine.core.processdefinition.jaxb.TSequenceFlow;
import com.catify.processengine.core.processdefinition.jaxb.TSubProcess;

public class EventDefinitionBridge implements UntypedActorFactory {

	private static final long serialVersionUID = 779471794057385901L;
	
	String clientId;
	TProcess processJaxb;
	ArrayList<TSubProcess> subProcessesJaxb;
	TFlowNode flowNodeJaxb;
	List<TSequenceFlow> sequenceFlowsJaxb;
	
	/**
	 * Instantiates a new EventDefinitionBridge to easily create a configured actor.
	 *
	 * @param eventDefinition the event definition
	 */
	public EventDefinitionBridge(String clientId,
			TProcess processJaxb,
			ArrayList<TSubProcess> subProcessesJaxb,
			TFlowNode flowNodeJaxb,
			List<TSequenceFlow> sequenceFlowsJaxb) {
		super();
		this.clientId = clientId;
		this.processJaxb = processJaxb;
		this.subProcessesJaxb = subProcessesJaxb;
		this.flowNodeJaxb = flowNodeJaxb;
		this.sequenceFlowsJaxb = sequenceFlowsJaxb;
	}

	@Override
	public synchronized Actor create() {
		return new EventDefinitionFactory().getEventDefinition(clientId, processJaxb,
				subProcessesJaxb, flowNodeJaxb);
	}
}