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
package com.catify.processengine.core.data.services;

import org.springframework.stereotype.Component;

import com.catify.processengine.core.data.model.entities.ArchiveNode;
import com.catify.processengine.core.data.model.entities.FlowNode;

/**
 * The Interface ArchivedNodeRepositoryService.
 * 
 * @author christopher köster
 * 
 */
@Component
public interface ArchivedNodeRepositoryService {

	/**
	 * Find an archive node by client id.
	 * 
	 * @param uniqueClientId
	 *            the flow node id
	 * @return the flow node
	 */
	ArchiveNode findByUniqueClientId(String uniqueClientId);

	/**
	 * Save an archive node to the database.
	 * 
	 * @param flowNode
	 *            the flow node
	 * @return the process
	 */
	ArchiveNode save(ArchiveNode archiveNode);

	/**
	 * Create an archive node or retrieve it from the db if it already exists.
	 * 
	 * @param flowNodeJaxb
	 *            the jaxb flow node
	 * @return {@link FlowNode}
	 */
	ArchiveNode getOrCreateArchivedNode(String uniqueClientId);
}
