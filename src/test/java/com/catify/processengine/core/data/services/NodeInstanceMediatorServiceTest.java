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

/**
 * 
 * @author chris köster
 * @author claus straube
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:META-INF/spring/spring-context.xml" })
@Transactional
public class NodeInstanceMediatorServiceTest {
	
	@Mock
	private FlowNodeInstanceRepositoryService flowNodeInstanceRepositoryService;
	
    @InjectMocks
    private NodeInstanceMediatorService nodeInstanceMediatorServiceMockInjected = new NodeInstanceMediatorService(UPID, UFID);
	
	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
	}
	
	private static final String UPID = "uniqueProcessId";
	private static final String UFID = "uniqueFlowNodeId";
	private static final String UIID = "testInstanceId";
    
	@Test
	public void testNodeInstanceMediatorService() {
		NodeInstanceMediatorService nodeInstanceMediatorService = new NodeInstanceMediatorService();
		assertNotNull(nodeInstanceMediatorService);
	}

	@Test
	public void testNodeInstanceMediatorServiceStringString() {
		NodeInstanceMediatorService nodeInstanceMediatorService = new NodeInstanceMediatorService(UPID, UFID);
		assertNotNull(nodeInstanceMediatorService);
		assertEquals(UPID, nodeInstanceMediatorService.getUniqueProcessId());
		assertEquals(UFID, nodeInstanceMediatorService.getUniqueFlowNodeId());
	}
	
	@Test
	public void testGetNodeInstance() {
		assertNotNull(nodeInstanceMediatorServiceMockInjected);
		
		FlowNodeInstance flowNodeInstance = new FlowNodeInstance("TESTSTATE", 0, 0);
		assertNotNull(flowNodeInstance);
		
		when(flowNodeInstanceRepositoryService.findFlowNodeInstance(UPID, UFID, UIID, 0)).thenReturn(flowNodeInstance);
		
		FlowNodeInstance flowNodeInstanceTest = nodeInstanceMediatorServiceMockInjected.getNodeInstance(UIID);
		assertEquals(flowNodeInstance, flowNodeInstanceTest);
	}

	@Test
	public void testGetState() {
		assertNotNull(nodeInstanceMediatorServiceMockInjected);
		
		FlowNodeInstance flowNodeInstance = new FlowNodeInstance("TESTSTATE", 0, 0);
		assertNotNull(flowNodeInstance);
		
		when(flowNodeInstanceRepositoryService.findFlowNodeInstance(UPID, UFID, UIID, 0)).thenReturn(flowNodeInstance);
		
		String state = nodeInstanceMediatorServiceMockInjected.getNodeInstanceState(UIID);
		
		assertEquals("TESTSTATE", state);
	}

	@Test
	public void testGetFiredFlowsNeeded() {
		assertNotNull(nodeInstanceMediatorServiceMockInjected);
		
		FlowNodeInstance flowNodeInstance = new FlowNodeInstance("TESTSTATE", 1, 0);
		assertNotNull(flowNodeInstance);
		
		when(flowNodeInstanceRepositoryService.findFlowNodeInstance(UPID, UFID, UIID, 0)).thenReturn(flowNodeInstance);
		
		int firedFlowsNeeded = nodeInstanceMediatorServiceMockInjected.getIncomingFiredFlowsNeeded(UIID);
		
		assertEquals(1, firedFlowsNeeded);
	}

	@Test
	public void testGetSequenceFlowsFired() {
		assertNotNull(nodeInstanceMediatorServiceMockInjected);
		
		FlowNodeInstance flowNodeInstance = new FlowNodeInstance("TESTSTATE", 0, 0);
		assertNotNull(flowNodeInstance);
		flowNodeInstance.setFlowsFired(1);
		
		when(flowNodeInstanceRepositoryService.findFlowNodeInstance(UPID, UFID, UIID, 0)).thenReturn(flowNodeInstance);
		
		int flowsFired = nodeInstanceMediatorServiceMockInjected.getSequenceFlowsFired(UIID);
		
		assertEquals(1, flowsFired);
	}

	@Test
	public void testSetState() {
		assertNotNull(nodeInstanceMediatorServiceMockInjected);
		
		FlowNodeInstance flowNodeInstance = new FlowNodeInstance("TESTSTATE", 0, 0);
		assertNotNull(flowNodeInstance);
		flowNodeInstance.setFlowsFired(1);
		
		when(flowNodeInstanceRepositoryService.findFlowNodeInstance(UPID, UFID, UIID, 0)).thenReturn(flowNodeInstance);
		
		nodeInstanceMediatorServiceMockInjected.setState(UIID, "TESTSTATE");
		
		assertEquals("TESTSTATE", flowNodeInstance.getNodeInstanceState());
	}

	@Test
	public void testSetFiredFlowsNeeded() {
		assertNotNull(nodeInstanceMediatorServiceMockInjected);
		
		FlowNodeInstance flowNodeInstance = new FlowNodeInstance("TESTSTATE", 0, 0);
		assertNotNull(flowNodeInstance);
		
		when(flowNodeInstanceRepositoryService.findFlowNodeInstance(UPID, UFID, UIID, 0)).thenReturn(flowNodeInstance);
		
		nodeInstanceMediatorServiceMockInjected.setFiredFlowsNeeded(UIID, 1);
		
		assertEquals(1, flowNodeInstance.getIncomingFiredFlowsNeeded());
	}

	@Test
	public void testSetSequenceFlowsFired() {
		assertNotNull(nodeInstanceMediatorServiceMockInjected);
		
		FlowNodeInstance flowNodeInstance = new FlowNodeInstance("TESTSTATE", 0, 0);
		assertNotNull(flowNodeInstance);
		
		when(flowNodeInstanceRepositoryService.findFlowNodeInstance(UPID, UFID, UIID, 0)).thenReturn(flowNodeInstance);
		
		nodeInstanceMediatorServiceMockInjected.setSequenceFlowsFired(UIID, 1);
		
		assertEquals(1, flowNodeInstance.getFlowsFired());
	}

	@Test
	public void testSetNodeInstanceStartTime() {
		assertNotNull(nodeInstanceMediatorServiceMockInjected);
		
		FlowNodeInstance flowNodeInstance = new FlowNodeInstance("TESTSTATE", 0, 0);
		assertNotNull(flowNodeInstance);
		assertNull(flowNodeInstance.getNodeInstanceStartTime());
		
		Date startTime = new Date(); 

		when(flowNodeInstanceRepositoryService.findFlowNodeInstance(UPID, UFID, UIID, 0)).thenReturn(flowNodeInstance);
		
		nodeInstanceMediatorServiceMockInjected.setNodeInstanceStartTime(UIID, startTime);
		
		assertEquals(startTime, flowNodeInstance.getNodeInstanceStartTime());
	}

	@Test
	public void testSetNodeInstanceEndTime() {
		assertNotNull(nodeInstanceMediatorServiceMockInjected);
		
		FlowNodeInstance flowNodeInstance = new FlowNodeInstance("TESTSTATE", 0, 0);
		assertNotNull(flowNodeInstance);
		assertNull(flowNodeInstance.getNodeInstanceEndTime());
		
		Date endTime = new Date(); 

		when(flowNodeInstanceRepositoryService.findFlowNodeInstance(UPID, UFID, UIID, 0)).thenReturn(flowNodeInstance);
		
		nodeInstanceMediatorServiceMockInjected.setNodeInstanceEndTime(UIID, endTime);
		
		assertEquals(endTime, flowNodeInstance.getNodeInstanceEndTime());
	}

	@Test
	public void testGetUniqueProcessId() {
		assertNotNull(nodeInstanceMediatorServiceMockInjected);
		
		nodeInstanceMediatorServiceMockInjected.setUniqueProcessId(UPID);
		
		assertEquals(UPID, nodeInstanceMediatorServiceMockInjected.getUniqueProcessId());
	}

	@Test
	public void testSetUniqueProcessId() {
		assertNotNull(nodeInstanceMediatorServiceMockInjected);
		
		nodeInstanceMediatorServiceMockInjected.setUniqueProcessId(UPID);
		
		assertEquals(UPID, nodeInstanceMediatorServiceMockInjected.getUniqueProcessId());
	}

	@Test
	public void testGetUniqueFlowNodeId() {
		assertNotNull(nodeInstanceMediatorServiceMockInjected);
		
		nodeInstanceMediatorServiceMockInjected.setUniqueFlowNodeId(UFID);
		
		assertEquals(UFID, nodeInstanceMediatorServiceMockInjected.getUniqueFlowNodeId());
	}

	@Test
	public void testSetUniqueFlowNodeId() {
		assertNotNull(nodeInstanceMediatorServiceMockInjected);
		
		nodeInstanceMediatorServiceMockInjected.setUniqueFlowNodeId(UFID);
		
		assertEquals(UFID, nodeInstanceMediatorServiceMockInjected.getUniqueFlowNodeId());
	}

	@Test
	public void testIsInitialized() {
		assertNotNull(nodeInstanceMediatorServiceMockInjected);
		
		assertFalse(nodeInstanceMediatorServiceMockInjected.isInitialized());
		
		FlowNodeInstance flowNodeInstance = new FlowNodeInstance("TESTSTATE", 0, 0);
		assertNotNull(flowNodeInstance);

		when(flowNodeInstanceRepositoryService.findFlowNodeInstance(UPID, UFID, UIID, 0)).thenReturn(flowNodeInstance);
		
		// call a method that caches a node instance in the nodeInstanceMediatorService
		nodeInstanceMediatorServiceMockInjected.getNodeInstanceState(UIID);
	
		assertTrue(nodeInstanceMediatorServiceMockInjected.isInitialized());
	}

}
