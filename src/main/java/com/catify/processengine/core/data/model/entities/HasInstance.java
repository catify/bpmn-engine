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
 * @author chris
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

	/** The instance id. */
	@Indexed
	private String instanceId;

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
		this.instanceId = instanceId;
	}

	/**
	 * Gets the instance id.
	 *
	 * @return the instance id
	 */
	public String getInstanceId() {
		return instanceId;
	}

	/**
	 * Sets the instance id.
	 *
	 * @param instanceId the new instance id
	 */
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
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
