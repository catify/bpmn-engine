package com.catify.processengine.core.data.model.entities;

import java.util.Map;

import com.catify.processengine.core.data.dataobjects.DataObjectIdService;
import com.catify.processengine.core.processdefinition.jaxb.TComplexGateway;
import com.catify.processengine.core.processdefinition.jaxb.TFlowNode;
import com.catify.processengine.core.processdefinition.jaxb.TParallelGateway;
import com.catify.processengine.core.processdefinition.jaxb.services.ExtensionService;

/**
 * A factory for creating FlowNode objects.
 */
public class FlowNodeFactory {
	
	private FlowNodeFactory() {
		
	}

	/**
	 * Creates a new FlowNode object.
	 *
	 * @param flowNodeJaxb the flow node jaxb
	 * @param uniqueFlowNodeId the unique flow node id
	 * @return the flow node
	 */
	public static FlowNode createFlowNode(TFlowNode flowNodeJaxb,
			String uniqueFlowNodeId) {

		FlowNode node = null;

		// the parallel and the complex gateway need to save their
		// activation condition (firedFlowsNeeded)
		if (flowNodeJaxb instanceof TParallelGateway) {
			node = new FlowNode(uniqueFlowNodeId, flowNodeJaxb.getId(),
					flowNodeJaxb.getClass().toString(), flowNodeJaxb.getName(),
					flowNodeJaxb.getIncoming().size());
		} else if (flowNodeJaxb instanceof TComplexGateway) {

			// TODO: the TExpression element should be parsed and handled
			// accordingly, see redmine #95
			int firedFlowsNeeded = ExtensionService.getTNOfM(flowNodeJaxb)
					.getCount();

			node = new FlowNode(uniqueFlowNodeId, flowNodeJaxb.getId(),
					flowNodeJaxb.getClass().toString(), flowNodeJaxb.getName(),
					firedFlowsNeeded);
			
		// the other nodes might have data associations
		} else {
			Map<String, String> objectIds = DataObjectIdService
					.getDataObjectIds(flowNodeJaxb);

			if (objectIds.size() > 0) {
				node = new FlowNode(uniqueFlowNodeId, flowNodeJaxb.getId(),
						flowNodeJaxb.getClass().toString(),
						flowNodeJaxb.getName(),
						objectIds.get(DataObjectIdService.DATAINPUTOBJECTID),
						objectIds.get(DataObjectIdService.DATAOUTPUTOBJECTID));
			} else {
				node = new FlowNode(uniqueFlowNodeId, flowNodeJaxb.getId(),
						flowNodeJaxb.getClass().toString(),
						flowNodeJaxb.getName());
			}
		}

		return node;
	}

}
