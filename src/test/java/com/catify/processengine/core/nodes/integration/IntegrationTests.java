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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import junit.framework.Assert;

import static org.junit.Assert.*;
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

    private final String client = "Client";
    private final String startEvent = "startEvent1";
    
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
	public void testprocess_throw() throws IOException, JAXBException, InterruptedException {
		simpleProcessTest("testprocess_throw.xml", 3000, 5000, 6, 3);
	    Assert.assertTrue(checkFlowNodeInstanceState(NodeInstaceStates.PASSED_STATE));
	}
	
	@Test
	public void testprocess_throw_throw_complex() throws IOException, JAXBException, InterruptedException {
		simpleProcessTest("testprocess_throw_throw_complex.xml", 3000, 5000, 10, 5);
	    Assert.assertTrue(checkFlowNodeInstanceState(NodeInstaceStates.PASSED_STATE));
	}
	
	@Test
	public void testprocess_throw_throw_and() throws IOException, JAXBException, InterruptedException {		
	    simpleProcessTest("testprocess_throw_throw_and.xml", 3000, 5000, 10, 5);
	    Assert.assertTrue(checkFlowNodeInstanceState(NodeInstaceStates.PASSED_STATE));
	}
	
	@Test
	public void testprocessExclusiveGateway() throws FileNotFoundException, JAXBException, InterruptedException {
		simpleProcessTest("testprocess_exclusive_gateway.bpmn", 3000, 5000, 20, 10);	
		assertEquals(4, countFlowNodeInstanceWithState(NodeInstaceStates.PASSED_STATE));
	}
	
	@Test
	public void testprocess_subprocess() throws IOException, JAXBException, InterruptedException {
		
	    File processDefinition = new File(getClass().getResource("/data/testprocess_subprocess.xml").getFile());
	    Assert.assertTrue(processDefinition.exists());
		
		pm.startProcessFromDefinitionFile(client, processDefinition);

		// wait for the process to start up
		Thread.sleep(3000);

    	List<TProcess> processes = xmlJaxbTransformer.getTProcessesFromBpmnXml(processDefinition);
	    pm.createProcessInstance(client, processes.get(0), startEvent, new TriggerMessage());
	    
	    // wait for the process instance to start up
	    Thread.sleep(5000);
	    
	    // check results
	    Assert.assertEquals(14, getFlowNodeCount());
	    Assert.assertEquals(7, getFlowNodeInstanceCount());
	    Assert.assertTrue(checkFlowNodeInstanceState(NodeInstaceStates.PASSED_STATE));
	}
	
	@Test
	public void testprocess_subprocess_subprocess() throws IOException, JAXBException, InterruptedException {
		
	    File processDefinition = new File(getClass().getResource("/data/testprocess_subprocess_subprocess.xml").getFile());
	    Assert.assertTrue(processDefinition.exists());
		
		pm.startProcessFromDefinitionFile(client, processDefinition);

		// wait for the process to start up
		Thread.sleep(3000);

    	List<TProcess> processes = xmlJaxbTransformer.getTProcessesFromBpmnXml(processDefinition);
	    pm.createProcessInstance(client, processes.get(0), startEvent, new TriggerMessage());
	    
	    // wait for the process instance to start up
	    Thread.sleep(5000);
	    
	    // check results
	    Assert.assertEquals(22, getFlowNodeCount());
	    Assert.assertEquals(11, getFlowNodeInstanceCount());
	    Assert.assertTrue(checkFlowNodeInstanceState(NodeInstaceStates.PASSED_STATE));
	}
	
	@Test
	public void testprocess_catch() throws IOException, JAXBException, InterruptedException {
		
	    File processDefinition = new File(getClass().getResource("/data/testprocess_catch.xml").getFile());
	    Assert.assertTrue(processDefinition.exists());
		
		pm.startProcessFromDefinitionFile(client, processDefinition);

		// wait for the process to start up
		Thread.sleep(3000);

    	List<TProcess> processes = xmlJaxbTransformer.getTProcessesFromBpmnXml(processDefinition);
    	String processInstanceId = UUID.randomUUID().toString();
    	
	    pm.createProcessInstance(client, processes.get(0), startEvent, new TriggerMessage(processInstanceId, null));
	    
	    // wait for the process instance to start up
	    Thread.sleep(5000);
	    
	    // trigger the waiting catch event
	    pm.sendTriggerMessage(client, processes.get(0), "catchEvent1", new TriggerMessage(processInstanceId, null));
	    
	    // wait for the process instance to end
	    Thread.sleep(3000);
	    
	    // check results
	    Assert.assertEquals(6, getFlowNodeCount());
	    Assert.assertEquals(3, getFlowNodeInstanceCount());
	    Assert.assertTrue(checkFlowNodeInstanceState(NodeInstaceStates.PASSED_STATE));
	}
	
	@Test
	public void testprocess_catch_throw_complex() throws IOException, JAXBException, InterruptedException {
		
	    File processDefinition = new File(getClass().getResource("/data/testprocess_catch_throw_complex.xml").getFile());
	    Assert.assertTrue(processDefinition.exists());
		
		pm.startProcessFromDefinitionFile(client, processDefinition);

		// wait for the process to start up
		Thread.sleep(3000);

    	List<TProcess> processes = xmlJaxbTransformer.getTProcessesFromBpmnXml(processDefinition);
    	String processInstanceId = UUID.randomUUID().toString();
    	
	    pm.createProcessInstance(client, processes.get(0), startEvent, new TriggerMessage(processInstanceId, null));
	    
	    // wait for the process instance to start up
	    Thread.sleep(5000);
	    
	    // check results
	    Assert.assertEquals(10, getFlowNodeCount());
	    Assert.assertEquals(5, getFlowNodeInstanceCount());
	    Assert.assertEquals(4, countFlowNodeInstanceWithState(NodeInstaceStates.PASSED_STATE));
	}
	
	@Test
	public void testprocess_catch_throw_and() throws IOException, JAXBException, InterruptedException {
		
	    File processDefinition = new File(getClass().getResource("/data/testprocess_catch_throw_and.xml").getFile());
	    Assert.assertTrue(processDefinition.exists());
		
		pm.startProcessFromDefinitionFile(client, processDefinition);

		// wait for the process to start up
		Thread.sleep(3000);

    	List<TProcess> processes = xmlJaxbTransformer.getTProcessesFromBpmnXml(processDefinition);
    	String processInstanceId = UUID.randomUUID().toString();
    	
	    pm.createProcessInstance(client, processes.get(0), startEvent, new TriggerMessage(processInstanceId, null));
	    
	    // wait for the process instance to start up
	    Thread.sleep(5000);
	    
	    // trigger the waiting catch event
	    pm.sendTriggerMessage(client, processes.get(0), "catchEvent1", new TriggerMessage(processInstanceId, null));
	    
	    // wait for the process instance to end
	    Thread.sleep(3000);
	    
	    // check results
	    Assert.assertEquals(10, getFlowNodeCount());
	    Assert.assertEquals(5, getFlowNodeInstanceCount());
	    Assert.assertTrue(checkFlowNodeInstanceState(NodeInstaceStates.PASSED_STATE));
	}
	
	@Test
	public void testprocess_ebg_catch_catch() throws IOException, JAXBException, InterruptedException {
		
	    File processDefinition = new File(getClass().getResource("/data/testprocess_ebg_catch_catch.xml").getFile());
	    Assert.assertTrue(processDefinition.exists());
		
		pm.startProcessFromDefinitionFile(client, processDefinition);

		// wait for the process to start up
		Thread.sleep(3000);

    	List<TProcess> processes = xmlJaxbTransformer.getTProcessesFromBpmnXml(processDefinition);
    	String processInstanceId = UUID.randomUUID().toString();
    	
	    pm.createProcessInstance(client, processes.get(0), startEvent, new TriggerMessage(processInstanceId, null));
	    
	    // wait for the process instance to start up
	    Thread.sleep(5000);
	    
	    // trigger the waiting catch event
	    pm.sendTriggerMessage(client, processes.get(0), "catchEvent1", new TriggerMessage(processInstanceId, null));
	    
	    // wait for the process instance to end
	    Thread.sleep(5000);
	    
	    // check results
	    Assert.assertEquals(10, getFlowNodeCount());
	    Assert.assertEquals(5, getFlowNodeInstanceCount());
	    Assert.assertEquals(4, countFlowNodeInstanceWithState(NodeInstaceStates.PASSED_STATE));
	}
	
	@Test
	public void testprocess_throw_throw_and_signavio() throws IOException, JAXBException, InterruptedException {
		
	    File processDefinition = new File(getClass().getResource("/data/testprocess_throw_throw_and_signavio.xml").getFile());
	    Assert.assertTrue(processDefinition.exists());
		
		pm.startProcessFromDefinitionFile(client, processDefinition);

		// wait for the process to start up
		Thread.sleep(3000);

    	List<TProcess> processes = xmlJaxbTransformer.getTProcessesFromBpmnXml(processDefinition);
	    pm.createProcessInstance(client, processes.get(0), "sid-0D6FCC2F-C081-4335-A4AF-F883147EFD21", new TriggerMessage());
	    
	    // wait for the process instance to start up
	    Thread.sleep(5000);
	    
	    // check results
	    Assert.assertEquals(10, getFlowNodeCount());
	    Assert.assertEquals(5, getFlowNodeInstanceCount());
	    Assert.assertTrue(checkFlowNodeInstanceState(NodeInstaceStates.PASSED_STATE));
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
	 * Helper to test standard scenarios
	 * 
	 * @param fileName name of the file without path and slash (must be in /src/test/resources/data/)
	 * @param firstSleep milliseconds of the first sleep
	 * @param secondSleep milliseconds of the first sleep
	 * @param awaitedFlowNodeCount awaited number of flow nodes 
	 * @param awaitedInstanceNodeCount awaited number of flow node instances
	 * @throws FileNotFoundException
	 * @throws JAXBException
	 * @throws InterruptedException
	 */
	private void simpleProcessTest(String fileName, int firstSleep, int secondSleep, int awaitedFlowNodeCount, int awaitedInstanceNodeCount) 
			throws FileNotFoundException, JAXBException, InterruptedException {
		File processDefinition = new File(getClass().getResource("/data/" + fileName).getFile());
		Assert.assertTrue(processDefinition.exists());

		pm.startProcessFromDefinitionFile(client, processDefinition);

		// wait for the process to start up
		Thread.sleep(firstSleep);

		List<TProcess> processes = xmlJaxbTransformer.getTProcessesFromBpmnXml(processDefinition);
		assertNotNull(processes);
		pm.createProcessInstance(client, processes.get(0), startEvent, new TriggerMessage());

		// wait for the process instance to start up
		Thread.sleep(secondSleep);

		// check results
		Assert.assertEquals(awaitedFlowNodeCount, getFlowNodeCount());
		Assert.assertEquals(awaitedInstanceNodeCount, getFlowNodeInstanceCount());
	}
	
}
