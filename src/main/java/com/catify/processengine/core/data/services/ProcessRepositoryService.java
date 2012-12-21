/**
 * 
 */
package com.catify.processengine.core.data.services;

import org.springframework.stereotype.Component;

import com.catify.processengine.core.data.model.entities.ProcessNode;

/**
 * The ProcessNode wraps methods of the ProcessRepository and provides
 * additional methods that hide implementation details.
 * 
 * @author chris
 */
@Component
public interface ProcessRepositoryService {

	/**
	 * Find by process id.
	 * 
	 * @param uniqueProcessId
	 *            the unique process id
	 * @return the process
	 */
	ProcessNode findByUniqueProcessId(String uniqueProcessId);
	
	/**
	 * Find an archive process by a running process id.
	 * 
	 * @param uniqueProcessId
	 *            the unique process id
	 * @return the process
	 */
	ProcessNode findArchivedByRunningUniqueProcessId(String uniqueProcessId);

	/**
	 * Save a process to the database.
	 * 
	 * @param process
	 *            the process
	 * @return the process
	 */
	ProcessNode save(ProcessNode process);

	/**
	 * Delete a process by its id.
	 * 
	 * @param uniqueProcessId
	 *            the unique process id
	 * @return true, if successful, false if no process with given id found
	 */
	boolean delete(String uniqueProcessId);

	/**
	 * Get an existing process from the database or create and persist the proposed process in the database.
	 *
	 * @param proposedProcessNode the proposed process node
	 * @return the existing or created process node
	 */
	ProcessNode getOrCreateProcessNode(ProcessNode proposedProcessNode);
}
