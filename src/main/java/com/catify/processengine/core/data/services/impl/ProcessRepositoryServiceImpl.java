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
