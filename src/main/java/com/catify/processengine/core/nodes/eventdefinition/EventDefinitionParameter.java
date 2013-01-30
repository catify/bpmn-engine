package com.catify.processengine.core.nodes.eventdefinition;

import java.util.ArrayList;

import com.catify.processengine.core.processdefinition.jaxb.TFlowNode;
import com.catify.processengine.core.processdefinition.jaxb.TProcess;
import com.catify.processengine.core.processdefinition.jaxb.TSubProcess;

/**
 * Parameter class for creating EventDefinitions.
 * 
 * @author christopher köster
 * 
 */
public class EventDefinitionParameter {
	public String clientId;
	public TProcess processJaxb;
	public ArrayList<TSubProcess> subProcessesJaxb;
	public TFlowNode flowNodeJaxb;

	public EventDefinitionParameter(String clientId, TProcess processJaxb,
			ArrayList<TSubProcess> subProcessesJaxb, TFlowNode flowNodeJaxb) {
		this.clientId = clientId;
		this.processJaxb = processJaxb;
		this.subProcessesJaxb = subProcessesJaxb;
		this.flowNodeJaxb = flowNodeJaxb;
	}
}