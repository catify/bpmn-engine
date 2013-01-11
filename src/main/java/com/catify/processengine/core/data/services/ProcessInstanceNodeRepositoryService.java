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
