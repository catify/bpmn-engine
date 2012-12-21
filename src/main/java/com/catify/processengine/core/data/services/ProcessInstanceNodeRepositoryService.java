/**
 * 
 */
package com.catify.processengine.core.data.services;

import org.springframework.stereotype.Component;

import com.catify.processengine.core.data.model.entities.ProcessInstanceNode;

/**
 * The ProcessInstanceNodeRepositoryService wraps methods of the ProcessInstanceNodeRepository and
 * provides additional methods that hide implementation details.
 *
 * @author chris
 */
@Component
public interface ProcessInstanceNodeRepositoryService {

	/**
	 * Save a process instance node.
	 *
	 * @param processInstanceNode the process instance node
	 * @return the process instance node
	 */
	ProcessInstanceNode save(ProcessInstanceNode processInstanceNode);

	/**
	 * Find a process instance node.
	 *
	 * @param uniqueProcessId the unique process id
	 * @param processInstanceId the process instance id
	 * @return the process instance node
	 */
	ProcessInstanceNode findProcessInstanceNode(String uniqueProcessId, String processInstanceId);
	
	/**
	 * Find an archived process instance node.
	 *
	 * @param uniqueProcessId the unique process id
	 * @param processInstanceId the process instance id
	 * @return the process instance node
	 */
	ProcessInstanceNode findArchivedProcessInstanceNode(String uniqueProcessId, String processInstanceId);

	/**
	 * Delete a process instance node
	 *
	 * @param processInstanceNode the process instance node
	 */
	void delete(ProcessInstanceNode processInstanceNode);
}
