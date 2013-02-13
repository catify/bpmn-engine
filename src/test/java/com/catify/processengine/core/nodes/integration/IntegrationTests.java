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
package com.catify.processengine.core.nodes.integration;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.constraints.AssertTrue;
import javax.xml.bind.JAXBException;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.helpers.collection.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.catify.processengine.core.data.model.NodeInstaceStates;
import com.catify.processengine.core.data.model.entities.FlowNodeInstance;
import com.catify.processengine.core.data.model.entities.ProcessNode;
import com.catify.processengine.core.data.services.FlowNodeInstanceRepositoryService;
import com.catify.processengine.core.data.services.IdService;
import com.catify.processengine.core.data.services.ProcessNodeRepositoryService;
import com.catify.processengine.core.messages.TriggerMessage;
import com.catify.processengine.core.processdefinition.jaxb.TFlowNode;
import com.catify.processengine.core.processdefinition.jaxb.TProcess;
import com.catify.processengine.core.processdefinition.jaxb.TSubProcess;
import com.catify.processengine.management.ProcessManagementService;
import com.catify.processengine.management.ProcessManagementServiceImpl;
import com.catify.processengine.management.XmlJaxbTransformer;

/**
 * @author chris
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:META-INF/spring/spring-context.xml" })
//@Transactional
public class IntegrationTests {
	
	static final Logger LOG = LoggerFactory
			.getLogger(IntegrationTests.class);
	
	@Autowired
    private ApplicationContext ctx;
	
	@Autowired 
	private Neo4jTemplate neo4jTemplate;
	
	@Autowired
	private FlowNodeInstanceRepositoryService flowNodeInstanceRepo;
	
	@Autowired
	private ProcessNodeRepositoryService processNodeRepo;

    private final String client = "Client";
    private final String startEvent = "startEvent1";
    private final String catchEvent = "catchEvent1";
    private final String defaultInstanceId = "42";
    
    private ProcessManagementService pm = new ProcessManagementServiceImpl();
    private XmlJaxbTransformer xmlJaxbTransformer = new XmlJaxbTransformer();
    
	@Test
	public void testprocessThrow() throws IOException, JAXBException, InterruptedException {
		TProcess process = simpleProcessTest("testprocess_throw.bpmn", 3000, 5000, 6, 3);
	    Assert.assertTrue(checkFlowNodeInstanceState(NodeInstaceStates.PASSED_STATE, process, defaultInstanceId));
	}
	
//	@Test
//	public void testprocessMessageIntegration() throws IOException, JAXBException, InterruptedException {
//		TProcess process = simpleProcessTest("testprocess_throw_camel_messageIntegration.bpmn", 3000, 5000, 6, 3);
//	    Assert.assertTrue(checkFlowNodeInstanceState(NodeInstaceStates.PASSED_STATE, process, defaultInstanceId));
//	}
	
	@Test
	public void testprocessThrowThrowComplex() throws IOException, JAXBException, InterruptedException {
		TProcess process = simpleProcessTest("testprocess_throw_throw_complex.bpmn", 3000, 5000, 10, 5);
	    Assert.assertTrue(checkFlowNodeInstanceState(NodeInstaceStates.PASSED_STATE, process, defaultInstanceId));
	}
	
	@Test
	public void testprocessThrowThrowAnd() throws IOException, JAXBException, InterruptedException {		
		TProcess process = simpleProcessTest("testprocess_throw_throw_and.bpmn", 3000, 5000, 10, 5);
	    Assert.assertTrue(checkFlowNodeInstanceState(NodeInstaceStates.PASSED_STATE, process, defaultInstanceId));
	}
	
	@Test
	public void testprocessExclusiveGateway() throws FileNotFoundException, JAXBException, InterruptedException {
		TProcess process = simpleProcessTest("testprocess_exclusive_gateway.bpmn", 3000, 5000, 20, 10);		
		assertEquals(4, countFlowNodeInstanceWithState(NodeInstaceStates.PASSED_STATE, process, defaultInstanceId));
		// check nodes
		checkNodeInstance(process, "_17", NodeInstaceStates.PASSED_STATE);
		checkNodeInstance(process, "_18", NodeInstaceStates.PASSED_STATE);
	}
	
	@Test
	public void testprocessExclusiveGatewayDefault() throws FileNotFoundException, JAXBException, InterruptedException {
		TProcess process = simpleProcessTest("testprocess_exclusive_gateway_default.bpmn", 3000, 5000, 20, 10);		
		assertEquals(4, countFlowNodeInstanceWithState(NodeInstaceStates.PASSED_STATE, process, defaultInstanceId));
		// check nodes
		checkNodeInstance(process, "_13", NodeInstaceStates.PASSED_STATE);
		checkNodeInstance(process, "_15", NodeInstaceStates.PASSED_STATE);
	}
	
	@Test
	public void testprocessSubprocess() throws IOException, JAXBException, InterruptedException {
		TProcess process = simpleProcessTest("testprocess_subprocess.bpmn", 3000, 5000, 6, 7);
	    Assert.assertTrue(checkFlowNodeInstanceState(NodeInstaceStates.PASSED_STATE, process, defaultInstanceId));
	}
	
	@Test
	public void testprocessSubprocessSubprocess() throws IOException, JAXBException, InterruptedException {
		TProcess process = simpleProcessTest("testprocess_subprocess_subprocess.bpmn", 3000, 5000, 6, 11);
	    Assert.assertTrue(checkFlowNodeInstanceState(NodeInstaceStates.PASSED_STATE, process, defaultInstanceId));
	}
	
	@Test
	public void testprocessSubprocessSubprocessTerminate() throws IOException, JAXBException, InterruptedException {
		TProcess process = simpleProcessTest("testprocess_subprocess_subprocess_terminate.bpmn", 3000, 5000, 6, 11);
		
		Thread.sleep(3000);
		
	    Assert.assertFalse(checkFlowNodeInstanceState(NodeInstaceStates.PASSED_STATE, process, defaultInstanceId));
	    
	    checkNodeInstance(process, "startEvent1", NodeInstaceStates.PASSED_STATE);
	    checkNodeInstance(process, "subProcess1", NodeInstaceStates.DEACTIVATED_STATE);
	    checkNodeInstance(process, "endEvent1", NodeInstaceStates.INACTIVE_STATE);
	    
	    // checking of sub process nodes still needs to be implemented yet
	    checkNodeInstance(process, "subSubProcess1", NodeInstaceStates.DEACTIVATED_STATE);
	}
	
	@Test
	public void testprocessSubprocessTerminate() throws IOException, JAXBException, InterruptedException {
		TProcess process = simpleProcessTest("testprocess_subprocess_terminate.bpmn", 3000, 5000, 6, 6);
		
		Thread.sleep(3000);
		
	    Assert.assertFalse(checkFlowNodeInstanceState(NodeInstaceStates.PASSED_STATE, process, defaultInstanceId));
	    
	    checkNodeInstance(process, "startEvent1", NodeInstaceStates.PASSED_STATE);
	    checkNodeInstance(process, "subProcess1", NodeInstaceStates.DEACTIVATED_STATE);
	    checkNodeInstance(process, "endEvent1", NodeInstaceStates.INACTIVE_STATE);
	    
	    // checking of sub process nodes still needs to be implemented yet
	    checkNodeInstance(process, "subThrowEvent1", NodeInstaceStates.PASSED_STATE);
	}

	@Test
	public void testprocessCatch() throws IOException, JAXBException, InterruptedException {
		TProcess process = simpleProcessTestWithTrigger("testprocess_catch.bpmn", catchEvent, 3000, 5000, 5000, 6, 3);		
	    Assert.assertTrue(checkFlowNodeInstanceState(NodeInstaceStates.PASSED_STATE, process, defaultInstanceId));
	}
	
	@Test
	public void testprocessCatchThrowComplex() throws IOException, JAXBException, InterruptedException {
		TProcess process = simpleProcessTest("testprocess_catch_throw_complex.bpmn", 3000, 5000, 10, 5);	
	    Assert.assertEquals(4, countFlowNodeInstanceWithState(NodeInstaceStates.PASSED_STATE, process, defaultInstanceId));
	}
	
	@Test
	public void testprocessCatchThrowAnd() throws IOException, JAXBException, InterruptedException {
		TProcess process = simpleProcessTestWithTrigger("testprocess_catch_throw_and.bpmn", catchEvent, 3000, 5000, 5000, 10, 5);		
	    Assert.assertTrue(checkFlowNodeInstanceState(NodeInstaceStates.PASSED_STATE, process, defaultInstanceId));
	}
	
	@Test
	public void testprocessEbgCatchCatch() throws IOException, JAXBException, InterruptedException {
		TProcess process = simpleProcessTestWithTrigger("testprocess_ebg_catch_catch.bpmn", catchEvent, 3000, 5000, 5000, 10, 5);		
	    Assert.assertEquals(4, countFlowNodeInstanceWithState(NodeInstaceStates.PASSED_STATE, process, defaultInstanceId));
	}
	
	@Test
	public void testprocessThrowThrowAndSignavio() throws IOException, JAXBException, InterruptedException {
		TProcess process = simpleProcessTest("testprocess_throw_throw_and_signavio.bpmn", 3000, 5000, 10, 5);
	    Assert.assertTrue(checkFlowNodeInstanceState(NodeInstaceStates.PASSED_STATE, process, defaultInstanceId));
	}
	
	@Test
	public void testprocessThrowMassData() throws IOException, JAXBException, InterruptedException {
		TProcess process = startProcess("testprocess_throw_massdata.bpmn", 3000);
		pm.createProcessInstance(client, process, startEvent, new TriggerMessage(defaultInstanceId, null));
		
		Thread.sleep(5000);
		
		for (int i = 0; i < 299; i++) {
			// trigger the waiting catch event
		    pm.sendTriggerMessage(client, process, startEvent, new TriggerMessage(null, null));
		}
		
		Thread.sleep(1000);
		int i = 0;
		while (countAllProcessInstances(process) != 300 && i < 5) {
			Thread.sleep(10000);
			i++;
		}
		
		Assert.assertEquals(300, countAllProcessInstances(process));
	    Assert.assertTrue(checkFlowNodeInstanceState(NodeInstaceStates.PASSED_STATE, process, defaultInstanceId));
	}
	
	@Test
	public void testprocessSignalEventCatchThrow() throws FileNotFoundException, JAXBException, InterruptedException {
		// deploy processes
		TProcess p1 = startProcess("testprocess_throw_signal_event.bpmn", 3000);
		TProcess p2 = startProcess("testprocess_catch_signal_event.bpmn", 3000);
		TProcess p3 = startProcess("testprocess_catch_signal_start_event.bpmn", 3000);
		TProcess p4 = startProcess("testprocess_catch_other_signal_event.bpmn", 3000);
		
		// start processes
		pm.createProcessInstance(client, p2, startEvent, new TriggerMessage("20", null)); // catch signal 'SIG_1' 
		pm.createProcessInstance(client, p2, startEvent, new TriggerMessage("21", null)); // catch signal 'SIG_1' 
		pm.createProcessInstance(client, p4, startEvent, new TriggerMessage("40", null)); // catch signal 'SIG_2' 
		pm.createProcessInstance(client, p4, startEvent, new TriggerMessage("41", null)); // catch signal 'SIG_2' 
		Thread.sleep(2000);
		pm.createProcessInstance(client, p1, startEvent, new TriggerMessage("10", null)); // throw signal 'SIG_1'
		Thread.sleep(1500);
		
		//  check results 
		this.checkProcess(p1, 6, 3, "10");
		this.checkProcess(p2, 6, 3, "20");
		this.checkProcess(p2, 6, 3, "21");
		this.checkProcess(p4, 6, 3, "40");
		this.checkProcess(p4, 6, 3, "41");
		assertTrue(this.checkFlowNodeInstanceState(NodeInstaceStates.PASSED_STATE, p1, "10")); // send 'SIG_1'
		assertTrue(this.checkFlowNodeInstanceState(NodeInstaceStates.PASSED_STATE, p2, "20")); // received 'SIG_1'
		assertTrue(this.checkFlowNodeInstanceState(NodeInstaceStates.PASSED_STATE, p2, "21")); // received 'SIG_1'
		assertFalse(this.checkFlowNodeInstanceState(NodeInstaceStates.PASSED_STATE, p4, "40")); // received 'SIG_1' but not 'SIG_2'
		assertFalse(this.checkFlowNodeInstanceState(NodeInstaceStates.PASSED_STATE, p4, "41")); // received 'SIG_1' but not 'SIG_2'
		assertEquals(1, this.countAllProcessInstances(p3)); // started instance on 'SIG_1'
	}
	
	/**
	 * Helper method to test standard scenarios.
	 *
	 * @param fileName name of the file without path and slash (must be in /src/test/resources/data/)
	 * @param firstSleep milliseconds of the first sleep
	 * @param secondSleep milliseconds of the first sleep
	 * @param awaitedFlowNodeCount awaited number of flow nodes (on top level)
	 * @param awaitedInstanceNodeCount awaited number of flow node instances
	 * @return the jaxb process
	 * @throws FileNotFoundException the file not found exception
	 * @throws JAXBException the jAXB exception
	 * @throws InterruptedException the interrupted exception
	 */
	private TProcess simpleProcessTest(String fileName, int firstSleep, int secondSleep, int awaitedFlowNodeCount, int awaitedInstanceNodeCount) 
			throws FileNotFoundException, JAXBException, InterruptedException {
		TProcess process = startProcess(fileName, firstSleep);
		pm.createProcessInstance(client, process, startEvent, new TriggerMessage(defaultInstanceId, null));

		// wait for the process instance to start up
		Thread.sleep(secondSleep);

		// check results
		this.checkProcess(process, awaitedFlowNodeCount, awaitedInstanceNodeCount);
		
		return process;
	}
	
	/**
	 * Checks if a process execution was successful.
	 * 
	 * @param process
	 * @param awaitedFlowNodeCount
	 * @param awaitedInstanceNodeCount
	 */
	private void checkProcess(TProcess process, int awaitedFlowNodeCount, int awaitedInstanceNodeCount) {
		this.checkProcess(process, awaitedFlowNodeCount, awaitedInstanceNodeCount, defaultInstanceId);
	}
	
	/**
	 * Checks if a process execution was successful.
	 * 
	 * @param process
	 * @param awaitedFlowNodeCount
	 * @param awaitedInstanceNodeCount
	 * @param instanceId
	 */
	private void checkProcess(TProcess process, int awaitedFlowNodeCount, int awaitedInstanceNodeCount, String instanceId) {
		Assert.assertEquals(awaitedFlowNodeCount, getFlowNodeCount(process));
		Assert.assertEquals(awaitedInstanceNodeCount, getFlowNodeInstanceCount(process, instanceId));
	}
	
	/**
	 * Helper method to test standard scenarios with a node to trigger.
	 *
	 * @param fileName name of the file without path and slash (must be in /src/test/resources/data/)
	 * @param flowNodeIdToTrigger the flow node id to trigger
	 * @param firstSleep milliseconds of the first sleep
	 * @param secondSleep milliseconds of the first sleep
	 * @param thirdSleep milliseconds of the third sleep
	 * @param awaitedFlowNodeCount awaited number of flow nodes
	 * @param awaitedInstanceNodeCount awaited number of flow node instances
	 * @return the jaxb process
	 * @throws FileNotFoundException the file not found exception
	 * @throws JAXBException the jAXB exception
	 * @throws InterruptedException the interrupted exception
	 */
	private TProcess simpleProcessTestWithTrigger(String fileName, String flowNodeIdToTrigger, int firstSleep, int secondSleep, int thirdSleep, int awaitedFlowNodeCount, int awaitedInstanceNodeCount) 
			throws FileNotFoundException, JAXBException, InterruptedException {
		TProcess process = startProcess(fileName, firstSleep);
		pm.createProcessInstance(client, process, startEvent, new TriggerMessage(defaultInstanceId, null));

		// wait for the process instance to start up
		Thread.sleep(secondSleep);
		
	    // trigger the waiting catch event
	    pm.sendTriggerMessage(client, process, flowNodeIdToTrigger, new TriggerMessage(defaultInstanceId, null));
	    
	    // wait for the process instance to end
	    Thread.sleep(thirdSleep);

		// check results
		this.checkProcess(process, awaitedFlowNodeCount, awaitedInstanceNodeCount);
		
		return process;
	}
	
	/**
	 * Start a process.
	 *
	 * @param fileName name of the file without path and slash (must be in /src/test/resources/data/)
	 * @param firstSleep milliseconds of the first sleep
	 * @return the jaxb process
	 * @throws FileNotFoundException the file not found exception
	 * @throws JAXBException the jAXB exception
	 * @throws InterruptedException the interrupted exception
	 */
	private TProcess startProcess(String fileName, int firstSleep)
			throws FileNotFoundException, JAXBException, InterruptedException {
		File processDefinition = new File(getClass().getResource("/data/" + fileName).getFile());
		Assert.assertTrue(processDefinition.exists());

		pm.startProcessFromDefinitionFile(client, processDefinition);

		// wait for the process to start up
		Thread.sleep(firstSleep);

		List<TProcess> processes = xmlJaxbTransformer.getTProcessesFromBpmnXml(processDefinition);
		assertNotNull(processes);
		return processes.get(0);
	}
	
	/**
	 * Gets the flow node count. Note that a process always has running and archived flow nodes.
	 *
	 * @return the flow node count
	 */
	long getFlowNodeCount(TProcess process) {
		String processId = IdService.getUniqueProcessId(client, process);
		ProcessNode processNode = processNodeRepo.findByUniqueProcessId(processId);
		ProcessNode archivedProcessNode = processNodeRepo.findArchivedByRunningUniqueProcessId(processId);
		
		return processNode.getFlowNodes().size() + archivedProcessNode.getFlowNodes().size();
	}
	
	/**
	 * Gets the flow node instance count.
	 *
	 * @return the flow node instance count
	 */
	long getFlowNodeInstanceCount(TProcess process) {		
		return this.getFlowNodeInstanceCount(process, defaultInstanceId);
	}
	
	/**
	 * Gets the flow node instance count.
	 * 
	 * @param process {@link TProcess} process object
	 * @param instanceId instance id as {@link String}
	 * @return
	 */
	long getFlowNodeInstanceCount(TProcess process, String instanceId) {
		String processId = IdService.getUniqueProcessId(client, process);
		Set<FlowNodeInstance> fni = flowNodeInstanceRepo.findAllFlowNodeInstances(processId, instanceId);
		
		return Iterables.count(fni);
	}

	/**
	 * Check flow node instance state.
	 *
	 * @param state the state
	 * @return true, if all FlowNodeInstances have the given state
	 */
	boolean checkFlowNodeInstanceState(String state, TProcess process, String processInstanceId) {
		
		String processId = IdService.getUniqueProcessId(client, process);
		Set<FlowNodeInstance> fni = flowNodeInstanceRepo.findAllFlowNodeInstances(processId, processInstanceId);
		
		if (fni.size()==0) {
			return false;
		}
		
		for (FlowNodeInstance flowNodeInstance : fni) {
			if (!flowNodeInstance.getNodeInstanceState().equals(state)) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Count the flow node instances in a given state.
	 *
	 * @param state the state
	 * @return the int
	 */
	int countFlowNodeInstanceWithState(String state, TProcess process, String processInstanceId) {
		String processId = IdService.getUniqueProcessId(client, process);
		Set<FlowNodeInstance> fni = flowNodeInstanceRepo.findAllFlowNodeInstances(processId, processInstanceId);
		
		int counter = 0;
		
		for (FlowNodeInstance flowNodeInstance : fni) {
			if (flowNodeInstance.getNodeInstanceState().equals(state)) {
				counter++;
			}
		}
		
		return counter;
	}
	
	/**
	 * Count the flow node instances of a process.
	 *
	 * @param state the state
	 * @return the int
	 */
	int countAllProcessInstances(TProcess process) {
		String processId = IdService.getUniqueProcessId(client, process);
		Set<String> fni = flowNodeInstanceRepo.findAllFlowNodeInstances(processId);

		return fni.size();
	}
	

	/**
	 * Check node instance of a top level flow node.
	 *
	 * @param process the jaxb process
	 * @param id the id of the flow node to check
	 * @param state the desired state
	 */
	private void checkNodeInstance(TProcess process, String id, String state) {
		ArrayList<TSubProcess> subProcessJaxb = IdService.getTSubprocessesById(process, id);
		String flowNodeId = IdService.getUniqueFlowNodeId(client, process, subProcessJaxb, id); // default throw
		String processId = IdService.getUniqueProcessId(client, process);
		FlowNodeInstance nodeInstance = flowNodeInstanceRepo.findFlowNodeInstance(processId, flowNodeId, defaultInstanceId);
		assertNotNull(nodeInstance);
		assertEquals(state, nodeInstance.getNodeInstanceState());
	}

}
