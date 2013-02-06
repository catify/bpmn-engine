package com.catify.processengine.core.nodes.eventdefinition;

import java.util.ArrayList;

import com.catify.processengine.core.data.services.IdService;
import com.catify.processengine.core.processdefinition.jaxb.TFlowNode;
import com.catify.processengine.core.processdefinition.jaxb.TProcess;
import com.catify.processengine.core.processdefinition.jaxb.TSubProcess;

/**
 * Parameter class for creating EventDefinitions.
 * 
 * @author christopher köster
 * @author claus straube
 * 
 */
public class EventDefinitionParameter {
	public String clientId;
	public TProcess processJaxb;
	public ArrayList<TSubProcess> subProcessesJaxb;
	public TFlowNode flowNodeJaxb;
	private String uniqueFlowNodeId;
	private String uniqueProcessId;

	public EventDefinitionParameter(String clientId, TProcess processJaxb,
			ArrayList<TSubProcess> subProcessesJaxb, TFlowNode flowNodeJaxb) {
		this.clientId = clientId;
		this.processJaxb = processJaxb;
		this.subProcessesJaxb = subProcessesJaxb;
		this.flowNodeJaxb = flowNodeJaxb;
	}

	/**
	 * Generates the unique flow node id ({@link IdService}).
	 * 
	 * @return uniqueFlowNodeId
	 */
	public String getUniqueFlowNodeId() {
		if(uniqueFlowNodeId == null) {
			uniqueFlowNodeId = IdService.getUniqueFlowNodeId(this);
		}
		return uniqueFlowNodeId;
	}

	/**
	 * Generates the unique process id ({@link IdService}).
	 * 
	 * @return uniqueProcessId
	 */
	public String getUniqueProcessId() {
		if(uniqueProcessId == null) {
			uniqueProcessId = IdService.getUniqueProcessId(clientId, processJaxb);
		}
		return uniqueProcessId;
	}
	
	
}