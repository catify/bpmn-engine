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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.catify.processengine.core.data.model.ModelTestBase;
import com.catify.processengine.core.data.model.entities.ClientNode;
import com.catify.processengine.core.data.model.entities.FlowNode;
import com.catify.processengine.core.data.model.entities.FlowNodeInstance;
import com.catify.processengine.core.data.model.entities.ProcessNode;
import com.catify.processengine.core.data.model.entities.RootNode;
import com.catify.processengine.core.data.model.entities.RunningNode;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:META-INF/spring/spring-context.xml" })
@Transactional
public class FlowNodeInstanceRepositoryServiceImplTest {

	@Autowired
	private FlowNodeInstanceRepositoryServiceImpl flowNodeInstanceRepositoryServiceImpl;
	
	/** The neo4j template. */
	@Autowired
	private Neo4jTemplate neo4jTemplate;
	
	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testFlowNodeInstanceRepositoryServiceImpl() {
		assertNotNull(flowNodeInstanceRepositoryServiceImpl);
	}

	private FlowNodeInstance createAndSaveFlowNodeInstance(String instanceId) {
		// create nodes
		RootNode rootNode = new RootNode();
		ClientNode clientNode = new ClientNode("uniqueClientId");
		RunningNode runningNode = new RunningNode();
		
		ModelTestBase model = new ModelTestBase();
		ProcessNode processNode = model.createProcessNode("uniqueProcessId", "processId", "processName", "processVersion");
		FlowNode flowNode = model.createFlowNode("uniqueFlowNodeId", "flowNodeId", "typeOfNode", "name");
		FlowNodeInstance flowNodeInstance = model.createFlowNodeInstance();
		
		// create base relationships
		rootNode.addRelationshipToClientNode(clientNode);
		clientNode.addRelationshipToRunningProcessNode(runningNode);
		runningNode.addRelationshipToProcessNode(processNode);
		
		// create a relationship between process node and flow node
		processNode.addRelationshipToFlowNode(flowNode);
		// create a relationship between flow node and instance
		flowNodeInstance.addAsInstanceOf(flowNode, instanceId);
		
		// save the nodes to the db
		neo4jTemplate.save(rootNode);
		neo4jTemplate.save(clientNode);
		neo4jTemplate.save(runningNode);
		neo4jTemplate.save(processNode);
		neo4jTemplate.save(flowNode);
		neo4jTemplate.save(flowNodeInstance);
		
		return flowNodeInstance;
	}

	
	@Test
	public void testFindFlowNodeInstanceStringStringString() {
		FlowNodeInstance flowNodeInstance = createAndSaveFlowNodeInstance("TESTINSTANCE");
		assertNotNull(flowNodeInstance);
		
		FlowNodeInstance flowNodeInstanceFound = flowNodeInstanceRepositoryServiceImpl.findFlowNodeInstance("uniqueProcessId", "uniqueFlowNodeId", "TESTINSTANCE");
		
		assertEquals(flowNodeInstance.getGraphId(), flowNodeInstanceFound.getGraphId());
	}

	@Test
	public void testFindFlowNodeInstanceLongString() {
		FlowNodeInstance flowNodeInstance = createAndSaveFlowNodeInstance("TESTINSTANCE");
		assertNotNull(flowNodeInstance);
		
		FlowNodeInstance flowNodeInstanceFound = flowNodeInstanceRepositoryServiceImpl.findFlowNodeInstance(flowNodeInstance.getFlowNodeOfHasInstanceRelationship().getGraphId(), "TESTINSTANCE");
		assertEquals(flowNodeInstance.getGraphId(), flowNodeInstanceFound.getGraphId());
	}

	@Test
	public void testFindAllFlowNodeInstances() {
		FlowNodeInstance flowNodeInstance = createAndSaveFlowNodeInstance("TESTINSTANCE");
		assertNotNull(flowNodeInstance);
		
		Set<FlowNodeInstance> flowNodeInstances = flowNodeInstanceRepositoryServiceImpl.findAllFlowNodeInstances("uniqueProcessId", "TESTINSTANCE");
		assertNotNull(flowNodeInstances);
		
		// the flow node instance should be in the set (and nothing else) 
		for (FlowNodeInstance flowNodeInstanceFound : flowNodeInstances) {
			assertEquals(flowNodeInstance.getGraphId(), flowNodeInstanceFound.getGraphId());
		}
	}

	@Test
	public void testDelete() {
		FlowNodeInstance flowNodeInstance = createAndSaveFlowNodeInstance("TESTINSTANCE");
		assertNotNull(flowNodeInstance);
		
		flowNodeInstanceRepositoryServiceImpl.delete(flowNodeInstance);
		
		// using findFlowNodeInstance because neo4jTemplate claims "node deleted in transaction"
//		neo4jTemplate.findOne(flowNodeInstance.getGraphId(), FlowNodeInstance.class);
		assertNull(flowNodeInstanceRepositoryServiceImpl.findFlowNodeInstance(flowNodeInstance.getGraphId(), "TESTINSTANCE"));
	}

	@Test
	public void testSave() {
		FlowNodeInstance flowNodeInstance = new FlowNodeInstance("TESTING");
		
		flowNodeInstanceRepositoryServiceImpl.save(flowNodeInstance);
		
		// load the flow node instance from the db by its graph id
		FlowNodeInstance flowNodeInstanceFound = neo4jTemplate.findOne(flowNodeInstance.getGraphId(), FlowNodeInstance.class);
		
		// evaluate if we found the right flow node instance
		assertNotNull(flowNodeInstanceFound);
	}

	@Test
	public void testFindFlowNodeInstancesAtCurrentLevelByState() {
		FlowNodeInstance flowNodeInstance = createAndSaveFlowNodeInstance("TESTINSTANCE");
		assertNotNull(flowNodeInstance);
		
		Set<FlowNodeInstance> flowNodeInstances = flowNodeInstanceRepositoryServiceImpl.findFlowNodeInstancesAtCurrentLevelByState("uniqueFlowNodeId", "instanceId", "TESTING");
		assertNotNull(flowNodeInstances);
		
		// the flow node instance should be in the set (and nothing else) 
		for (FlowNodeInstance flowNodeInstanceFound : flowNodeInstances) {
			assertEquals(flowNodeInstance.getGraphId(), flowNodeInstanceFound.getGraphId());
		}
	}

	@Test
	public void testFindLoosingFlowNodeInstances() {
		fail("Not yet implemented");
	}

	@Test
	public void testFindLoosingFlowNodeIds() {
		fail("Not yet implemented");
	}

	@Test
	public void testDeleteAllFlowNodeInstanceNodes() {
		fail("Not yet implemented");
	}

	@Test
	public void testFindAllFlowNodeInstancesAndFlowNodeIds() {
		fail("Not yet implemented");
	}

}
