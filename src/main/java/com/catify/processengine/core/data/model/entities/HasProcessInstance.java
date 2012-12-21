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
 * @author chris
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
