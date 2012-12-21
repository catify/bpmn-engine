package com.catify.processengine.core.data.services;

import org.springframework.stereotype.Component;

import com.catify.processengine.core.data.model.entities.ArchiveNode;
import com.catify.processengine.core.data.model.entities.FlowNode;

/**
 * The Interface ArchivedNodeRepositoryService.
 */
@Component
public interface ArchivedNodeRepositoryService {

	/**
	 * Find an archive node by client id.
	 * 
	 * @param uniqueClientId
	 *            the flow node id
	 * @return the flow node
	 */
	ArchiveNode findByUniqueClientId(String uniqueClientId);

	/**
	 * Save an archive node to the database.
	 * 
	 * @param flowNode
	 *            the flow node
	 * @return the process
	 */
	ArchiveNode save(ArchiveNode archiveNode);

	/**
	 * Create an archive node or retrieve it from the db if it already exists.
	 * 
	 * @param flowNodeJaxb
	 *            the jaxb flow node
	 * @return {@link FlowNode}
	 */
	ArchiveNode getOrCreateArchivedNode(String uniqueClientId);
}
