/**
 * 
 */
package com.catify.processengine.core.data.services;

import org.springframework.stereotype.Component;

import com.catify.processengine.core.data.model.entities.FlowNode;
import com.catify.processengine.core.data.model.entities.RootNode;

/**
 * @author chris
 *
 */
@Component
public interface RootNodeRepositoryService {

	/**
	 * Find a flow node by flow node id.
	 * 
	 * @param uniqueRootId
	 *            the flow node id
	 * @return the flow node
	 */
	RootNode findByUniqueRootId(String uniqueRootNodeId);

	/**
	 * delete a flow node by id.
	 * 
	 * @param uniqueRootId
	 *            the flow node id
	 * @return true, if successful, false if no process with given id found
	 */
	boolean delete(String uniqueRootNodeId);

	/**
	 * Save a process to the database.
	 * 
	 * @param flowNode
	 *            the flow node
	 * @return the process
	 */
	RootNode save(RootNode rootNode);

	/**
	 * Save flow node or retrieve it from the db if it already exists.
	 * 
	 * @param flowNodeJaxb
	 *            the jaxb flow node
	 * @return {@link FlowNode}
	 */
	RootNode getOrCreateRootNode(String uniqueRootNodeId);
	
}
