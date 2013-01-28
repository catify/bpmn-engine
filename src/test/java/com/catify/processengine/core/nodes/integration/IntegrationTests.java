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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.helpers.collection.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.neo4j.conversion.EndResult;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.catify.processengine.core.data.model.NodeInstaceStates;
import com.catify.processengine.core.data.model.entities.FlowNode;
import com.catify.processengine.core.data.model.entities.FlowNodeInstance;
import com.catify.processengine.core.data.services.FlowNodeInstanceRepositoryService;
import com.catify.processengine.core.data.services.impl.IdService;
import com.catify.processengine.core.messages.TriggerMessage;
import com.catify.processengine.core.processdefinition.jaxb.TProcess;
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
	private FlowNodeInstanceRepositoryService flowNodeRepo;

    private final String client = "Client";
    private final String startEvent = "startEvent1";
    private final String catchEvent = "catchEvent1";
    private final String defaultInstanceId = "42";
    
    private ProcessManagementService pm = new ProcessManagementServiceImpl();
    private XmlJaxbTransformer xmlJaxbTransformer = new XmlJaxbTransformer();

    @SuppressWarnings("rawtypes") // we just need to clear all values
    @Before
    public void prepareTestDatabase()
    {
    	Map<String, GraphRepository> graphRepositories = ctx.getBeansOfType(GraphRepository.class);
      LOG.debug("Clearing database!");
      for (GraphRepository graphRepository : graphRepositories.values()) {
          graphRepository.deleteAll();
      }
      try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }

    
	@Test
	public void testprocessThrow() throws IOException, JAXBException, InterruptedException {
		simpleProcessTest("testprocess_throw.bpmn", 3000, 5000, 6, 3);
	    Assert.assertTrue(checkFlowNodeInstanceState(NodeInstaceStates.PASSED_STATE));
	}
	
	@Test
	public void testprocessThrowThrowComplex() throws IOException, JAXBException, InterruptedException {
		simpleProcessTest("testprocess_throw_throw_complex.bpmn", 3000, 5000, 10, 5);
	    Assert.assertTrue(checkFlowNodeInstanceState(NodeInstaceStates.PASSED_STATE));
	}
	
	@Test
	public void testprocessThrowThrowAnd() throws IOException, JAXBException, InterruptedException {		
	    simpleProcessTest("testprocess_throw_throw_and.bpmn", 3000, 5000, 10, 5);
	    Assert.assertTrue(checkFlowNodeInstanceState(NodeInstaceStates.PASSED_STATE));
	}
	
	@Test
	public void testprocessExclusiveGateway() throws FileNotFoundException, JAXBException, InterruptedException {
		TProcess process = simpleProcessTest("testprocess_exclusive_gateway.bpmn", 3000, 5000, 20, 10);		
		assertEquals(4, countFlowNodeInstanceWithState(NodeInstaceStates.PASSED_STATE));
		// check nodes
		checkNodeInstance(process, "_17", NodeInstaceStates.PASSED_STATE);
		checkNodeInstance(process, "_18", NodeInstaceStates.PASSED_STATE);
	}
	
	@Test
	public void testprocessExclusiveGatewayDefault() throws FileNotFoundException, JAXBException, InterruptedException {
		TProcess process = simpleProcessTest("testprocess_exclusive_gateway_default.bpmn", 3000, 5000, 20, 10);		
		assertEquals(4, countFlowNodeInstanceWithState(NodeInstaceStates.PASSED_STATE));
		// check nodes
		checkNodeInstance(process, "_13", NodeInstaceStates.PASSED_STATE);
		checkNodeInstance(process, "_15", NodeInstaceStates.PASSED_STATE);
	}
	
	@Test
	public void testprocessSubprocess() throws IOException, JAXBException, InterruptedException {
		simpleProcessTest("testprocess_subprocess.bpmn", 3000, 5000, 14, 7);
	    Assert.assertTrue(checkFlowNodeInstanceState(NodeInstaceStates.PASSED_STATE));
	}
	
	@Test
	public void testprocessSubprocessSubprocess() throws IOException, JAXBException, InterruptedException {
		simpleProcessTest("testprocess_subprocess_subprocess.bpmn", 3000, 5000, 22, 11);
	    Assert.assertTrue(checkFlowNodeInstanceState(NodeInstaceStates.PASSED_STATE));
	}
	
	@Test
	public void testprocessSubprocessSubprocessTerminate() throws IOException, JAXBException, InterruptedException {
		simpleProcessTest("testprocess_subprocess_subprocess_terminate.bpmn", 3000, 5000, 22, 11);
	    Assert.assertTrue(checkFlowNodeInstanceState(NodeInstaceStates.PASSED_STATE));
	}
	
	@Test
	public void testprocessCatch() throws IOException, JAXBException, InterruptedException {
		simpleProcessTestWithTrigger("testprocess_catch.bpmn", catchEvent, 3000, 5000, 5000, 6, 3);		
	    Assert.assertTrue(checkFlowNodeInstanceState(NodeInstaceStates.PASSED_STATE));
	}
	
	@Test
	public void testprocessCatchThrowComplex() throws IOException, JAXBException, InterruptedException {
		simpleProcessTest("testprocess_catch_throw_complex.bpmn", 3000, 5000, 10, 5);	
	    Assert.assertEquals(4, countFlowNodeInstanceWithState(NodeInstaceStates.PASSED_STATE));
	}
	
	@Test
	public void testprocessCatchThrowAnd() throws IOException, JAXBException, InterruptedException {
		simpleProcessTestWithTrigger("testprocess_catch_throw_and.bpmn", catchEvent, 3000, 5000, 5000, 10, 5);		
	    Assert.assertTrue(checkFlowNodeInstanceState(NodeInstaceStates.PASSED_STATE));
	}
	
	@Test
	public void testprocessEbgCatchCatch() throws IOException, JAXBException, InterruptedException {
		simpleProcessTestWithTrigger("testprocess_ebg_catch_catch.bpmn", catchEvent, 3000, 5000, 5000, 10, 5);		
	    Assert.assertEquals(4, countFlowNodeInstanceWithState(NodeInstaceStates.PASSED_STATE));
	}
	
	@Test
	public void testprocessThrowThrowAndSignavio() throws IOException, JAXBException, InterruptedException {
	    simpleProcessTest("testprocess_throw_throw_and_signavio.bpmn", 3000, 5000, 10, 5);
	    Assert.assertTrue(checkFlowNodeInstanceState(NodeInstaceStates.PASSED_STATE));
	}
	
	/**
	 * Helper method to test standard scenarios.
	 *
	 * @param fileName name of the file without path and slash (must be in /src/test/resources/data/)
	 * @param firstSleep milliseconds of the first sleep
	 * @param secondSleep milliseconds of the first sleep
	 * @param awaitedFlowNodeCount awaited number of flow nodes
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
		Assert.assertEquals(awaitedFlowNodeCount, getFlowNodeCount());
		Assert.assertEquals(awaitedInstanceNodeCount, getFlowNodeInstanceCount());
		
		return process;
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
		Assert.assertEquals(awaitedFlowNodeCount, getFlowNodeCount());
		Assert.assertEquals(awaitedInstanceNodeCount, getFlowNodeInstanceCount());
		
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
	long getFlowNodeCount() {
		EndResult<FlowNode> fn = neo4jTemplate.findAll(FlowNode.class);
		return Iterables.count(fn);
	}
	
	/**
	 * Gets the flow node instance count.
	 *
	 * @return the flow node instance count
	 */
	long getFlowNodeInstanceCount() {
		EndResult<FlowNodeInstance> fni = neo4jTemplate.findAll(FlowNodeInstance.class);
		return Iterables.count(fni);
	}

	/**
	 * Check flow node instance state.
	 *
	 * @param state the state
	 * @return true, if all FlowNodeInstances have the given state
	 */
	boolean checkFlowNodeInstanceState(String state) {
		EndResult<FlowNodeInstance> fni = neo4jTemplate.findAll(FlowNodeInstance.class);
		
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
	int countFlowNodeInstanceWithState(String state) {
		EndResult<FlowNodeInstance> fni = neo4jTemplate.findAll(FlowNodeInstance.class);
		int counter = 0;
		
		for (FlowNodeInstance flowNodeInstance : fni) {
			if (flowNodeInstance.getNodeInstanceState().equals(state)) {
				counter++;
			}
		}
		
		return counter;
	}
	

	/**
	 * Check node instance.
	 *
	 * @param process the jaxb process
	 * @param id the id of the flow node to check
	 * @param state the desired state
	 */
	private void checkNodeInstance(TProcess process, String id, String state) {
		String flowNodeId = IdService.getArchivedUniqueFlowNodeId(client, process, null, id); // default throw
		String processId = IdService.getArchivedUniqueProcessId(client, process);
		FlowNodeInstance nodeInstance = flowNodeRepo.findFlowNodeInstance(processId, flowNodeId, defaultInstanceId);
		assertNotNull(nodeInstance);
		assertEquals(state, nodeInstance.getNodeInstanceState());
	}
}
