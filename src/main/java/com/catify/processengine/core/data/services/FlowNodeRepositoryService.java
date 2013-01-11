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
package com.catify.processengine.core.data.services;

import org.springframework.stereotype.Component;

import com.catify.processengine.core.data.model.entities.FlowNode;

/**
 * The FlowNodeRepositoryService wraps methods of the FlowNodeRepository and
 * provides additional methods that hide implementation details.
 * 
 * @author chris
 */
@Component
public interface FlowNodeRepositoryService {

	/**
	 * Find a flow node by flow node id.
	 * 
	 * @param uniqueFlowNodeId
	 *            the flow node id
	 * @return the flow node
	 */
	FlowNode findByUniqueFlowNodeId(String uniqueFlowNodeId);
	
	/**
	 * Find an archive flow node by the uniqueFlowNodeId of a running flow node.
	 * 
	 * @param uniqueFlowNodeId
	 *            the flow node id
	 * @return the flow node
	 */
	FlowNode findArchivedByRunningUniqueFlowNodeId(String uniqueFlowNodeId);

	/**
	 * delete a flow node by id.
	 * 
	 * @param uniqueFlowNodeId
	 *            the flow node id
	 * @return true, if successful, false if no process with given id found
	 */
	boolean delete(String uniqueFlowNodeId);

	/**
	 * Save a process to the database.
	 * 
	 * @param flowNode
	 *            the flow node
	 * @return the process
	 */
	FlowNode save(FlowNode flowNode);

	/**
	 * Get an existing flow node from the database or create and persist the proposed flow node in the database.
	 *
	 * @param proposedFlowNode the proposed {@link FlowNode}
	 * @return the flow node
	 */
	FlowNode getOrCreateFlowNode(FlowNode proposedFlowNode);

}
