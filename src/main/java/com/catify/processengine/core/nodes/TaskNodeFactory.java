package com.catify.processengine.core.nodes;

import java.util.ArrayList;
import java.util.List;

import com.catify.processengine.core.data.services.IdService;
import com.catify.processengine.core.nodes.eventdefinition.EventDefinitionParameter;
import com.catify.processengine.core.processdefinition.jaxb.TFlowNode;
import com.catify.processengine.core.processdefinition.jaxb.TProcess;
import com.catify.processengine.core.processdefinition.jaxb.TSendTask;
import com.catify.processengine.core.processdefinition.jaxb.TSequenceFlow;
import com.catify.processengine.core.processdefinition.jaxb.TSubProcess;
import com.catify.processengine.core.services.ActorReferenceService;

/**
 * The TaskNodeFactory extends the {@link NodeFactoryImpl} to override
 * the methods that create the tasks in order to return {@link Task}Action actors instead of {@link LoopTaskWrapper} actors returned by the NodeFactoryImpl. 
 */
// TODO: Instead of extending the NodeFactoryImpl, we might create a base class and extend that instead
public class TaskNodeFactory extends NodeFactoryImpl {

	/**
	 * Creates the send task node action.
	 *
	 * @param clientId the client id
	 * @param processJaxb the jaxb process
	 * @param subProcessesJaxb the sub processes jaxb
	 * @param flowNodeJaxb the jaxb parallel gateway node
	 * @param sequenceFlowsJaxb the list of jaxb sequence flows of that process
	 * @return the send task node
	 */
	@Override
	protected FlowElement createSendTaskNode(String clientId, TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb,
			TFlowNode flowNodeJaxb, List<TSequenceFlow> sequenceFlowsJaxb) {

		final TSendTask sendTaskJaxb = (TSendTask) flowNodeJaxb;

		return new SendTaskNode(
				IdService.getUniqueProcessId(clientId, processJaxb),
				IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb,
						sendTaskJaxb), 
				this.getOutgoingActorReferences(clientId, 
						processJaxb, subProcessesJaxb, sendTaskJaxb, sequenceFlowsJaxb),
				ActorReferenceService.getActorReferenceString(
								IdService.getUniqueFlowNodeId(clientId, processJaxb, subProcessesJaxb,
										flowNodeJaxb)),  
				new EventDefinitionParameter(clientId, processJaxb, subProcessesJaxb,flowNodeJaxb), 
				this.getDataObjectHandling(flowNodeJaxb),
				this.getBoundaryEvents(clientId, processJaxb, subProcessesJaxb, flowNodeJaxb));
	}

}
