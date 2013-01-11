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

import com.catify.processengine.core.data.model.entities.ProcessNode;
import com.catify.processengine.core.data.repositories.ProcessRepository;
import com.catify.processengine.core.data.services.ProcessRepositoryService;

/**
 * The ProcessRepositoryServiceImpl implements the {@link ProcessRepositoryService}.
 * It therefore uses methods from the Spring Data managed {@link ProcessRepository}.
 * 
 * @author chris
 */
@Component
public class ProcessRepositoryServiceImpl implements ProcessRepositoryService {

	/** The process repository. */
	@Autowired
	private ProcessRepository processRepository;

	/* (non-Javadoc)
	 * @see com.catify.processengine.core.data.services.ProcessRepositoryService#findByUniqueProcessId(java.lang.String)
	 */
	@Override
	public ProcessNode findByUniqueProcessId(String uniqueProcessId) {
		return processRepository.findByPropertyValue("uniqueProcessId",
				uniqueProcessId);
	}
	
	/* (non-Javadoc)
	 * @see com.catify.processengine.core.data.services.ProcessRepositoryService#findArchivedByUniqueProcessId(java.lang.String)
	 */
	@Override
	public ProcessNode findArchivedByRunningUniqueProcessId(String uniqueProcessId) {
		return processRepository.findByPropertyValue("uniqueProcessId",
				IdService.ARCHIVEPREFIX + uniqueProcessId);
	}
	
	/* (non-Javadoc)
	 * @see com.catify.processengine.core.data.services.ProcessRepositoryService#save(com.catify.processengine.core.data.model.entities.ProcessNode)
	 */
	@Override
	public ProcessNode save(ProcessNode process) {
		return processRepository.save(process);
	}
	
	/* (non-Javadoc)
	 * @see com.catify.processengine.core.data.services.ProcessRepositoryService#getOrCreateProcessNode(com.catify.processengine.core.data.model.entities.ProcessNode)
	 */
	@Override
	public ProcessNode getOrCreateProcessNode(ProcessNode proposedProcessNode) {
		
		ProcessNode existingProcess = findByUniqueProcessId(proposedProcessNode.getUniqueProcessId());
		
		if (existingProcess == null) {
			return this.save(proposedProcessNode);
		}

		return existingProcess;
	}
	
	/* (non-Javadoc)
	 * @see com.catify.processengine.core.data.services.ProcessRepositoryService#delete(java.lang.String)
	 */
	@Override
	public boolean delete(String uniqueProcessId) {
		ProcessNode process = findByUniqueProcessId(uniqueProcessId);

		if (process != null) {
			processRepository.delete(process);
			return true;
		} else {
			return false;
		}
	}

}
