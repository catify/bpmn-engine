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

import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;


// TODO: Auto-generated Javadoc
/**
 * The Class HasProcessInstance is a Spring Data neo4j RelationshipEntity. 
 * It is the graph connection between {@link ProcessNode} and their instances
 * ({@link ProcessInstanceNode}) that holds the instance id.
 * 
 * @author christopher köster
 * 
 */
@RelationshipEntity(type = "HAS_PROCESS_INSTANCE")
public class HasProcessInstance {

	/**
	 * The graph id needed by Spring Data/Neo4j. Not to be accessed or used
	 * directly.
	 */
	@GraphId
	private Long graphId;

	/** The process node. */
	@StartNode
	private ProcessNode processNode;

	/** The process instance node. */
	@EndNode
	private ProcessInstanceNode processInstanceNode;
	
	/**
	 * Instantiates a new process instance relationship.
	 */
	public HasProcessInstance() {
	}
	
	/**
	 * Instantiates a new process instance relationship between the given {@link ProcessNode} and {@link ProcessInstanceNode}.
	 *
	 * @param processNode the process node
	 * @param processInstanceNode the process instance node
	 */
	public HasProcessInstance(ProcessNode processNode, ProcessInstanceNode processInstanceNode) {
		super();
		this.processNode = processNode;
		this.processInstanceNode = processInstanceNode;
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
	 * Gets the process node.
	 *
	 * @return the process node
	 */
	public ProcessNode getProcessNode() {
		return processNode;
	}

	/**
	 * Sets the process node.
	 *
	 * @param processNode the new process node
	 */
	public void setProcessNode(ProcessNode processNode) {
		this.processNode = processNode;
	}

	/**
	 * Gets the process instance node.
	 *
	 * @return the process instance node
	 */
	public ProcessInstanceNode getProcessInstanceNode() {
		return processInstanceNode;
	}

	/**
	 * Sets the process instance node.
	 *
	 * @param processInstanceNode the new process instance node
	 */
	public void setProcessInstanceNode(ProcessInstanceNode processInstanceNode) {
		this.processInstanceNode = processInstanceNode;
	}
}
