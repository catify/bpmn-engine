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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.catify.processengine.core.data.model.NodeInstaceStates;
import com.catify.processengine.core.integration.MessageIntegrationSPI;
import com.catify.processengine.core.messageintegration.MessageIntegrationSPIMock;
import com.catify.processengine.core.messages.TriggerMessage;
import com.catify.processengine.core.processdefinition.jaxb.TProcess;

/**
 * @author chris
 * @author claus straube
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:META-INF/spring/spring-context.xml" })
//@Transactional
public class IntegrationTests extends IntegrationTestBase {
	
	
	@Test
	public void testprocessThrow() throws IOException, JAXBException, InterruptedException {
		TProcess process = simpleProcessTest("testprocess_throw.bpmn", 3000, 5000, 6, 3);
	    Assert.assertTrue(checkFlowNodeInstanceState(NodeInstaceStates.PASSED_STATE, process, defaultInstanceId));
	}
	
	@Test
	public void testprocessMessageIntegration() throws IOException, JAXBException, InterruptedException {		
		TProcess process = simpleProcessTest("testprocess_throw_camel_messageIntegration.bpmn", 3000, 5000, 6, 3);
	    Assert.assertTrue(checkFlowNodeInstanceState(NodeInstaceStates.PASSED_STATE, process, defaultInstanceId));
	    MessageIntegrationSPIMock mock = (MessageIntegrationSPIMock) MessageIntegrationSPI.getMessageIntegrationImpl(MessageIntegrationSPIMock.MOCK_PREFIX);
	    assertEquals(1, mock.sends.size());
	}
	
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

}
