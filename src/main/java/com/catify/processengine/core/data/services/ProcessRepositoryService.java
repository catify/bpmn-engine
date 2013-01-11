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
