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
