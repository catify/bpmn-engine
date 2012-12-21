/**
 * 
 */
package com.catify.processengine.core.data.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.catify.processengine.core.data.model.entities.RootNode;
import com.catify.processengine.core.data.repositories.RootNodeRepository;
import com.catify.processengine.core.data.services.RootNodeRepositoryService;

/**
 * The RootNodeRepositoryServiceImpl implements the {@link RootNodeRepositoryService}.
 * It therefore uses methods from the Spring Data managed {@link RootNodeRepository}.
 * 
 * @author chris
 *
 */
@Component
public class RootNodeRepositoryServiceImpl implements RootNodeRepositoryService {

	/** The root node repository. */
	@Autowired
	private RootNodeRepository rootNodeRepository;

	/* (non-Javadoc)
	 * @see com.catify.processengine.core.data.services.RootNodeRepositoryService#findByUniqueRootId(java.lang.String)
	 */
	@Override
	public RootNode findByUniqueRootId(String uniqueRootNodeId) {
		return rootNodeRepository.findByPropertyValue("uniqueRootNodeId",
				uniqueRootNodeId);
	}

	/* (non-Javadoc)
	 * @see com.catify.processengine.core.data.services.RootNodeRepositoryService#delete(java.lang.String)
	 */
	@Override
	public boolean delete(String uniqueRootNodeId) {

		RootNode rootNode = findByUniqueRootId(uniqueRootNodeId);

		if (rootNode != null) {
			rootNodeRepository.delete(rootNode);
			return true;
		} else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see com.catify.processengine.core.data.services.RootNodeRepositoryService#save(com.catify.processengine.core.data.model.entities.RootNode)
	 */
	@Override
	public RootNode save(RootNode rootNode) {
		return rootNodeRepository.save(rootNode);
	}

	/* (non-Javadoc)
	 * @see com.catify.processengine.core.data.services.RootNodeRepositoryService#getOrCreateRootNode(java.lang.String)
	 */
	@Override
	public RootNode getOrCreateRootNode(String uniqueRootNodeId) {

		RootNode node = findByUniqueRootId(uniqueRootNodeId);

		if (node == null) {
			node = new RootNode();
		}
		rootNodeRepository.save(node);

		return node;
	}
	
}
