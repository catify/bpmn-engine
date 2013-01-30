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
package com.catify.processengine.core.data.repositories;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.neo4j.repository.IndexRepository;

import com.catify.processengine.core.data.model.entities.FlowNode;

/**
 * The Spring Data neo4j managed interface FlowNodeRepository provides
 * convenient methods for accessing {@link FlowNode}s in the database and has
 * been enhanced with custom Cypher queries. For implementation details please
 * see {@link GraphRepository}.
 * 
 * @author christopher köster
 * 
 */
public interface FlowNodeRepository extends GraphRepository<FlowNode>,
		IndexRepository<FlowNode> {

	@Query("start process=node:ProcessNode(uniqueProcessId={0}) match process-[r1:HAS*1..1000]->flownode where flownode.uniqueFlowNodeId = {1} return flownode")
	FlowNode findFlowNode(String uniqueProcessId, String uniqueFlowNodeId);
}
