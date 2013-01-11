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
