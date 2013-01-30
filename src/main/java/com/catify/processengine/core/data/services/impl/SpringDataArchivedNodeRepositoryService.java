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
package com.catify.processengine.core.data.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.catify.processengine.core.data.model.entities.ArchiveNode;
import com.catify.processengine.core.data.repositories.ArchivedNodeRepository;
import com.catify.processengine.core.data.services.ArchivedNodeRepositoryService;

/**
 * The Class SpringDataArchivedNodeRepositoryService implements the {@link ArchivedNodeRepositoryService}. 
 * It therefore uses methods from the Spring Data managed {@link ArchivedNodeRepository}.
 * 
 * @author christopher köster
 * 
 */
@Component
public class SpringDataArchivedNodeRepositoryService implements
		ArchivedNodeRepositoryService {

	/** The archived node repository. */
	@Autowired
	private ArchivedNodeRepository archivedNodeRepository;
	
	/* (non-Javadoc)
	 * @see com.catify.processengine.core.data.services.ArchivedNodeRepositoryService#findByUniqueClientId(java.lang.String)
	 */
	@Override
	public ArchiveNode findByUniqueClientId(String uniqueClientId) {
		return archivedNodeRepository.findByPropertyValue("uniqueClientId",
				uniqueClientId);
	}

	/* (non-Javadoc)
	 * @see com.catify.processengine.core.data.services.ArchivedNodeRepositoryService#save(com.catify.processengine.core.data.model.entities.ArchiveNode)
	 */
	@Override
	public ArchiveNode save(ArchiveNode archiveNode) {
		return archivedNodeRepository.save(archiveNode);
	}

	/* (non-Javadoc)
	 * @see com.catify.processengine.core.data.services.ArchivedNodeRepositoryService#getOrCreateArchivedNode(java.lang.String)
	 */
	@Override
	public ArchiveNode getOrCreateArchivedNode(String uniqueClientId) {
		ArchiveNode node = findByUniqueClientId(uniqueClientId);

		if (node == null) {
			node = new ArchiveNode(uniqueClientId);
		}
		archivedNodeRepository.save(node);

		return node;
	}



}
