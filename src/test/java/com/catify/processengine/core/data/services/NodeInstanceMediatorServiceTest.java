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
package com.catify.processengine.core.data.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.catify.processengine.core.data.model.entities.FlowNodeInstance;
import com.catify.processengine.core.services.NodeInstanceMediatorService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:META-INF/spring/spring-context.xml" })
@Transactional
public class NodeInstanceMediatorServiceTest {
	
	@Mock
	private FlowNodeInstanceRepositoryService flowNodeInstanceRepositoryService;
	
    @InjectMocks
    private NodeInstanceMediatorService nodeInstanceMediatorServiceMockInjected = new NodeInstanceMediatorService("uniqueProcessId", "uniqueFlowNodeId");
	
	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
	}
    
	@Test
	public void testNodeInstanceMediatorService() {
		NodeInstanceMediatorService nodeInstanceMediatorService = new NodeInstanceMediatorService();
		assertNotNull(nodeInstanceMediatorService);
	}

	@Test
	public void testNodeInstanceMediatorServiceStringString() {
		NodeInstanceMediatorService nodeInstanceMediatorService = new NodeInstanceMediatorService("uniqueProcessId", "uniqueFlowNodeId");
		assertNotNull(nodeInstanceMediatorService);
		assertEquals("uniqueProcessId", nodeInstanceMediatorService.getUniqueProcessId());
		assertEquals("uniqueFlowNodeId", nodeInstanceMediatorService.getUniqueFlowNodeId());
	}
	
	@Test
	public void testGetNodeInstance() {
		assertNotNull(nodeInstanceMediatorServiceMockInjected);
		
		FlowNodeInstance flowNodeInstance = new FlowNodeInstance("TESTSTATE");
		assertNotNull(flowNodeInstance);
		
		when(flowNodeInstanceRepositoryService.findFlowNodeInstance("uniqueProcessId", "uniqueFlowNodeId", "testInstanceId")).thenReturn(flowNodeInstance);
		
		FlowNodeInstance flowNodeInstanceTest = nodeInstanceMediatorServiceMockInjected.getNodeInstance("testInstanceId");
		assertEquals(flowNodeInstance, flowNodeInstanceTest);
	}

	@Test
	public void testGetState() {
		assertNotNull(nodeInstanceMediatorServiceMockInjected);
		
		FlowNodeInstance flowNodeInstance = new FlowNodeInstance("TESTSTATE");
		assertNotNull(flowNodeInstance);
		
		when(flowNodeInstanceRepositoryService.findFlowNodeInstance("uniqueProcessId", "uniqueFlowNodeId", "testInstanceId")).thenReturn(flowNodeInstance);
		
		String state = nodeInstanceMediatorServiceMockInjected.getState("testInstanceId");
		
		assertEquals("TESTSTATE", state);
	}

	@Test
	public void testGetFiredFlowsNeeded() {
		assertNotNull(nodeInstanceMediatorServiceMockInjected);
		
		FlowNodeInstance flowNodeInstance = new FlowNodeInstance("TESTSTATE", 1);
		assertNotNull(flowNodeInstance);
		
		when(flowNodeInstanceRepositoryService.findFlowNodeInstance("uniqueProcessId", "uniqueFlowNodeId", "testInstanceId")).thenReturn(flowNodeInstance);
		
		int firedFlowsNeeded = nodeInstanceMediatorServiceMockInjected.getFiredFlowsNeeded("testInstanceId");
		
		assertEquals(1, firedFlowsNeeded);
	}

	@Test
	public void testGetSequenceFlowsFired() {
		assertNotNull(nodeInstanceMediatorServiceMockInjected);
		
		FlowNodeInstance flowNodeInstance = new FlowNodeInstance("TESTSTATE", 1);
		assertNotNull(flowNodeInstance);
		flowNodeInstance.setFlowsFired(1);
		
		when(flowNodeInstanceRepositoryService.findFlowNodeInstance("uniqueProcessId", "uniqueFlowNodeId", "testInstanceId")).thenReturn(flowNodeInstance);
		
		int flowsFired = nodeInstanceMediatorServiceMockInjected.getSequenceFlowsFired("testInstanceId");
		
		assertEquals(1, flowsFired);
	}

	@Test
	public void testSetState() {
		assertNotNull(nodeInstanceMediatorServiceMockInjected);
		
		FlowNodeInstance flowNodeInstance = new FlowNodeInstance("TESTSTATE");
		assertNotNull(flowNodeInstance);
		flowNodeInstance.setFlowsFired(1);
		
		when(flowNodeInstanceRepositoryService.findFlowNodeInstance("uniqueProcessId", "uniqueFlowNodeId", "testInstanceId")).thenReturn(flowNodeInstance);
		
		nodeInstanceMediatorServiceMockInjected.setState("testInstanceId", "TESTSTATE");
		
		assertEquals("TESTSTATE", flowNodeInstance.getNodeInstanceState());
	}

	@Test
	public void testSetFiredFlowsNeeded() {
		assertNotNull(nodeInstanceMediatorServiceMockInjected);
		
		FlowNodeInstance flowNodeInstance = new FlowNodeInstance("TESTSTATE", 0);
		assertNotNull(flowNodeInstance);
		
		when(flowNodeInstanceRepositoryService.findFlowNodeInstance("uniqueProcessId", "uniqueFlowNodeId", "testInstanceId")).thenReturn(flowNodeInstance);
		
		nodeInstanceMediatorServiceMockInjected.setFiredFlowsNeeded("testInstanceId", 1);
		
		assertEquals(1, flowNodeInstance.getFiredFlowsNeeded());
	}

	@Test
	public void testSetSequenceFlowsFired() {
		assertNotNull(nodeInstanceMediatorServiceMockInjected);
		
		FlowNodeInstance flowNodeInstance = new FlowNodeInstance("TESTSTATE");
		assertNotNull(flowNodeInstance);
		
		when(flowNodeInstanceRepositoryService.findFlowNodeInstance("uniqueProcessId", "uniqueFlowNodeId", "testInstanceId")).thenReturn(flowNodeInstance);
		
		nodeInstanceMediatorServiceMockInjected.setSequenceFlowsFired("testInstanceId", 1);
		
		assertEquals(1, flowNodeInstance.getFlowsFired());
	}

	@Test
	public void testSetNodeInstanceStartTime() {
		assertNotNull(nodeInstanceMediatorServiceMockInjected);
		
		FlowNodeInstance flowNodeInstance = new FlowNodeInstance("TESTSTATE");
		assertNotNull(flowNodeInstance);
		assertNull(flowNodeInstance.getNodeInstanceStartTime());
		
		Date startTime = new Date(); 

		when(flowNodeInstanceRepositoryService.findFlowNodeInstance("uniqueProcessId", "uniqueFlowNodeId", "testInstanceId")).thenReturn(flowNodeInstance);
		
		nodeInstanceMediatorServiceMockInjected.setNodeInstanceStartTime("testInstanceId", startTime);
		
		assertEquals(startTime, flowNodeInstance.getNodeInstanceStartTime());
	}

	@Test
	public void testSetNodeInstanceEndTime() {
		assertNotNull(nodeInstanceMediatorServiceMockInjected);
		
		FlowNodeInstance flowNodeInstance = new FlowNodeInstance("TESTSTATE");
		assertNotNull(flowNodeInstance);
		assertNull(flowNodeInstance.getNodeInstanceEndTime());
		
		Date endTime = new Date(); 

		when(flowNodeInstanceRepositoryService.findFlowNodeInstance("uniqueProcessId", "uniqueFlowNodeId", "testInstanceId")).thenReturn(flowNodeInstance);
		
		nodeInstanceMediatorServiceMockInjected.setNodeInstanceEndTime("testInstanceId", endTime);
		
		assertEquals(endTime, flowNodeInstance.getNodeInstanceEndTime());
	}

	@Test
	public void testGetUniqueProcessId() {
		assertNotNull(nodeInstanceMediatorServiceMockInjected);
		
		nodeInstanceMediatorServiceMockInjected.setUniqueProcessId("uniqueProcessId");
		
		assertEquals("uniqueProcessId", nodeInstanceMediatorServiceMockInjected.getUniqueProcessId());
	}

	@Test
	public void testSetUniqueProcessId() {
		assertNotNull(nodeInstanceMediatorServiceMockInjected);
		
		nodeInstanceMediatorServiceMockInjected.setUniqueProcessId("uniqueProcessId");
		
		assertEquals("uniqueProcessId", nodeInstanceMediatorServiceMockInjected.getUniqueProcessId());
	}

	@Test
	public void testGetUniqueFlowNodeId() {
		assertNotNull(nodeInstanceMediatorServiceMockInjected);
		
		nodeInstanceMediatorServiceMockInjected.setUniqueFlowNodeId("uniqueFlowNodeId");
		
		assertEquals("uniqueFlowNodeId", nodeInstanceMediatorServiceMockInjected.getUniqueFlowNodeId());
	}

	@Test
	public void testSetUniqueFlowNodeId() {
		assertNotNull(nodeInstanceMediatorServiceMockInjected);
		
		nodeInstanceMediatorServiceMockInjected.setUniqueFlowNodeId("uniqueFlowNodeId");
		
		assertEquals("uniqueFlowNodeId", nodeInstanceMediatorServiceMockInjected.getUniqueFlowNodeId());
	}

	@Test
	public void testIsInitialized() {
		assertNotNull(nodeInstanceMediatorServiceMockInjected);
		
		assertFalse(nodeInstanceMediatorServiceMockInjected.isInitialized());
		
		FlowNodeInstance flowNodeInstance = new FlowNodeInstance("TESTSTATE");
		assertNotNull(flowNodeInstance);

		when(flowNodeInstanceRepositoryService.findFlowNodeInstance("uniqueProcessId", "uniqueFlowNodeId", "testInstanceId")).thenReturn(flowNodeInstance);
		
		// call a method that caches a node instance in the nodeInstanceMediatorService
		nodeInstanceMediatorServiceMockInjected.getState("testInstanceId");
	
		assertTrue(nodeInstanceMediatorServiceMockInjected.isInitialized());
	}

}
