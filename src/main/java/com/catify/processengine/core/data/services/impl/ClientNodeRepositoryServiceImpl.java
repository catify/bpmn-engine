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
package com.catify.processengine.core.data.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.catify.processengine.core.data.model.entities.ClientNode;
import com.catify.processengine.core.data.repositories.ClientNodeRepository;
import com.catify.processengine.core.data.services.ClientNodeRepositoryService;

/**
 * The Class ClientNodeRepositoryServiceImpl implements the {@link ClientNodeRepositoryService}. 
 * It therefore uses methods from the Spring Data managed {@link ClientNodeRepository}.
 * 
 * @author christopher köster
 * 
 */
@Component
public class ClientNodeRepositoryServiceImpl implements ClientNodeRepositoryService {

	@Autowired
	private ClientNodeRepository clientNodeRepository;


	/* (non-Javadoc)
	 * @see com.catify.processengine.core.data.services.ClientNodeRepositoryService#findByUniqueClientId(java.lang.String)
	 */
	@Override
	public ClientNode findByUniqueClientId(String uniqueClientId) {
		return clientNodeRepository.findByPropertyValue("uniqueClientId",
				uniqueClientId);
	}

	/* (non-Javadoc)
	 * @see com.catify.processengine.core.data.services.ClientNodeRepositoryService#delete(java.lang.String)
	 */
	@Override
	public void delete(String uniqueClientId) {

		ClientNode clientNode = findByUniqueClientId(uniqueClientId);

		if (clientNode != null) {
			clientNodeRepository.delete(clientNode);
		}

	}

	/* (non-Javadoc)
	 * @see com.catify.processengine.core.data.services.ClientNodeRepositoryService#save(com.catify.processengine.core.data.model.entities.ClientNode)
	 */
	@Override
	public ClientNode save(ClientNode clientNode) {
		return clientNodeRepository.save(clientNode);
	}

	/* (non-Javadoc)
	 * @see com.catify.processengine.core.data.services.ClientNodeRepositoryService#getOrCreateClientNode(java.lang.String)
	 */
	@Override
	public ClientNode getOrCreateClientNode(String uniqueClientId) {

		ClientNode node = findByUniqueClientId(uniqueClientId);

		if (node == null) {
			node = new ClientNode(uniqueClientId);
		}
		clientNodeRepository.save(node);

		return node;
	}
	
}
