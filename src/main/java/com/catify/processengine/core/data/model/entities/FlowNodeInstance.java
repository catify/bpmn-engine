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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.data.neo4j.annotation.RelatedToVia;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import com.catify.processengine.core.data.model.NodeInstaceStates;

/**
 * The FlowNodeInstance holds the instance information for a single flow node.
 * The most important part being the state this flow node instance is in. The
 * FlowNodeInstance has a single {@link FlowNode} incoming {@link HasInstance}
 * relationship. This relationship holds the information about the process
 * instance id. It also is connected to (multiple) following instances that are
 * connected to it on {@link FlowNode} level.
 * <br><br>
 * Level 3 of the database process representation.
 * 
 * @author christopher köster
 * 
 */
@NodeEntity
@Configurable
public class FlowNodeInstance {

	public static final Logger LOG = LoggerFactory
			.getLogger(FlowNodeInstance.class);
	
	/**
	 * The graph id needed by Spring Data/Neo4j. Not to be accessed or used
	 * directly.
	 */
	@GraphId
	private Long graphId;

	/**
	 * The nodeInstanceState holds the state this flow node instance has right
	 * now. The states a node instance can have are declared in the
	 * {@link NodeInstaceStates} class. This state is the basis of every action
	 * a node service will perform.
	 */
	private String nodeInstanceState = NodeInstaceStates.INACTIVE_STATE;
	
	/** The loop count to track the number of loops on an instance. */
	private int loopCount = 0;

	/** The incoming fired flows needed until a gateway fires. */
	private int incomingFiredFlowsNeeded = 0;

	/** The count of incoming flows that fired. */
	private int flowsFired = 0;
	
	/** The node instance start time. */
	@DateTimeFormat(iso=ISO.DATE_TIME)
	private Date nodeInstanceStartTime;
	
	/** The node instance end time. */
	@DateTimeFormat(iso=ISO.DATE_TIME)
	private Date nodeInstanceEndTime;

	/** The following instances connected to this instance. */
	@RelatedTo(type = "HAS_FOLLOWING_INSTANCE", direction = Direction.OUTGOING)
	private Set<FlowNodeInstance> followingInstances = new HashSet<FlowNodeInstance>();

	/** The {@link HasInstance} relation can be accessed via this set. */
	@RelatedToVia(elementClass = HasInstance.class, direction = Direction.INCOMING)
	private Set<HasInstance> instanceOf = new HashSet<HasInstance>();

	public Set<HasInstance> getInstanceOf() {
		return instanceOf;
	}

	public void setInstanceOf(Set<HasInstance> instanceOf) {
		this.instanceOf = instanceOf;
	}

	/**
	 * Gets the set with the flow node this instance is an instance of.
	 * 
	 * @return the checks for instance node
	 */
	public Set<HasInstance> getHasInstanceRelationships() {
		return this.instanceOf;
	}

	/**
	 * Gets the {@link HasInstance} relationship to the flow node this instance
	 * is an instance of.
	 * 
	 * @return the the HasInstance relationship
	 */
	public HasInstance getHasInstanceRelationship() {
		// the HAS_INSTANCE relation is 1:1 but spring data does not allow
		// single fields in 2.0
		ArrayList<HasInstance> flowNodeSet = new ArrayList<HasInstance>(
				this.getHasInstanceRelationships());
		return flowNodeSet.get(0);
	}

	/**
	 * Get the (lazy loaded) flow node that this instance is an instance of.
	 * 
	 * @return the flow node instance
	 */
	public FlowNode getFlowNodeOfHasInstanceRelationship() {
		return this.getHasInstanceRelationship().getNode();
	}

	/**
	 * Get the (fetched) flow node that this instance is an instance of.
	 * 
	 * @return the flow node instance
	 */
	public FlowNode getFlowNode(
			Neo4jTemplate neo4jTemplate) {
		return neo4jTemplate.fetch(this.getHasInstanceRelationship().getNode());
	}
	
	/**
	 * Get the (not fetched!) flow node that this instance is an instance of.
	 * 
	 * @return the flow node instance
	 */
	public FlowNode getFlowNode() {
		return this.getHasInstanceRelationship().getNode();
	}
	
	/**
	 * Adds the {@link HasInstance} relation to the supervising flow node. From the
	 * point of view of the {@linkplain FlowNodeInstance} this is an 'instance
	 * of' relationship.
	 * 
	 * @param flowNode
	 *            the flow node
	 * @param instanceId
	 *            the instance id
	 * @return the checks for instance
	 */
	public HasInstance addAsInstanceOf(FlowNode flowNode, String instanceId) {
		HasInstance instance = new HasInstance(flowNode, this, instanceId);
		
		this.instanceOf.add(instance);

		return instance;
	}
	
