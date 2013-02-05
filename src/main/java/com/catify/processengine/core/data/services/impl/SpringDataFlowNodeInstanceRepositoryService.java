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

import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.kernel.impl.traversal.TraversalDescriptionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.catify.processengine.core.data.model.NodeInstaceStates;
import com.catify.processengine.core.data.model.entities.FlowNodeInstance;
import com.catify.processengine.core.data.repositories.FlowNodeInstanceRepository;
import com.catify.processengine.core.data.services.FlowNodeInstanceRepositoryService;

/**
 * The FlowNodeInstanceRepositoryServiceImpl implements the {@link FlowNodeInstanceRepositoryService}.
 * It therefore uses methods from the Spring Data managed {@link FlowNodeInstanceRepository}.
 * 
 * @author christopher köster
 * 
 */
@Component
public class SpringDataFlowNodeInstanceRepositoryService implements FlowNodeInstanceRepositoryService {
	
	static final Logger LOG = LoggerFactory.getLogger(SpringDataFlowNodeInstanceRepositoryService.class);

	/** The flow node instance repository. */
	@Autowired
	private FlowNodeInstanceRepository flowNodeInstanceRepository;

	/**
	 * Instantiates a new flow node instance repository service impl.
	 */
	public SpringDataFlowNodeInstanceRepositoryService() {
	}

	/* (non-Javadoc)
	 * @see com.catify.processengine.core.data.services.FlowNodeInstanceRepositoryService#findFlowNodeInstance(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public FlowNodeInstance findFlowNodeInstance(String uniqueProcessId,
			String uniqueFlowNodeId, String instanceId) {
		// two way query for later evaluation)
//		FlowNode fn = flowNodeRepository.findFlowNode(uniqueProcessId, uniqueFlowNodeId);
//		FlowNodeInstance fni = flowNodeInstanceRepository.findFlowNodeInstance(fn.getGraphId(), instanceId);
		
		// query for the FlowNodeInstance on that process/node combination
		LOG.debug("Executing findFlowNodeInstance with parameters --> processID: " + uniqueProcessId+ ", uniqueFlowNodeId: " + uniqueFlowNodeId+ ", instanceId: " + instanceId);
		
		/*
		 * the do/try block is a bugfix for neo4j, when concurrently reading and deleting nodes.
		 * it will be removed as soon as this has been patched. 
		 */	
		boolean errorCatched = false;

		do {
			try {
				return flowNodeInstanceRepository.findFlowNodeInstance(
						uniqueProcessId, uniqueFlowNodeId, instanceId);
			} catch (org.neo4j.kernel.impl.nioneo.store.InvalidRecordException e) {
				errorCatched = true;
				LOG.debug("Concurrent searching and deleting lead to a InvalidRecordException in findFlowNodeInstance(). This is expected, retrying query.");
			} catch (org.neo4j.cypher.EntityNotFoundException e2) {
				errorCatched = true;
				LOG.debug("Concurrent searching and deleting lead to a EntityNotFoundException in findFlowNodeInstance(). This is expected, retrying query.");
			} catch (org.neo4j.graphdb.NotFoundException e3) {
				errorCatched = true;
				LOG.debug("Concurrent searching and deleting lead to a NotFoundException in findFlowNodeInstance(). This is expected, retrying query.");
			}
		} while (errorCatched);

