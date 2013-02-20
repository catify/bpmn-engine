package com.catify.processengine.core.nodes.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBException;

import junit.framework.Assert;

import org.neo4j.helpers.collection.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.neo4j.support.Neo4jTemplate;

import com.catify.processengine.core.data.model.entities.FlowNodeInstance;
import com.catify.processengine.core.data.model.entities.ProcessNode;
import com.catify.processengine.core.data.services.FlowNodeInstanceRepositoryService;
import com.catify.processengine.core.data.services.IdService;
import com.catify.processengine.core.data.services.ProcessNodeRepositoryService;
import com.catify.processengine.core.messages.TriggerMessage;
import com.catify.processengine.core.processdefinition.jaxb.TProcess;
import com.catify.processengine.core.processdefinition.jaxb.TSubProcess;
import com.catify.processengine.management.ProcessManagementService;
import com.catify.processengine.management.ProcessManagementServiceImpl;
import com.catify.processengine.management.XmlJaxbTransformer;

public class IntegrationTestBase {

	static final Logger LOG = LoggerFactory
				.getLogger(IntegrationTestBase.class);
	@Autowired
	private ApplicationContext ctx;
	
	@Autowired
	private Neo4jTemplate neo4jTemplate;
	
	@Autowired
	private FlowNodeInstanceRepositoryService flowNodeInstanceRepo;
	
	@Autowired
	private ProcessNodeRepositoryService processNodeRepo;
	
	protected final String client = "Client";
	protected final String startEvent = "startEvent1";
	protected final String catchEvent = "catchEvent1";
	protected final String defaultInstanceId = "42";
	
	protected ProcessManagementService pm = new ProcessManagementServiceImpl();
	private XmlJaxbTransformer xmlJaxbTransformer = new XmlJaxbTransformer();

	public IntegrationTestBase() {
		super();
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
	protected TProcess simpleProcessTest(String fileName, int firstSleep, int secondSleep,
			int awaitedFlowNodeCount, int awaitedInstanceNodeCount) throws FileNotFoundException,
			JAXBException, InterruptedException {
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
	protected void checkProcess(TProcess process, int awaitedFlowNodeCount, int awaitedInstanceNodeCount,
			String instanceId) {
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
	protected TProcess simpleProcessTestWithTrigger(String fileName, String flowNodeIdToTrigger,
			int firstSleep, int secondSleep, int thirdSleep, int awaitedFlowNodeCount, int awaitedInstanceNodeCount)
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
	protected TProcess startProcess(String fileName, int firstSleep)
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
	protected long getFlowNodeCount(TProcess process) {
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
	protected long getFlowNodeInstanceCount(TProcess process) {		
		return this.getFlowNodeInstanceCount(process, defaultInstanceId);
	}

	/**
	 * Gets the flow node instance count.
	 * 
	 * @param process {@link TProcess} process object
	 * @param instanceId instance id as {@link String}
	 * @return
	 */
	protected long getFlowNodeInstanceCount(TProcess process, String instanceId) {
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
	protected boolean checkFlowNodeInstanceState(String state, TProcess process,
			String processInstanceId) {
				
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
	protected int countFlowNodeInstanceWithState(String state, TProcess process,
			String processInstanceId) {
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
	protected int countAllProcessInstances(TProcess process) {
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
	protected void checkNodeInstance(TProcess process, String id, String state) {
		List<TSubProcess> subProcessJaxb = IdService.getTSubprocessesById(process, id);
		String flowNodeId = IdService.getUniqueFlowNodeId(client, process, subProcessJaxb, id); // default throw
		String processId = IdService.getUniqueProcessId(client, process);
		FlowNodeInstance nodeInstance = flowNodeInstanceRepo.findFlowNodeInstance(processId, flowNodeId, defaultInstanceId);
		assertNotNull(nodeInstance);
		assertEquals(state, nodeInstance.getNodeInstanceState());
	}

}