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
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;


/**
 * The Class HasInstance is a Spring Data neo4j RelationshipEntity. 
 * It is the graph connection between {@link FlowNode} and their instances
 * ({@link FlowNodeInstance}) that holds the instance id.
 * 
 * @author christopher köster
 * 
 */
@RelationshipEntity(type = "HAS_INSTANCE")
public class HasInstance {

	/**
	 * The graph id needed by Spring Data/Neo4j. Not to be accessed or used
	 * directly.
	 */
	@GraphId
	private Long graphId;

	/** The node. */
	@StartNode
	private FlowNode node;

	/** The flow node instance. */
	@EndNode
	private FlowNodeInstance flowNodeInstance;

	/** The process instance id. */
	@Indexed
	private String processInstanceId;

	/**
	 * Instantiates a new has instance relationship.
	 */
	public HasInstance() {
	}

	/**
	 * Instantiates a new has instance relationship between the given {@link FlowNode} and {@link FlowNodeInstance}.
	 *
	 * @param node the node
	 * @param flowNodeInstance the flow node instance
	 * @param instanceId the instance id
	 */
	public HasInstance(FlowNode node, FlowNodeInstance flowNodeInstance,
			String instanceId) {
		super();
		this.node = node;
		this.flowNodeInstance = flowNodeInstance;
		this.processInstanceId = instanceId;
	}

	/**
	 * Gets the process instance id.
	 *
	 * @return the instance id
	 */
	public String getProcessInstanceId() {
		return processInstanceId;
	}

	/**
	 * Sets the process instance id.
	 *
	 * @param instanceId the new instance id
	 */
	public void setProcessInstanceId(String instanceId) {
		this.processInstanceId = instanceId;
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
	 * Gets the flow node.
	 *
	 * @return the flow node
	 */
	public FlowNode getNode() {
		return node;
	}

	/**
	 * Sets the flow node.
	 *
	 * @param flowNode the new flow node
	 */
	public void setNode(FlowNode flowNode) {
		this.node = flowNode;
	}

	/**
	 * Gets the flow node instance.
	 *
	 * @return the flow node instance
	 */
	public FlowNodeInstance getFlowNodeInstance() {
		return flowNodeInstance;
	}

	/**
	 * Sets the flow node instance.
	 *
	 * @param flowNodeInstance the new flow node instance
	 */
	public void setFlowNodeInstance(FlowNodeInstance flowNodeInstance) {
		this.flowNodeInstance = flowNodeInstance;
	}
}
