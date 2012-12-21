/**
 * 
 */
package com.catify.processengine.core.data.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.catify.processengine.core.data.model.entities.FlowNode;
import com.catify.processengine.core.data.model.entities.FlowNodeInstance;
import com.catify.processengine.core.data.model.entities.ProcessNode;

/**
 * @author chris
 * 
 */
public class ModelTestBase {

	public static String CLIENT_ID = "clientId";
	
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

	public FlowNodeInstance createFlowNodeInstance() {

		FlowNodeInstance flowNodeInstance = new FlowNodeInstance("TESTING");

		assertNotNull(flowNodeInstance);

		return flowNodeInstance;
	}

}
