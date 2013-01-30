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

import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

/**
 * The Class ConnectedTo is a Spring Data neo4j RelationshipEntity. It is used
 * to connect the flow node instances as they are connected in the bpmn process
 * definition. Its bpmn equivalent is the sequence flow.
 * 
 * @author christopher köster
 * 
 */
@RelationshipEntity(type = "CONNECTED_TO")
public class ConnectedTo {

	/**
	 * The graph id needed by Spring Data/Neo4j. Not to be accessed or used
	 * directly.
	 */
	@GraphId
	private Long graphId;

	/** The source node. */
	@StartNode
	private FlowNode sourceNode;

	/** The target node. */
	@EndNode
	private FlowNode targetNode;

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
	 * @param graphId
	 *            the new graph id
	 */
	public void setGraphId(Long graphId) {
		this.graphId = graphId;
	}

	/**
	 * Gets the source node.
	 * 
	 * @return the source node
	 */
	public FlowNode getSourceNode() {
		return sourceNode;
	}

	/**
	 * Sets the source node.
	 * 
	 * @param sourceNode
	 *            the new source node
	 */
	public void setSourceNode(FlowNode sourceNode) {
		this.sourceNode = sourceNode;
	}

	/**
	 * Gets the target node.
	 * 
	 * @return the target node
	 */
	public FlowNode getTargetNode() {
		return targetNode;
	}

	/**
	 * Sets the target node.
	 * 
	 * @param targetNode
	 *            the new target node
	 */
	public void setTargetNode(FlowNode targetNode) {
		this.targetNode = targetNode;
	}

}
