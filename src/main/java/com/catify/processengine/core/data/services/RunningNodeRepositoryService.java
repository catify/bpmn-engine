package com.catify.processengine.core.data.services;

import org.springframework.stereotype.Component;

import com.catify.processengine.core.data.model.entities.FlowNode;
import com.catify.processengine.core.data.model.entities.RunningNode;

@Component
public interface RunningNodeRepositoryService {

	/**
	 * Find a running node by client id.
	 * 
	 * @param uniqueClientId
	 *            the flow node id
	 * @return the flow node
	 */
	RunningNode findByUniqueClientId(String uniqueClientId);

	/**
	 * Save a running node to the database.
	 * 
	 * @param flowNode
	 *            the flow node
	 * @return the process
	 */
	RunningNode save(RunningNode runningNode);

	/**
	 * Create a running node or retrieve it from the db if it already exists.
	 * 
	 * @param flowNodeJaxb
	 *            the jaxb flow node
	 * @return {@link FlowNode}
	 */
	RunningNode getOrCreateRunningNode(String uniqueClientId);
	
}
