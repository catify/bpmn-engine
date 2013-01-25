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
package com.catify.processengine.core.nodes;

import java.util.ArrayList;
import java.util.List;

import akka.actor.ActorRef;

import com.catify.processengine.core.data.dataobjects.DataObjectService;
import com.catify.processengine.core.nodes.eventdefinition.SynchronousEventDefinition;
import com.catify.processengine.core.processdefinition.jaxb.TFlowNode;
import com.catify.processengine.core.processdefinition.jaxb.TProcess;
import com.catify.processengine.core.processdefinition.jaxb.TSequenceFlow;
import com.catify.processengine.core.processdefinition.jaxb.TSubProcess;

/**
 * A factory for creating akka node objects. The factory implementation will instantiate an object derived from the {@link FlowElement}.
 * 
 * @author chris
 */
public interface NodeFactory {

	/**
	 * Creates a service node from a jaxb flow node which can be used to create a service node actor
	 * actor.
	 *
	 * @param clientId the client id
	 * @param processJaxb the jaxb process
	 * @param flowNodeJaxb the flow nodeJaxb
	 * @param sequenceFlowsJaxb the list of jaxb sequence flows of that process
	 * @return the start event node
	 */
	FlowElement createServiceNode(String clientId, TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb,
			TFlowNode flowNodeJaxb, List<TSequenceFlow> sequenceFlowsJaxb, ActorRef eventDefinitionActor);
	
	/**
	 * Creates the service task worker node.
	 *
	 * @param uniqueProcessId the unique process id
	 * @param uniqueFlowNodeId the unique flow node id
	 * @param outgoingNodes the outgoing nodes
	 * @param messageEventDefinitionInOut the message event definition in out
	 * @param dataObjectHandling the data object handling
	 * @return the service task worker
	 */
	 FlowElement createServiceTaskWorkerNode(String uniqueProcessId, String uniqueFlowNodeId,
				List<ActorRef> outgoingNodes,
				SynchronousEventDefinition messageEventDefinitionInOut, DataObjectService dataObjectHandling);
}
