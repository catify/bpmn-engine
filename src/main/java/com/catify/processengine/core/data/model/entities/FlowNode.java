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
import org.springframework.data.neo4j.annotation.RelatedToVia;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.transaction.annotation.Transactional;


/**
 * The FlowNode is equivalent to the flow node in bpmn. This is its database
 * representation. All inherited flow nodes (like Events, Tasks etc.) are saved
 * in the database as FlowNodes. The actual implementation that this node
 * represents is stored in the field {@link nodeType}. A FlowNode has an
 * incoming relationship from its process and outgoing {@link HasInstance} relationships to its
 * {@link FlowNodeInstances}.
 * <br><br>
 * Level 2 of the database process representation.
 * 
 * @author chris
 */
@NodeEntity
public class FlowNode {

	/**
	 * The graph id needed by Spring Data/Neo4j. Not to be accessed or used
	 * directly.
	 */
	@GraphId
	private Long graphId;

	/**
	 * The unique flow node id can identify a flow node without specifying its
	 * process, because its globally unique. This means, if this id is queried
	 * there can be only one result.
	 */
	@Indexed
	private String uniqueFlowNodeId;

	/** The flow node id defined in the bpmn-process.xml. */
	private String flowNodeId;

	/** The node type this database entry represents. (eg. a StartEvent) */
	private String nodeType;

	/** The name defined in the bpmn-process.xml. */
	private String name;

	/**
	 * The incoming fired flows needed until a gateway fires. Note: This is
	 * saved at both {@link FlowNode} level and {@link FlowNodeInstance} level
	 * to be able to easily instantiate those {@linkplain FlowNodeInstance}
	 * needing the information.
	 */
	private int firedFlowsNeeded = 0;
	
	/** The data input object id. */
	private String dataInputObjectId;
	
	/** The data output object id. */
	private String dataOutputObjectId;

	/** The following flow nodes connected to this flow node. */
	@RelatedTo(type = "CONNECTED_TO", direction = Direction.OUTGOING)
	private Set<FlowNode> followingFlowNodes = new HashSet<FlowNode>();
	
	/** The flow nodes of this sub process flow node (if this is a sub process) (not eagerly fetched - be careful when 
	 * fetching this, because of the recursion of nested sub processes). */
	@RelatedTo(type = "HAS", direction = Direction.OUTGOING)
	private Set<FlowNode> subProcessNodes = new HashSet<FlowNode>();

	/**
	 * Gets the sub process nodes (not eagerly fetched - be careful when 
	 * fetching this, because of the recursion of nested sub processes).
	 *
	 * @return the sub process nodes
	 */
	public Set<FlowNode> getSubProcessNodes() {
		return subProcessNodes;
	}

	/**
	 * Sets the sub process nodes.
	 *
	 * @param subProcessNodes the new sub process nodes
	 */
	public void setSubProcessNodes(Set<FlowNode> subProcessNodes) {
		this.subProcessNodes = subProcessNodes;
	}

	/**
	 * Gets the following flow nodes.
	 * 
	 * @return the following flow nodes
	 */
	public Set<FlowNode> getFollowingFlowNodes() {
		return followingFlowNodes;
	}

	/** The {@link ConnectedTo} relation can be accessed via this set. */
	@RelatedToVia(elementClass = ConnectedTo.class, direction = Direction.OUTGOING)
	private Set<ConnectedTo> connectedTo = new HashSet<ConnectedTo>();;

	/**
	 * Instantiates a new flow node.
	 */
	public FlowNode() {
	}

	/**
	 * Instantiates a new flow node. (use for non-gateway nodes)
	 * 
	 * @param uniqueFlowNodeId
	 *            the unique flow node id
	 * @param flowNodeId
	 *            the process flow node id
	 * @param typeOfNode
	 *            the type of the node
	 * @param name
	 *            the name
	 */
	public FlowNode(String uniqueFlowNodeId, String flowNodeId,
			String typeOfNode, String name) {
		this.uniqueFlowNodeId = uniqueFlowNodeId;
		this.flowNodeId = flowNodeId;
		this.nodeType = typeOfNode;
		this.name = name;
	}
	
