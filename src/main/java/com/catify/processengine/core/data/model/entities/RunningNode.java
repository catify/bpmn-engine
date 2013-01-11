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
package com.catify.processengine.core.data.model.entities;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Class RunningNode.
 */
@NodeEntity
public class RunningNode {

	/**
	 * The graph id needed by Spring Data/Neo4j. Not to be accessed or used
	 * directly.
	 */
	@GraphId
	private Long graphId;
	
	/**
	 * The unique client id is globally unique. This means, if this id is queried
	 * there can be only one result.
	 */
	@Indexed
	private String uniqueClientId;
	
	/** The process nodes of this client (not fetched). */
	@RelatedTo(type = "HAS_RUNNING_PROCESS", direction = Direction.OUTGOING)
	private Set<ProcessNode> runningProcessNodes = new HashSet<ProcessNode>();
	
	
	/**
	 * Instantiates a new running node.
	 */
	public RunningNode() {
	}
	
	/**
	 * Instantiates a new running node.
	 *
	 * @param uniqueClientId the unique client id
	 */
	public RunningNode(String uniqueClientId) {
		this.uniqueClientId = uniqueClientId;
	}
	
	/**
	 * Adds the relationship to the given process node.
	 *
	 * @param processNode the process node
	 */
	@Transactional
	public void addRelationshipToProcessNode(ProcessNode processNode) {
		runningProcessNodes.add(processNode);
	}

	/**
	 * Gets the graph id.
	 *
	 * @return the graph id
	 */
	public Long getGraphId() {
		return graphId;
	}

	/**
	 * Sets the graph id.
	 *
	 * @param graphId the new graph id
	 */
	public void setGraphId(Long graphId) {
		this.graphId = graphId;
	}

	/**
	 * Gets the unique client id.
	 *
	 * @return the unique client id
	 */
	public String getUniqueClientId() {
		return uniqueClientId;
	}

	/**
	 * Sets the unique client id.
	 *
	 * @param uniqueClientId the new unique client id
	 */
	public void setUniqueClientId(String uniqueClientId) {
		this.uniqueClientId = uniqueClientId;
	}
}
