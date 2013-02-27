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

import com.catify.processengine.core.data.model.ModelTestBase;

public class FlowNodeInstanceRepositoryServiceImplTest extends ModelTestBase {

//	@Autowired
//	private SpringDataFlowNodeInstanceRepositoryService flowNodeInstanceRepositoryServiceImpl;
//	
//	@Test
//	public void testFlowNodeInstanceRepositoryServiceImpl() {
//		assertNotNull(flowNodeInstanceRepositoryServiceImpl);
//	}
//	
//	@Test
//	public void testFindFlowNodeInstanceStringStringString() {
//		FlowNode flowNode = createBaseNodesToFlowNode();
//		FlowNodeInstance flowNodeInstance = createAndSaveFlowNodeInstance(flowNode, "TESTINSTANCE", NodeInstaceStates.ACTIVE_STATE);
//		assertNotNull(flowNodeInstance);
//		
//		FlowNodeInstance flowNodeInstanceFound = flowNodeInstanceRepositoryServiceImpl.findFlowNodeInstance(
//				ModelTestBase.UNIQUEPROCESSID, ModelTestBase.UNIQUE_FLOWNODE_ID, "TESTINSTANCE");
//		
//		assertEquals(flowNodeInstance.getGraphId(), flowNodeInstanceFound.getGraphId());
//	}
//
//	@Test
//	public void testFindFlowNodeInstanceLongString() {
//		FlowNode flowNode = createBaseNodesToFlowNode();
//		FlowNodeInstance flowNodeInstance = createAndSaveFlowNodeInstance(flowNode, "TESTINSTANCE", NodeInstaceStates.ACTIVE_STATE);
//		assertNotNull(flowNodeInstance);
//		
//		FlowNodeInstance flowNodeInstanceFound = flowNodeInstanceRepositoryServiceImpl.findFlowNodeInstance(
//				flowNodeInstance.getFlowNodeOfHasInstanceRelationship().getGraphId(), "TESTINSTANCE");
//		assertEquals(flowNodeInstance.getGraphId(), flowNodeInstanceFound.getGraphId());
//	}
//
//	@Test
//	public void testFindAllFlowNodeInstances() {
//		FlowNode flowNode = createBaseNodesToFlowNode();
//		FlowNodeInstance flowNodeInstance = createAndSaveFlowNodeInstance(flowNode, "TESTINSTANCE", NodeInstaceStates.ACTIVE_STATE);
//		assertNotNull(flowNodeInstance);
//		
//		Set<FlowNodeInstance> flowNodeInstances = flowNodeInstanceRepositoryServiceImpl.findAllFlowNodeInstances(
//				"uniqueProcessId", "TESTINSTANCE");
//		assertNotNull(flowNodeInstances);
//		
//		// the flow node instance should be in the set (and nothing else) 
//		for (FlowNodeInstance flowNodeInstanceFound : flowNodeInstances) {
//			assertEquals(flowNodeInstance.getGraphId(), flowNodeInstanceFound.getGraphId());
//		}
//	}
//
//	@Test
//	public void testDelete() {
//		FlowNode flowNode = createBaseNodesToFlowNode();
//		FlowNodeInstance flowNodeInstance = createAndSaveFlowNodeInstance(flowNode, "TESTINSTANCE", NodeInstaceStates.ACTIVE_STATE);
//		assertNotNull(flowNodeInstance);
//		
//		flowNodeInstanceRepositoryServiceImpl.delete(flowNodeInstance);
//		
//		// using findFlowNodeInstance because neo4jTemplate claims "node deleted in transaction"
////		neo4jTemplate.findOne(flowNodeInstance.getGraphId(), FlowNodeInstance.class);
//		assertNull(flowNodeInstanceRepositoryServiceImpl.findFlowNodeInstance(flowNodeInstance.getGraphId(), "TESTINSTANCE"));
//	}
//
//	@Test
//	public void testSave() {
//		FlowNodeInstance flowNodeInstance = new FlowNodeInstance("TESTING");
//		
//		flowNodeInstanceRepositoryServiceImpl.save(flowNodeInstance);
//		
//		// load the flow node instance from the db by its graph id
//		FlowNodeInstance flowNodeInstanceFound = neo4jTemplate.findOne(flowNodeInstance.getGraphId(), FlowNodeInstance.class);
//		
//		// evaluate if we found the right flow node instance
//		assertNotNull(flowNodeInstanceFound);
//	}
//
//	@Test
//	public void testFindFlowNodeInstancesAtCurrentLevelByState() {
//		FlowNode flowNode = createBaseNodesToFlowNode();
//		FlowNodeInstance flowNodeInstance = createAndSaveFlowNodeInstance(flowNode, "TESTINSTANCE", NodeInstaceStates.ACTIVE_STATE);
//		assertNotNull(flowNodeInstance);
//		
//		Set<FlowNodeInstance> flowNodeInstances = flowNodeInstanceRepositoryServiceImpl.findFlowNodeInstancesAtCurrentLevelByState("uniqueFlowNodeId", "instanceId", "TESTING");
//		assertNotNull(flowNodeInstances);
//		
//		// the flow node instance should be in the set (and nothing else) 
//		for (FlowNodeInstance flowNodeInstanceFound : flowNodeInstances) {
//			assertEquals(flowNodeInstance.getGraphId(), flowNodeInstanceFound.getGraphId());
//		}
//	}
//	
//	@Test
//	public void testFindAllFlowNodeInstancesAtState() {
//		FlowNode flowNode = createBaseNodesToFlowNode();
//		FlowNodeInstance flowNodeInstance = createAndSaveFlowNodeInstance(flowNode, "TESTINSTANCE", NodeInstaceStates.ACTIVE_STATE);
//		assertNotNull(flowNodeInstance);
//		
//		FlowNodeInstance flowNodeInstance2 = createAndSaveFlowNodeInstance(flowNode, "TESTINSTANCE2", NodeInstaceStates.PASSED_STATE);
//		assertNotNull(flowNodeInstance2);
//		
//		Set<String> flowNodeInstanceIds = flowNodeInstanceRepositoryServiceImpl.findAllFlowNodeInstancesAtState("uniqueProcessId", "uniqueFlowNodeId", NodeInstaceStates.ACTIVE_STATE);
//		assertNotNull(flowNodeInstanceIds);
//		assertEquals(1, flowNodeInstanceIds.size());
//	}
//
//	private FlowNode createBaseNodesToFlowNode() {
//		RunningNode runningNode = createAndSaveBaseNodes();
//		ProcessNode processNode = createAndSaveProcessNode(runningNode);
//		return createAndSaveFlowNode(processNode);
//	}
}
