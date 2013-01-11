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
package com.catify.processengine.core.data.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.catify.processengine.core.data.model.entities.FlowNode;
import com.catify.processengine.core.data.model.entities.ProcessNode;
import com.catify.processengine.core.data.repositories.ProcessRepository;

/**
 * As this is a DAO only the critical methods will be tested (mostly spring data involving methods). 
 * 
 * @author chris
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:META-INF/spring/spring-context.xml" })
@Transactional
public class ProcessNodeTest extends ModelTestBase {

	@Autowired
	private ProcessRepository processRepository;

	@Autowired
	Neo4jTemplate template;
	
	@Test
	public void testSaveAndLoad() {

		ProcessNode process = createProcessNode(CLIENT_ID, PROCESS_ID, PROCESS_NAME,
				PROCESS_VERSION);

		assertNotNull(process);

		processRepository.save(process);

		process = processRepository.findByPropertyValue("uniqueProcessId",
				process.getUniqueProcessId());

		assertEquals(PROCESS_ID, process.getProcessId());
		assertEquals(PROCESS_NAME, process.getProcessName());
		assertEquals(PROCESS_VERSION, process.getProcessVersion());
	}

	@Test
	public void testAddRelationshipToFlowNode() {

		ProcessNode process = createProcessNode(CLIENT_ID, PROCESS_ID, PROCESS_NAME,
				PROCESS_VERSION);
		assertNotNull(process);

		// create FlowNode
		FlowNode flowNode = createFlowNode(UNIQUE_FLOWNODE_ID, FLOWNODE_ID,
				FlOWNODE_TYPE, FLOWNODE_NAME);

		// create the relationship
		process.addRelationshipToFlowNode(flowNode);
		process = processRepository.save(process);
		
		// get all relationships from the db
		ArrayList<FlowNode> flowNodeSet = new ArrayList<FlowNode>(
				template.fetch(process.getFlowNodes()));

		// check if it could be added successfully
		assertEquals(FLOWNODE_ID, flowNodeSet.get(0).getFlowNodeId());
	}
}
