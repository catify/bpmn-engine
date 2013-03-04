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
package com.catify.processengine.core.data.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.catify.processengine.core.data.model.entities.ClientNode;
import com.catify.processengine.core.data.model.entities.FlowNode;
import com.catify.processengine.core.data.model.entities.FlowNodeInstance;
import com.catify.processengine.core.data.model.entities.ProcessNode;
import com.catify.processengine.core.data.model.entities.RootNode;
import com.catify.processengine.core.data.model.entities.RunningNode;

/**
 * @author chris
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:META-INF/spring/spring-context.xml" })
@Transactional
public class ModelTestBase {

	/** The neo4j template. */
	@Autowired
	public Neo4jTemplate neo4jTemplate;
	
	public static String CLIENT_ID = "clientId";
	
	public static String UNIQUEPROCESSID = "uniqueProcessId";
	public static String PROCESS_ID = "processId";
	public static String PROCESS_NAME = "processName";
	public static String PROCESS_VERSION = "processVersion";

	public static String UNIQUE_FLOWNODE_ID = "uniqueFlowNodeId";
	public static String FLOWNODE_ID = "flowNodeId";
	public static String FlOWNODE_TYPE = "typeOfNode";
	public static String FLOWNODE_NAME = "name";

	public ProcessNode createProcessNode(String uniqueProcessId, String processId,
			String processName, String processVersion) {

		ProcessNode process = new ProcessNode(uniqueProcessId, processId, processName,
				processVersion);

		assertNotNull(process);

		assertEquals(processId, process.getProcessId());
		assertEquals(processName, process.getProcessName());
		assertEquals(processVersion, process.getProcessVersion());

		return process;
	}

	public FlowNode createFlowNode(String uniqueFlowNodeId,
			String flowNodeId, String typeOfNode, String name) {

		FlowNode flowNode = new FlowNode(uniqueFlowNodeId, flowNodeId,
				typeOfNode, name);

		assertNotNull(flowNode);

		assertEquals(uniqueFlowNodeId, flowNode.getUniqueFlowNodeId());
		assertEquals(flowNodeId, flowNode.getFlowNodeId());
		assertEquals(typeOfNode, flowNode.getNodeType());
		assertEquals(name, flowNode.getName());

		return flowNode;
	}

	public FlowNodeInstance createFlowNodeInstance(String state) {

		FlowNodeInstance flowNodeInstance = new FlowNodeInstance(state, 0, 0);

		assertNotNull(flowNodeInstance);

		return flowNodeInstance;
	}

	public RunningNode createAndSaveBaseNodes() {
		// create nodes
		RootNode rootNode = new RootNode();
		ClientNode clientNode = new ClientNode(CLIENT_ID);
		RunningNode runningNode = new RunningNode();

		neo4jTemplate.save(rootNode);
		neo4jTemplate.save(clientNode);
		neo4jTemplate.save(runningNode);
		
		// create base relationships
		rootNode.addRelationshipToClientNode(clientNode);
		clientNode.addRelationshipToRunningProcessNode(runningNode);
		
		neo4jTemplate.save(rootNode);
		neo4jTemplate.save(clientNode);
		neo4jTemplate.save(runningNode);
		return runningNode;
	}

	public ProcessNode createAndSaveProcessNode(RunningNode runningNode) {
		ProcessNode processNode = createProcessNode(UNIQUEPROCESSID, PROCESS_ID, PROCESS_NAME, PROCESS_VERSION);
		runningNode.addRelationshipToProcessNode(processNode);
		
		neo4jTemplate.save(runningNode);
		neo4jTemplate.save(processNode);
		return processNode;
	}
	
	public FlowNode createAndSaveFlowNode(ProcessNode processNode) {
		// create a relationship between process node and flow node
		FlowNode flowNode = createFlowNode(UNIQUE_FLOWNODE_ID, FLOWNODE_ID, FlOWNODE_TYPE, FLOWNODE_NAME);
		processNode.addRelationshipToFlowNode(flowNode);
		
		neo4jTemplate.save(processNode);
		neo4jTemplate.save(flowNode);
		return flowNode;
	}
	
	public FlowNodeInstance createAndSaveFlowNodeInstance(FlowNode flowNode, String instanceId, String state) {
		
		// create a relationship between flow node and instance
		FlowNodeInstance flowNodeInstance = createFlowNodeInstance(state);
		flowNodeInstance.addAsInstanceOf(flowNode, instanceId);
		
		// save the nodes to the db
		neo4jTemplate.save(flowNode);
		neo4jTemplate.save(flowNodeInstance);
		
		return flowNodeInstance;
	}
}
