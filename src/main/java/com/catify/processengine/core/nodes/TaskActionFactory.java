package com.catify.processengine.core.nodes;

import java.util.List;

import com.catify.processengine.core.data.services.IdService;
import com.catify.processengine.core.nodes.eventdefinition.EventDefinitionParameter;
import com.catify.processengine.core.processdefinition.jaxb.TFlowNode;
import com.catify.processengine.core.processdefinition.jaxb.TProcess;
import com.catify.processengine.core.processdefinition.jaxb.TSequenceFlow;
import com.catify.processengine.core.processdefinition.jaxb.TSubProcess;

/**
 * A factory for creating TaskAction objects. Extends the {@link NodeFactoryImpl} to override the createTask-methods and return task action actors.
 */
public class TaskActionFactory extends NodeFactoryImpl {

	/**
	 * Creates the send task action.
	 *
	 * @param clientId the client id
	 * @param processJaxb the jaxb process 
	 * @param subProcessesJaxb the jaxb sub processes 
	 * @param flowNodeJaxb the jaxb flow node 
	 * @param sequenceFlowsJaxb the jaxb sequence flows 
	 * @return the send task action flow element
	 */
	@Override
	protected FlowElement createSendTaskNode(final String clientId, final TProcess processJaxb, final List<TSubProcess> subProcessesJaxb,
			final TFlowNode flowNodeJaxb, final List<TSequenceFlow> sequenceFlowsJaxb) {

		final String uniqueProcessId = IdService.getUniqueProcessId(clientId, processJaxb);
		final String uniqueFlowNodeId = IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb, flowNodeJaxb);
		
		// create the task action actor
		return new SendTaskNode(uniqueProcessId, uniqueFlowNodeId, 
				new EventDefinitionParameter(clientId, processJaxb, subProcessesJaxb, flowNodeJaxb));

	}
	
	/**
	 * Creates the receive task action.
	 *
	 * @param clientId the client id
	 * @param processJaxb the jaxb process 
	 * @param subProcessesJaxb the jaxb sub processes 
	 * @param flowNodeJaxb the jaxb flow node 
	 * @param sequenceFlowsJaxb the jaxb sequence flows 
	 * @return the receive task action flow element
	 */
	@Override
	protected FlowElement createReceiveTaskNode(final String clientId, final TProcess processJaxb, final List<TSubProcess> subProcessesJaxb,
			final TFlowNode flowNodeJaxb, final List<TSequenceFlow> sequenceFlowsJaxb) {
		
		final String uniqueProcessId = IdService.getUniqueProcessId(clientId, processJaxb);
		final String uniqueFlowNodeId = IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb, flowNodeJaxb);
		
		// create the task action actor
		return new ReceiveTaskNode(uniqueProcessId, uniqueFlowNodeId, 
				new EventDefinitionParameter(clientId, processJaxb, subProcessesJaxb, flowNodeJaxb));
	}
	
	
}
