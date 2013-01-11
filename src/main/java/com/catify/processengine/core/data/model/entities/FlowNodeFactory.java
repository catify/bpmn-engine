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
