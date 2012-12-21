/**
 * 
 */
package com.catify.processengine.core.data.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.catify.processengine.core.data.model.entities.FlowNode;
import com.catify.processengine.core.data.repositories.FlowNodeRepository;
import com.catify.processengine.core.data.services.FlowNodeRepositoryService;

/**
 * The FlowNodeRepositoryService implements the {@link FlowNodeRepositoryService}.
 * It therefore uses methods from the Spring Data managed {@link FlowNodeRepository}.
 * 
 * @author chris
 */
@Component
public class FlowNodeRepositoryServiceImpl implements FlowNodeRepositoryService {

	/** The flow node repository. */
	@Autowired
	private FlowNodeRepository flowNodeRepository;

	/* (non-Javadoc)
	 * @see com.catify.processengine.core.data.services.FlowNodeRepositoryService#findByUniqueFlowNodeId(java.lang.String)
	 */
	@Override
	public FlowNode findByUniqueFlowNodeId(String uniqueFlowNodeId) {
		return flowNodeRepository.findByPropertyValue("uniqueFlowNodeId",
				uniqueFlowNodeId);
	}
	
	/* (non-Javadoc)
	 * @see com.catify.processengine.core.data.services.FlowNodeRepositoryService#findArchivedByUniqueFlowNodeId(java.lang.String)
	 */
	@Override
	public FlowNode findArchivedByRunningUniqueFlowNodeId(String uniqueFlowNodeId) {
		return flowNodeRepository.findByPropertyValue("uniqueFlowNodeId",
				IdService.ARCHIVEPREFIX + uniqueFlowNodeId);
	}

	/* (non-Javadoc)
	 * @see com.catify.processengine.core.data.services.FlowNodeRepositoryService#delete(java.lang.String)
	 */
	@Override
	public boolean delete(String uniqueFlowNodeId) {

		FlowNode flowNode = findByUniqueFlowNodeId(uniqueFlowNodeId);

		if (flowNode != null) {
			flowNodeRepository.delete(flowNode);
			return true;
		} else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see com.catify.processengine.core.data.services.FlowNodeRepositoryService#save(com.catify.processengine.core.data.model.entities.FlowNode)
	 */
	@Override
	public FlowNode save(FlowNode flowNode) {
		return flowNodeRepository.save(flowNode);
	}

	/* (non-Javadoc)
	 * @see com.catify.processengine.core.data.services.FlowNodeRepositoryService#getOrCreateFlowNode(com.catify.processengine.core.data.model.entities.FlowNode)
	 */
	@Override
	public FlowNode getOrCreateFlowNode(FlowNode proposedFlowNode) {
		
		FlowNode node = findByUniqueFlowNodeId(proposedFlowNode.getUniqueFlowNodeId());

		if (node == null) {
			return this.save(proposedFlowNode);
		}

		return node;
	}
	
}
