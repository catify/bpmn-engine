/**
 * 
 */
package com.catify.processengine.core.data.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.catify.processengine.core.data.model.entities.ProcessInstanceNode;
import com.catify.processengine.core.data.repositories.ProcessInstanceNodeRepository;
import com.catify.processengine.core.data.services.ProcessInstanceNodeRepositoryService;

/**
 * The ProcessInstanceNodeRepositoryServiceImpl implements the {@link ProcessInstanceNodeRepositoryService}.
 * It therefore uses methods from the Spring Data managed {@link ProcessInstanceNodeRepository}.
 *
 * @author chris
 */
@Component
public class ProcessInstanceNodeRepositoryServiceImpl implements ProcessInstanceNodeRepositoryService {
	
	/** The process instance node repository. */
	@Autowired
	private ProcessInstanceNodeRepository processInstanceNodeRepository;


	/**
	 * Instantiates a new process instance node repository service impl.
	 */
	public ProcessInstanceNodeRepositoryServiceImpl() {
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
