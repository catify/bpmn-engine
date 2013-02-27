package com.catify.processengine.core.nodes;

import java.util.List;

import com.catify.processengine.core.data.services.IdService;
import com.catify.processengine.core.processdefinition.jaxb.TFlowNode;
import com.catify.processengine.core.processdefinition.jaxb.TProcess;
import com.catify.processengine.core.processdefinition.jaxb.TSequenceFlow;
import com.catify.processengine.core.processdefinition.jaxb.TSubProcess;

/**
 * Parameter class for creating nodes.
 * 
 * @author christopher köster
 * @author claus straube
 * 
 */
public class NodeParameter {
	public String clientId;
	public TProcess processJaxb;
	public List<TSubProcess> subProcessesJaxb;
	public TFlowNode flowNodeJaxb;
	public List<TSequenceFlow> sequenceFlowsJaxb;
	private String uniqueFlowNodeId;
	private String uniqueProcessId;

	public NodeParameter(String clientId, TProcess processJaxb,
			List<TSubProcess> subProcessesJaxb, TFlowNode flowNodeJaxb, List<TSequenceFlow> sequenceFlowsJaxb) {
		this.clientId = clientId;
		this.processJaxb = processJaxb;
		this.subProcessesJaxb = subProcessesJaxb;
		this.flowNodeJaxb = flowNodeJaxb;
		this.sequenceFlowsJaxb = sequenceFlowsJaxb;
	}

	/**
	 * Generates the unique flow node id ({@link IdService}).
	 * 
	 * @return uniqueFlowNodeId
	 */
	public String getUniqueFlowNodeId() {
		if(uniqueFlowNodeId == null) {
			uniqueFlowNodeId = IdService.getUniqueFlowNodeId(this.clientId, this.processJaxb, this.subProcessesJaxb, this.flowNodeJaxb);
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