		return null;
	}

	/* (non-Javadoc)
	 * @see com.catify.processengine.core.data.services.FlowNodeInstanceRepositoryService#findFlowNodeInstance(java.lang.Long, java.lang.String)
	 */
	@Override
	public FlowNodeInstance findFlowNodeInstance(Long flowNodeGraphId,
			String processInstanceId) {
		return flowNodeInstanceRepository.findFlowNodeInstance(flowNodeGraphId,
				processInstanceId);
	}
	
	/* (non-Javadoc)
	 * @see com.catify.processengine.core.data.services.FlowNodeInstanceRepositoryService#findAllFlowNodeInstances(java.lang.String, java.lang.String)
	 */
	@Override
	public Set<FlowNodeInstance> findAllFlowNodeInstances(String uniqueProcessId,
			String processInstanceId) {
		return flowNodeInstanceRepository.findAllFlowNodeInstances(uniqueProcessId, processInstanceId);
	}

	/* (non-Javadoc)
	 * @see com.catify.processengine.core.data.services.FlowNodeInstanceRepositoryService#delete(com.catify.processengine.core.data.model.entities.FlowNodeInstance)
	 */
	@Override
	public void delete(FlowNodeInstance flowNodeInstance) {
			flowNodeInstanceRepository.delete(flowNodeInstance);
	}

	/* (non-Javadoc)
	 * @see com.catify.processengine.core.data.services.FlowNodeInstanceRepositoryService#save(com.catify.processengine.core.data.model.entities.FlowNodeInstance)
	 */
	@Override
	public FlowNodeInstance save(FlowNodeInstance flowNodeInstance) {
		return flowNodeInstanceRepository.save(flowNodeInstance);
	}
	
	/* (non-Javadoc)
	 * @see com.catify.processengine.core.data.services.FlowNodeInstanceRepositoryService#findFlowNodeInstancesAtCurrentLevelByState(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Set<FlowNodeInstance> findFlowNodeInstancesAtCurrentLevelByState(String uniqueFlowNodeId, String instanceId,
			String state) {
		
		/*
		 * the do/try block is a bugfix for neo4j, when concurrently reading and deleting nodes.
		 * it will be removed as soon as this has been patched. 
		 */	
		boolean errorCatched = false;

		do {

			try {
				return flowNodeInstanceRepository
						.findFlowNodeInstancesAtCurrentLevelByState(
								uniqueFlowNodeId, instanceId, state);
			} catch (org.neo4j.kernel.impl.nioneo.store.InvalidRecordException e) {
				errorCatched = true;
				LOG.debug("Concurrend searching and deleting lead to a InvalidRecordException in findFlowNodeInstancesAtCurrentLevelByState(). This is expected, retrying query.");
			} catch (org.neo4j.cypher.EntityNotFoundException e2) {
				errorCatched = true;
				LOG.debug("Concurrend searching and deleting lead to a EntityNotFoundException in findFlowNodeInstancesAtCurrentLevelByState(). This is expected, retrying query.");
			} catch (org.neo4j.graphdb.NotFoundException e3) {
				errorCatched = true;
				LOG.debug("Concurrend searching and deleting lead to a NotFoundException in findFlowNodeInstancesAtCurrentLevelByState(). This is expected, retrying query.");
			
			// although the exception should have been catched ("nested exception is org.neo4j.graphdb.NotFoundException: Relationship[xy] not found.")
				// we need to ctach the parent exception
			} catch (org.springframework.dao.DataRetrievalFailureException e4) {
				errorCatched = true;
				LOG.debug("Concurrend searching and deleting lead to a DataRetrievalFailureException in findFlowNodeInstancesAtCurrentLevelByState(). This is expected, retrying query.");
			}

		} while (errorCatched);

		return null;
	}

	/* (non-Javadoc)
	 * @see com.catify.processengine.core.data.services.FlowNodeInstanceRepositoryService#findLoosingFlowNodeInstances(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Iterable<FlowNodeInstance> findLoosingFlowNodeInstances(
			String uniqueProcessId, String uniqueFlowNodeId, String instanceId) {

		FlowNodeInstance startingNode = this.findFlowNodeInstance(
				uniqueProcessId, uniqueFlowNodeId, instanceId);
		
		return flowNodeInstanceRepository.findAllByTraversal(startingNode,
				getTraversalDescriptionStopOnStatePassed());
	}
	
	/* (non-Javadoc)
	 * @see com.catify.processengine.core.data.services.FlowNodeInstanceRepositoryService#findLoosingFlowNodeIds(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Set<String> findLoosingFlowNodeIds(
			String uniqueProcessId, String uniqueFlowNodeId, String instanceId) {

		FlowNodeInstance startingNode = this.findFlowNodeInstance(
				uniqueProcessId, uniqueFlowNodeId, instanceId);
				
		return flowNodeInstanceRepository.findPreviousFlowNodeIdsNotInGivenStates(
				startingNode.getGraphId(), NodeInstaceStates.PASSED_STATE, NodeInstaceStates.DEACTIVATED_STATE);
	}

	/**
	 * Gets a traversal description which iterates backwards from a flow node
	 * instance and returns all nodes of a path until a passed node is reached.
	 * 
	 * @return the traversal description_stop on state_passed
	 */
	private TraversalDescription getTraversalDescriptionStopOnStatePassed() {
		return new TraversalDescriptionImpl()
				.breadthFirst()
				.relationships(
						DynamicRelationshipType.withName("HAS_FOLLOWING_INSTANCE"),
						Direction.INCOMING)
				.evaluator(new Evaluator() {
							@Override
							public Evaluation evaluate(Path path) {
								
								if (path.length() == 0) {
									return Evaluation.EXCLUDE_AND_CONTINUE;
								}
								
								Node node = path.lastRelationship().getStartNode();
								Object stopProperty = node.getProperty(
										"nodeInstanceState", null);

								if (stopProperty instanceof String
										&& ((String) stopProperty)
												.equals(NodeInstaceStates.PASSED_STATE)) {
									return Evaluation.EXCLUDE_AND_PRUNE;
								} else {								
									return Evaluation.INCLUDE_AND_CONTINUE;
								}
							}
						})
				.evaluator(Evaluators.excludeStartPosition())
				;
	}

	/* (non-Javadoc)
	 * @see com.catify.processengine.core.data.services.FlowNodeInstanceRepositoryService#deleteAllFlowNodeInstanceNodes(java.lang.String, java.lang.String)
	 */
	@Override
	public void deleteAllFlowNodeInstanceNodes(String uniqueProcessId,
			String processInstanceId) {
		// this can be done in one or two steps (implemented both). the two step methos seems to use less ram.
		
		flowNodeInstanceRepository.deleteAllInstanceNodes(uniqueProcessId, processInstanceId);
		
//		flowNodeInstanceRepository.deleteAllFlowNodeInstanceNodesOnly(uniqueProcessId, processInstanceId);
//		flowNodeInstanceRepository.deleteAllProcessInstanceNodes(uniqueProcessId, processInstanceId);
	}
	
	/* (non-Javadoc)
	 * @see com.catify.processengine.core.data.services.FlowNodeInstanceRepositoryService#findAllFlowNodeInstancesAndFlowNodeIds(java.lang.String, java.lang.String)
	 */
	@Override
	public Iterable<Map<String,Object>> findAllFlowNodeInstancesAndFlowNodeIds(String uniqueProcessId, String processInstanceId) {
		return flowNodeInstanceRepository.findAllFlowNodeInstancesAndFlowNodeIds(uniqueProcessId, processInstanceId);
	}

}
