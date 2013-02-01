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

import com.catify.processengine.core.data.model.entities.ProcessInstanceNode;
import com.catify.processengine.core.data.repositories.ProcessInstanceNodeRepository;
import com.catify.processengine.core.data.services.IdService;
import com.catify.processengine.core.data.services.ProcessInstanceNodeRepositoryService;

/**
 * The ProcessInstanceNodeRepositoryServiceImpl implements the {@link ProcessInstanceNodeRepositoryService}.
 * It therefore uses methods from the Spring Data managed {@link ProcessInstanceNodeRepository}.
 * 
 * @author christopher köster
 * 
 */
@Component
public class SpringDataProcessInstanceNodeRepositoryService implements ProcessInstanceNodeRepositoryService {
	
	/** The process instance node repository. */
	@Autowired
	private ProcessInstanceNodeRepository processInstanceNodeRepository;


	/**
	 * Instantiates a new process instance node repository service impl.
	 */
	public SpringDataProcessInstanceNodeRepositoryService() {
	}

	/* (non-Javadoc)
	 * @see com.catify.processengine.core.data.services.ProcessInstanceNodeRepositoryService#save(com.catify.processengine.core.data.model.entities.ProcessInstanceNode)
	 */
	@Override
	public ProcessInstanceNode save(ProcessInstanceNode processInstanceNode) {
		return processInstanceNodeRepository.save(processInstanceNode);
	}

	/* (non-Javadoc)
	 * @see com.catify.processengine.core.data.services.ProcessInstanceNodeRepositoryService#findProcessInstanceNode(java.lang.String, java.lang.String)
	 */
	@Override
	public ProcessInstanceNode findProcessInstanceNode(String uniqueProcessId, 	String processInstanceId) {
		// invoke cypher query in ProcessInstanceNodeRepository
		return processInstanceNodeRepository.findProcessInstanceNode(uniqueProcessId, processInstanceId);
	}

	/* (non-Javadoc)
	 * @see com.catify.processengine.core.data.services.ProcessInstanceNodeRepositoryService#delete(com.catify.processengine.core.data.model.entities.ProcessInstanceNode)
	 */
	@Override
	public void delete(ProcessInstanceNode processInstanceNode) {
		processInstanceNodeRepository.delete(processInstanceNode);
	}

	/* (non-Javadoc)
	 * @see com.catify.processengine.core.data.services.ProcessInstanceNodeRepositoryService#findArchivedProcessInstanceNode(java.lang.String, java.lang.String)
	 */
	@Override
	public ProcessInstanceNode findArchivedProcessInstanceNode(
			String uniqueProcessId, String processInstanceId) {
		return processInstanceNodeRepository.findProcessInstanceNode(IdService.ARCHIVEPREFIX + uniqueProcessId, processInstanceId);
	}
}