	/**
	 * Move the process instance node to the archived process node.
	 *
	 * @param processNode the process node
	 * @param instanceId the instance id
	 */
	public void moveToArchive(FlowNode archiveFlowNode, String instanceId) {
		instanceOf.clear();
		addAsInstanceOf(archiveFlowNode, instanceId);
	}

	/**
	 * Instantiates a new flow node instance.
	 */
	public FlowNodeInstance() {
	}

	/**
	 * Instantiates a new flow node instance.
	 * 
	 * @param nodeInstanceState
	 *            the node instance state
	 */
	public FlowNodeInstance(String nodeInstanceState) {
		this.nodeInstanceState = nodeInstanceState;
	}

	/**
	 * Instantiates a new flow node instance.
	 * 
	 * @param nodeInstanceState
	 *            the node instance state
	 * @param incomingFiredFlowsNeeded
	 *            the fired flows needed
	 */
	public FlowNodeInstance(String nodeInstanceState, int firedFlowsNeeded) {
		this.nodeInstanceState = nodeInstanceState;
		this.incomingFiredFlowsNeeded = firedFlowsNeeded;
	}

	/**
	 * Adds the following instance.
	 * 
	 * @param flowNodeInstance
	 *            the flow node instance following
	 */
	public void addFollowingInstance(FlowNodeInstance flowNodeInstance) {
		this.followingInstances.add(flowNodeInstance);
	}

	/**
	 * Gets the following flow node instances.
	 *
	 * @return the following instances
	 */
	public Set<FlowNodeInstance> getFollowingInstances() {
		return followingInstances;
	}

	/**
	 * Sets the following flow node instances.
	 *
	 * @param followingInstances the new following instances
	 */
	public void setFollowingInstances(Set<FlowNodeInstance> followingInstances) {
		this.followingInstances = followingInstances;
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
	 * Gets the node instance state.
	 * 
	 * @return the node instance state
	 */
	public String getNodeInstanceState() {
		return nodeInstanceState;
	}

	/**
	 * Sets the node instance state.
	 * 
	 * @param nodeInstanceState
	 *            the new node instance state
	 */
	public void setNodeInstanceState(String nodeInstanceState) {
		this.nodeInstanceState = nodeInstanceState;
	}

	
	/**
	 * Gets the loop count.
	 *
	 * @return the loop count
	 */
	public int getLoopCount() {
		return loopCount;
	}

	/**
	 * Sets the loop count.
	 *
	 * @param loopCount the new loop count
	 */
	public void setLoopCount(int loopCount) {
		this.loopCount = loopCount;
	}
	
	/**
	 * Gets the fired flows needed.
	 * 
	 * @return the fired flows needed
	 */
	public int getIncomingFiredFlowsNeeded() {
		return incomingFiredFlowsNeeded;
	}

	/**
	 * Sets the fired flows needed.
	 * 
	 * @param incomingFiredFlowsNeeded
	 *            the new fired flows needed
	 */
	public void setIncomingFiredFlowsNeeded(int incomingFiredFlowsNeeded) {
		this.incomingFiredFlowsNeeded = incomingFiredFlowsNeeded;
	}

	/**
	 * Gets the flows fired.
	 * 
	 * @return the flows fired
	 */
	public int getFlowsFired() {
		return flowsFired;
	}

	/**
	 * Sets the flows fired.
	 * 
	 * @param flowsFired
	 *            the new flows fired
	 */
	public void setFlowsFired(int flowsFired) {
		this.flowsFired = flowsFired;
	}

	/**
	 * Gets the node instance start time.
	 *
	 * @return the node instance start time
	 */
	public Date getNodeInstanceStartTime() {
		return nodeInstanceStartTime;
	}

	/**
	 * Sets the node instance start time.
	 *
	 * @param nodeInstanceStartTime the new node instance start time
	 */
	public void setNodeInstanceStartTime(Date nodeInstanceStartTime) {
		this.nodeInstanceStartTime = nodeInstanceStartTime;
	}

	/**
	 * Gets the node instance end time.
	 *
	 * @return the node instance end time
	 */
	public Date getNodeInstanceEndTime() {
		return nodeInstanceEndTime;
	}

	/**
	 * Sets the node instance end time.
	 *
	 * @param nodeInstanceEndTime the new node instance end time
	 */
	public void setNodeInstanceEndTime(Date nodeInstanceEndTime) {
		this.nodeInstanceEndTime = nodeInstanceEndTime;
	}

}
