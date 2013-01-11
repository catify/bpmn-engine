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
package com.catify.processengine.core.data.model.entities;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

/**
 * The root node is the master node of the graph database. 
 * It has no incoming connections but it has outgoing connections to all {@link ClientNodes}.
 * 
 * @author chris
 *
 */
@NodeEntity
public class RootNode {

	/**
	 * The graph id needed by Spring Data/Neo4j. Not to be accessed or used
	 * directly.
	 */
	@GraphId
	private Long graphId;

	/**
	 * The unique root node id is globally unique. This means, if this id is queried
	 * there can be only one result. At this time, there is only one root node.
	 */
	@Indexed
	//FIXME : provide a rootId
	private String uniqueRootNodeId = "secretRootId";

	/** The client nodes of this process (not fetched). */
	@RelatedTo(type = "ROOT", direction = Direction.OUTGOING)
	private Set<ClientNode> clientNodes = new HashSet<ClientNode>();
	
	/**
	 * Adds a relationship to a given client node.
	 *
	 * @param clientNode the client node
	 */
	public void addRelationshipToClientNode(ClientNode clientNode) {
		clientNodes.add(clientNode);
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
	 * Gets the unique root node id.
	 *
	 * @return the unique root node id
	 */
	public String getUniqueRootNodeId() {
		return uniqueRootNodeId;
	}

	/**
	 * Sets the unique root node id.
	 *
	 * @param uniqueRootNodeId the new unique root node id
	 */
	public void setUniqueRootNodeId(String uniqueRootNodeId) {
		this.uniqueRootNodeId = uniqueRootNodeId;
	}
}
