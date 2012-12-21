package com.catify.processengine.core.data.services.impl;

import java.util.ArrayList;

import com.catify.processengine.core.processdefinition.jaxb.TFlowNode;
import com.catify.processengine.core.processdefinition.jaxb.TProcess;
import com.catify.processengine.core.processdefinition.jaxb.TSubProcess;
import com.catify.processengine.core.processdefinition.jaxb.services.ExtensionService;

/**
 * The Class IdService generates unique ids for processes and flow nodes.
 * Because the akka node services and the data services need the same
 * understanding of these ids, this is managed in a central service.
 */
public final class IdService {
	
	private IdService() {
		
	}
	
	public final static String ARCHIVEPREFIX = "Archive:";
	
	/**
	 * Gets the unique process id from a jaxb process, which is built via
	 * concatenation of the process id, name and version.
	 * 
	 * @param processJaxb
	 *            the jaxb process
	 * @return the unique process id
	 */
	public static String getUniqueProcessId(String clientId, TProcess processJaxb) {
		return new String(clientId + processJaxb.getName() + processJaxb.getId()
				+ ExtensionService.getTVersion(processJaxb).getVersion());
	}
	
	/**
	 * Gets the unique flow node id, which is built via concatenation of the client id, the
	 * process id, name and version, the parent sub process ids and names and the flow node id and name.
	 *
	 * @param clientId the client id
	 * @param processJaxb the jaxb process
	 * @param subProcessesJaxb the jaxb sub processes
	 * @param flowNodeJaxb the jaxb flow node that the unique id is searched for
	 * @return the unique flow node id
	 */
	public static String getUniqueFlowNodeId(String clientId, TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb,
			TFlowNode flowNodeJaxb) {
		StringBuilder parentSubProcesses = new StringBuilder();
		
		for (TSubProcess tSubProcess : subProcessesJaxb) {
			parentSubProcesses.append(tSubProcess.getId() + tSubProcess.getName());
		}
		
		return new String(clientId 
				+ processJaxb.getId() + processJaxb.getName() + ExtensionService.getTVersion(processJaxb).getVersion() 
				+ parentSubProcesses
				+ flowNodeJaxb.getId() + flowNodeJaxb.getName());
	}
	
	/**
	 * Gets the unique flow node id, which is built via concatenation of the client id, the
	 * process id, name and version, the parent sub process ids and names and the flow node id and name.
	 *
	 * @param clientId the client id
	 * @param processJaxb the jaxb process
	 * @param subProcessesJaxb the jaxb sub processes
	 * @param flowNodeJaxb the jaxb flow node that the unique id is searched for
	 * @return the unique flow node id
	 */
	public static String getArchivedUniqueFlowNodeId(String clientId, TProcess processJaxb, ArrayList<TSubProcess> subProcessesJaxb,
			TFlowNode flowNodeJaxb) {
		StringBuilder parentSubProcesses = new StringBuilder();
		
		for (TSubProcess tSubProcess : subProcessesJaxb) {
			parentSubProcesses.append(tSubProcess.getId() + tSubProcess.getName());
		}
		
		return new String(ARCHIVEPREFIX + clientId 
				+ processJaxb.getId() + processJaxb.getName() + ExtensionService.getTVersion(processJaxb).getVersion() 
				+ parentSubProcesses
				+ flowNodeJaxb.getId() + flowNodeJaxb.getName());
	}

}
