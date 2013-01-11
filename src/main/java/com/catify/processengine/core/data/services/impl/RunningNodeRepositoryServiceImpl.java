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
package com.catify.processengine.core.data.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.catify.processengine.core.data.model.entities.RunningNode;
import com.catify.processengine.core.data.repositories.RunningNodeRepository;
import com.catify.processengine.core.data.services.RunningNodeRepositoryService;

/**
 * The RunningNodeRepositoryServiceImpl implements the {@link RunningNodeRepositoryService}.
 * It therefore uses methods from the Spring Data managed {@link RunningNodeRepository}.
 * 
 */
@Component
public class RunningNodeRepositoryServiceImpl implements RunningNodeRepositoryService {

	/** The running node repository. */
	@Autowired
	private RunningNodeRepository runningNodeRepository;
	
	/* (non-Javadoc)
	 * @see com.catify.processengine.core.data.services.RunningNodeRepositoryService#findByUniqueClientId(java.lang.String)
	 */
	@Override
	public RunningNode findByUniqueClientId(String uniqueClientId) {
		return runningNodeRepository.findByPropertyValue("uniqueClientId",
				uniqueClientId);
	}

	/* (non-Javadoc)
	 * @see com.catify.processengine.core.data.services.RunningNodeRepositoryService#save(com.catify.processengine.core.data.model.entities.RunningNode)
	 */
	@Override
	public RunningNode save(RunningNode runningNode) {
		return runningNodeRepository.save(runningNode);
	}

	/* (non-Javadoc)
	 * @see com.catify.processengine.core.data.services.RunningNodeRepositoryService#getOrCreateRunningNode(java.lang.String)
	 */
	@Override
	public RunningNode getOrCreateRunningNode(String uniqueClientId) {
		RunningNode node = findByUniqueClientId(uniqueClientId);

		if (node == null) {
			node = new RunningNode(uniqueClientId);
		}
		runningNodeRepository.save(node);

		return node;
	}

}
