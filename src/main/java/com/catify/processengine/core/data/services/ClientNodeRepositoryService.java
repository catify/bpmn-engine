/**
 * 
 */
package com.catify.processengine.core.data.services;

import org.springframework.stereotype.Component;

import com.catify.processengine.core.data.model.entities.ClientNode;

/**
 * The Interface ClientNodeRepositoryService.
 *
 * @author chris
 */
@Component
public interface ClientNodeRepositoryService {

	/**
	 * Find a flow node by flow node id.
	 * 
	 * @param uniqueClientId
	 *            the flow node id
	 * @return the flow node
	 */
	ClientNode findByUniqueClientId(String uniqueClientId);

	/**
	 * Delete a flow node by id.
	 * 
	 * @param uniqueClientId
	 *            the flow node id
	 */
	void delete(String uniqueClientId);

	/**
	 * Save a process to the database.
	 * 
	 * @param flowNode
	 *            the flow node
	 */
	ClientNode save(ClientNode clientNode);

	/**
	 * Save flow node or retrieve it from the db if it already exists.
	 * 
	 * @param flowNodeJaxb
	 *            the jaxb flow node
	 * @return {@link ClientNode}
	 */
	ClientNode getOrCreateClientNode(String uniqueClientId);
	
}