	/**
	 * Instantiates a new flow node. (use for non-gateway nodes with data associations)
	 * 
	 * @param uniqueFlowNodeId
	 *            the unique flow node id
	 * @param flowNodeId
	 *            the process flow node id
	 * @param typeOfNode
	 *            the type of the node
	 * @param name
	 *            the name
	 */
	public FlowNode(String uniqueFlowNodeId, String flowNodeId,
			String typeOfNode, String name, String dataInputObjectId, String dataOutputObjectId) {
		this.uniqueFlowNodeId = uniqueFlowNodeId;
		this.flowNodeId = flowNodeId;
		this.nodeType = typeOfNode;
		this.name = name;
		this.dataInputObjectId = dataInputObjectId;
		this.dataOutputObjectId = dataOutputObjectId;
	}

	/**
	 * Instantiates a new flow node. (use for gateway-nodes)
	 * 
	 * @param uniqueFlowNodeId
	 *            the unique flow node id
	 * @param processFlowNodeId
	 *            the process flow node id
	 * @param typeOfNode
	 *            the type of the node
	 * @param name
	 *            the name
	 * @param firedFlowsNeeded
	 *            the fired flows needed
	 */
	public FlowNode(String uniqueFlowNodeId, String processFlowNodeId,
			String typeOfNode, String name, int firedFlowsNeeded) {
		this.uniqueFlowNodeId = uniqueFlowNodeId;
		this.flowNodeId = processFlowNodeId;
		this.nodeType = typeOfNode;
		this.name = name;
		this.firedFlowsNeeded = firedFlowsNeeded;
	}

	/**
	 * Add following flow nodes.
	 * 
	 * @param neo4jTemplate
	 *            the neo4jTemplate
	 * @param flowNode
	 *            the flow node
	 */
	@Transactional
	public void addFollowingFlowNodes(Neo4jTemplate neo4jTemplate,
			FlowNode flowNode) {
		neo4jTemplate.createRelationshipBetween(this, flowNode, ConnectedTo.class,
				"CONNECTED_TO", false);
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
	 * Gets the unique flow node id.
	 * 
	 * @return the unique flow node id
	 */
	public String getUniqueFlowNodeId() {
		return uniqueFlowNodeId;
	}

	/**
	 * Sets the unique flow node id.
	 * 
	 * @param uniqueFlowNodeId
	 *            the new unique flow node id
	 */
	public void setUniqueFlowNodeId(String uniqueFlowNodeId) {
		this.uniqueFlowNodeId = uniqueFlowNodeId;
	}

	/**
	 * Gets the flow node id.
	 * 
	 * @return the flow node id
	 */
	public String getFlowNodeId() {
		return flowNodeId;
	}

	/**
	 * Gets the node type.
	 * 
	 * @return the node type
	 */
	public String getNodeType() {
		return nodeType;
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 * 
	 * @param name
	 *            the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the fired flows needed.
	 * 
	 * @return the fired flows needed
	 */
	public int getFiredFlowsNeeded() {
		return firedFlowsNeeded;
	}

	/**
	 * Sets the fired flows needed.
	 * 
	 * @param firedFlowsNeeded
	 *            the new fired flows needed
	 */
	public void setFiredFlowsNeeded(int firedFlowsNeeded) {
		this.firedFlowsNeeded = firedFlowsNeeded;
	}

	/**
	 * Gets the connected to relationships.
	 *
	 * @return the connected to
	 */
	public Set<ConnectedTo> getConnectedTo() {
		return connectedTo;
	}

	/**
	 * Sets the connected to relationships.
	 *
	 * @param connectedTo the new connected to
	 */
	public void setConnectedTo(Set<ConnectedTo> connectedTo) {
		this.connectedTo = connectedTo;
	}

	/**
	 * Adds a relationship to a given sub process node.
	 *
	 * @param subProcessNode the sub process node
	 */
	public void addRelationshipToSubProcessNode(FlowNode subProcessNode) {
		subProcessNodes.add(subProcessNode);
	}

	public String getDataInputObjectId() {
		return dataInputObjectId;
	}

	public void setDataInputObjectId(String dataObjectId) {
		this.dataInputObjectId = dataObjectId;
	}

	public String getDataOutputObjectId() {
		return dataOutputObjectId;
	}

	public void setDataOutputObjectId(String dataOutputObjectId) {
		this.dataOutputObjectId = dataOutputObjectId;
	}
}
