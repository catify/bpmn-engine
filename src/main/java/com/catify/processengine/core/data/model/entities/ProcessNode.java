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
 * The ProcessNode holds all flow elements of a bpmn process. A ProcessNode has an
 * incoming relationship from either the {@link RunningNode} or the {@link ArchiveNode} and outgoing relationships to its
 * {@link FlowNodes}.
 * 
 * <br>
 * <br>
 * Level 1 of the database process representation.
 * 
 * @author chris
 */
@NodeEntity
public class ProcessNode {

	/**
	 * The graph id needed by Spring Data/Neo4j. Not to be accessed or used
	 * directly.
	 */
	@GraphId
	private Long graphId;

	/**
	 * The unique process id can identify a process without specifying its
	 * version or client, because its globally unique. This means, if this id is
	 * queried there can be only one result.
	 */
	@Indexed
	private String uniqueProcessId;

	/** The process id defined in the bpmn-process.xml. */
	private String processId;

	/** The process name defined in the bpmn-process.xml. */
	private String processName;

	/** The process version defined in the bpmn-process.xml. */
	private String processVersion;

	/** The flow nodes of this process (eagerly fetched). */
	@RelatedTo(type = "HAS", direction = Direction.OUTGOING)
	private Set<FlowNode> flowNodes = new HashSet<FlowNode>();

	/**
	 * Instantiates a new process node.
	 */
	public ProcessNode() {
	}
	
	/**
	 * Instantiates a new process node.
	 *
	 * @param uniqueProcessId the unique process id
	 * @param processId the process id
	 * @param processName the process name
	 * @param processVersion the process version
	 */
	public ProcessNode(String uniqueProcessId, String processId, String processName,
			String processVersion) {
		this.uniqueProcessId = uniqueProcessId;
		this.processId = processId;
		this.processName = processName;
		this.processVersion = processVersion;
	}

	/**
	 * Adds the relationship to a given flow node.
	 *
	 * @param flowNode the flow node
	 */
	@Transactional
	public void addRelationshipToFlowNode(FlowNode flowNode) {
		flowNodes.add(flowNode);
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
	 * Gets the unique process id.
	 *
	 * @return the unique process id
	 */
	public String getUniqueProcessId() {
		return uniqueProcessId;
	}

	/**
	 * Sets the unique process id.
	 *
	 * @param uniqueProcessId the new unique process id
	 */
	public void setUniqueProcessId(String uniqueProcessId) {
		this.uniqueProcessId = uniqueProcessId;
	}

	/**
	 * Gets the process id.
	 *
	 * @return the process id
	 */
	public String getProcessId() {
		return processId;
	}

	/**
	 * Sets the process id.
	 *
	 * @param processId the new process id
	 */
	public void setProcessId(String processId) {
		this.processId = processId;
	}

	/**
	 * Gets the process name.
	 *
	 * @return the process name
	 */
	public String getProcessName() {
		return processName;
	}

	/**
	 * Sets the process name.
	 *
	 * @param processName the new process name
	 */
	public void setProcessName(String processName) {
		this.processName = processName;
	}

	/**
	 * Gets the process version.
	 *
	 * @return the process version
	 */
	public String getProcessVersion() {
		return processVersion;
	}

	/**
	 * Sets the process version.
	 *
	 * @param processVersion the new process version
	 */
	public void setProcessVersion(String processVersion) {
		this.processVersion = processVersion;
	}

	/**
	 * Gets the process flow nodes.
	 *
	 * @return the flow nodes of the process
	 */
	public Set<FlowNode> getFlowNodes() {
		return flowNodes;
	}

	/**
	 * Sets the process nodes.
	 *
	 * @param processNodes the new process nodes
	 */
	public void setFlowNodes(Set<FlowNode> processNodes) {
		this.flowNodes = processNodes;
	}

}
