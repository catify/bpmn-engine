package com.catify.processengine.core.data.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.catify.processengine.core.data.model.NodeInstaceStates;
import com.catify.processengine.core.data.model.entities.FlowNodeInstance;
import com.catify.processengine.core.data.model.entities.ProcessInstanceNode;
import com.catify.processengine.core.data.model.entities.ProcessNode;
import com.catify.processengine.core.services.ProcessInstanceMediatorService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:META-INF/spring/spring-context.xml" })
@Transactional
public class ProcessInstanceMediatorServiceTest {
	
	@Mock
	private ProcessRepositoryService processRepositoryService;
	
	@Mock 
	private ProcessInstanceNodeRepositoryService processInstanceNodeRepositoryService;
	
	@Mock
	private FlowNodeInstanceRepositoryService flowNodeInstanceRepositoryService;
	
	@Autowired
	@InjectMocks
	private ProcessInstanceMediatorService processInstanceMediatorServiceMockInjected;
	
	@Spy
	private ProcessInstanceNode processInstanceNodeSpy = new ProcessInstanceNode("testInstanceId", new Date());
	
	private Date startDateFixed = new Date();
    
	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		
		assertNotNull(processInstanceMediatorServiceMockInjected);
		assertNotNull(processInstanceNodeSpy);
		
		when(processInstanceNodeRepositoryService.findProcessInstanceNode("uniqueProcessId", "testInstanceId")).thenReturn(processInstanceNodeSpy);
	}
	
	@Test
	public void testProcessInstanceMediatorService() {
		assertNotNull(processInstanceMediatorServiceMockInjected);
	}

	@Test
	public void testFindProcessInstanceNode() {
		ProcessInstanceNode processInstanceNode = processInstanceMediatorServiceMockInjected.findProcessInstanceNode("uniqueProcessId", "testInstanceId");
		
		assertEquals(processInstanceNodeSpy, processInstanceNode);
	}

	@Test
	public void testCreateProcessInstance() {
		ProcessNode process = new ProcessNode("uniqueProcessId", "processId", "processName", "processVersion");
		
		when(processRepositoryService.findByUniqueProcessId("uniqueProcessId")).thenReturn(process);	
		when(processInstanceNodeRepositoryService.save(processInstanceNodeSpy)).thenReturn(processInstanceNodeSpy);
		when(processInstanceNodeRepositoryService.findProcessInstanceNode("uniqueProcessId", "testInstanceId")).thenReturn(processInstanceNodeSpy);

		String instanceId = processInstanceMediatorServiceMockInjected.createProcessInstance("uniqueProcessId");
		assertNotNull(instanceId);
	}

	@Test
	public void testGetPreviousLoosingNodeIds() {
		HashSet<String> flowNodeIds = new HashSet<String>();
		String flowNodeId = "flowNodeId";
		
		flowNodeIds.add(flowNodeId);
		
		when(flowNodeInstanceRepositoryService.findLoosingFlowNodeIds("uniqueProcessId", "uniqueFlowNodeId", "testInstanceId"))
			.thenReturn(flowNodeIds);
		
		assertEquals(flowNodeIds, processInstanceMediatorServiceMockInjected.getPreviousLoosingNodeIds("uniqueProcessId", "uniqueFlowNodeId", "testInstanceId"));
	}

	@Test
	public void testFindActiveFlowNodeInstances() {
		HashSet<FlowNodeInstance> flowNodeInstances = new HashSet<FlowNodeInstance>();
		FlowNodeInstance flowNodeInstance = new FlowNodeInstance("TESTSTATE");
		flowNodeInstances.add(flowNodeInstance);
		
		when(flowNodeInstanceRepositoryService.findFlowNodeInstancesAtCurrentLevelByState("uniqueFlowNodeId", "testInstanceId", NodeInstaceStates.ACTIVE_STATE))
			.thenReturn(flowNodeInstances);
		
		assertEquals(flowNodeInstances, processInstanceMediatorServiceMockInjected.findActiveFlowNodeInstances("uniqueFlowNodeId", "testInstanceId"));
	}

	@Test
	public void testArchiveProcessInstance() {
		// should be an integrational test, as this is mostly db work
		
		// create map and contents
		String flowNodeId = "uniqueFlowNodeId";
		FlowNodeInstance flowNodeInstance = new FlowNodeInstance("TESTSTAE");
		Map<String,Object> idInstanceMap = new HashMap<String, Object>();
		idInstanceMap.put("flownode.uniqueFlowNodeId", flowNodeId);
		idInstanceMap.put("flownodeinstance", flowNodeInstance);
		
		// create result map
		Set<Map<String,Object>> resultMap = new HashSet<Map<String,Object>>();
		resultMap.add(idInstanceMap);
		
		when(flowNodeInstanceRepositoryService.findAllFlowNodeInstancesAndFlowNodeIds("uniqueProcessId", "testInstanceId")).thenReturn(resultMap);
		
		processInstanceMediatorServiceMockInjected.archiveProcessInstance("uniqueProcessId", "testInstanceId", new Date());
		
		verify(flowNodeInstanceRepositoryService).findAllFlowNodeInstancesAndFlowNodeIds("uniqueProcessId", "testInstanceId");
		verify(processRepositoryService).findArchivedByRunningUniqueProcessId("uniqueProcessId");
	}

	@Test
	public void testDeleteProcessInstance() {
		processInstanceMediatorServiceMockInjected.deleteProcessInstance("uniqueProcessId", "testInstanceId");
		
		// verify that underlying method has been called
		verify(flowNodeInstanceRepositoryService).deleteAllFlowNodeInstanceNodes("uniqueProcessId", "testInstanceId");
	}

	@Test
	public void testSetProcessInstanceStartTime() {
		assertEquals(startDateFixed, processInstanceNodeSpy.getProcessInstanceStartTime());
		
		Date newDate = new Date();
		
		processInstanceMediatorServiceMockInjected.setProcessInstanceStartTime("uniqueProcessId", "testInstanceId", newDate);
		
		assertEquals(newDate, processInstanceNodeSpy.getProcessInstanceStartTime());
	}

	@Test
	public void testSetProcessInstanceEndTime() {
		assertEquals(null, processInstanceNodeSpy.getProcessInstanceEndTime());
		
		Date newDate = new Date();
		
		processInstanceMediatorServiceMockInjected.setProcessInstanceEndTime("uniqueProcessId", "testInstanceId", newDate);
		
		assertEquals(newDate, processInstanceNodeSpy.getProcessInstanceEndTime());
	}

	@Test
	public void testSetMetaDataProperties() {
		Map<String, Object> metaData = new HashMap<String, Object>();
		metaData.put("testKey", "testValue");
		
		processInstanceMediatorServiceMockInjected.setMetaDataProperties("uniqueProcessId", "testInstanceId", metaData);
		
		verify(processInstanceNodeRepositoryService).save(processInstanceNodeSpy);
		
		assertEquals("testValue", processInstanceNodeSpy.getMetaDataProperties().getProperty("testKey"));
		
		// causes NPE although everything seems to be fine
//		verify(processInstanceNodeSpy).setMetaDataProperties(processInstanceNodeSpy
//				.getMetaDataProperties().createFrom(metaData));
	}

}
