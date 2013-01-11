/**
 * -------------------------------------------------------
 * Copyright (C) 2013 catify <info@catify.com>
 * -------------------------------------------------------
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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.data.neo4j.annotation.RelatedToVia;
import org.springframework.data.neo4j.fieldaccess.DynamicProperties;
import org.springframework.data.neo4j.fieldaccess.DynamicPropertiesContainer;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

/**
 * The Class ProcessInstanceNode.
 *
 * @author chris
 */
@NodeEntity
public class ProcessInstanceNode {

	/**
	 * The graph id needed by Spring Data/Neo4j. Not to be accessed or used
	 * directly.
	 */
	@GraphId
	private Long graphId;

	/** The instance id. */
	@Indexed
	private String instanceId;
	
	/** The process instance start time. */
	@DateTimeFormat(iso=ISO.DATE_TIME)
	private Date processInstanceStartTime;
	
	/** The process instance end time. */
	@DateTimeFormat(iso=ISO.DATE_TIME)
	private Date processInstanceEndTime;
	
	/** The meta data properties. */
	@Indexed
	private DynamicProperties metaDataProperties = new DynamicPropertiesContainer();
	
	/** The {@link HasProcessInstance} relation can be accessed via this set. 
	 * From the point of view of the {@linkplain ProcessInstanceNode} this is an instance of relationship. */
	@RelatedToVia(elementClass = HasProcessInstance.class, direction = Direction.INCOMING)
	private Set<HasProcessInstance> instanceOf = new HashSet<HasProcessInstance>();

	/** The start event node instances of this process instance (not eagerly fetched). */
	@RelatedTo(type = "START_VIA", direction = Direction.OUTGOING)
	private Set<FlowNodeInstance> startEventNodes = new HashSet<FlowNodeInstance>();
	
	/**
	 * Instantiates a new process instance node.
	 */
	public ProcessInstanceNode() {
		
	}
	
	/**
	 * Instantiates a new process instance node with the given start time.
	 *
	 * @param processInstanceId the process instance id
	 * @param processInstanceStartTime the process instance start time
	 */
	public ProcessInstanceNode(String processInstanceId, Date processInstanceStartTime) {
		this.setInstanceId(processInstanceId);
		this.processInstanceStartTime = processInstanceStartTime;
	}
	
	/**
	 * Instantiates a new process instance node with a start time and connects it to the given process node.
	 *
	 * @param processNode the process node
	 * @param processInstanceId the process instance id
	 * @param processInstanceStartTime the process instance start time
	 */
	public ProcessInstanceNode(ProcessNode processNode, String processInstanceId, Date processInstanceStartTime) {
		this.setInstanceId(processInstanceId);
		this.processInstanceStartTime = processInstanceStartTime;
		
		this.addAsInstanceOf(processNode, processInstanceId);
	}
	
	/**
	 * Adds the process instance node to a process and creates the relationship between the two.
	 *
	 * @param processNode the process node
	 * @param instanceId the instance id
	 * @return the checks for process instance
	 */
	public final HasProcessInstance addAsInstanceOf(ProcessNode processNode, String instanceId) {
		HasProcessInstance instance = new HasProcessInstance(processNode, this);
		this.instanceOf.add(instance);

		return instance;
	}
	
	/**
	 * Move the process instance node to the archived process node.
	 *
	 * @param archivedProcessNode the archived process node
	 * @param instanceId the instance id
	 * @return the checks for process instance
	 */
	public void moveInstanceToArchive(ProcessNode archivedProcessNode, String instanceId) {
			instanceOf.clear();
			addAsInstanceOf(archivedProcessNode, instanceId);
	}
	
	/**
	 * Adds a relationship to a given start event node instance.
	 *
	 * @param startEventInstance the start event node instance 
	 */
	public void addRelationshipToStartEventInstance(FlowNodeInstance startEventInstance) {
		getStartEventNodes().add(startEventInstance);
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
	 * Sets the property.
	 *
	 * @param key the key
	 * @param value the value
	 */
	public void setProperty(String key, Object value) {
		metaDataProperties.setProperty(key, value);
	}

	/**
	 * Gets the property.
	 *
	 * @param key the key
	 * @return the property
	 */
	public Object getProperty(String key) {
		return metaDataProperties.getProperty(key);
	}
	
	/**
	 * Gets the meta data properties.
	 *
	 * @return the meta data properties
	 */
	public DynamicProperties getMetaDataProperties() {
		return metaDataProperties;
	}

	/**
	 * Sets the meta data properties.
	 *
	 * @param metaDataProperties the new meta data properties
	 */
	public void setMetaDataProperties(DynamicProperties metaDataProperties) {
		this.metaDataProperties = metaDataProperties;
	}

	/**
	 * Gets the 'instance of'-relationship to the process this process instance is an instance of.
	 *
	 * @return the instance of relationship
	 */
	public Set<HasProcessInstance> getInstanceOf() {
		return instanceOf;
	}

	/**
	 * Sets the 'instance of' relationship to the process this process instance is an instance of.
	 *
	 * @param instanceOf the new instance of
	 */
	public void setInstanceOf(Set<HasProcessInstance> instanceOf) {
		this.instanceOf = instanceOf;
	}
	
	/**
	 * Gets the start event nodes.
	 *
	 * @return the start event nodes
	 */
	public Set<FlowNodeInstance> getStartEventNodes() {
		return startEventNodes;
	}

	/**
	 * Sets the start event nodes.
	 *
	 * @param startEventNodes the new start event nodes
	 */
	public void setStartEventNodes(Set<FlowNodeInstance> startEventNodes) {
		this.startEventNodes = startEventNodes;
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
	 * Gets the process instance start time.
	 *
	 * @return the process instance start time
	 */
	public Date getProcessInstanceStartTime() {
		return processInstanceStartTime;
	}

	/**
	 * Sets the process instance start time.
	 *
	 * @param processInstanceStartTime the new process instance start time
	 */
	public void setProcessInstanceStart(Date processInstanceStartTime) {
		this.processInstanceStartTime = processInstanceStartTime;
	}

	/**
	 * Gets the process instance end time.
	 *
	 * @return the process instance end time
	 */
	public Date getProcessInstanceEndTime() {
		return processInstanceEndTime;
	}

	/**
	 * Sets the process instance end time.
	 *
	 * @param processInstanceEndTime the new process instance end time
	 */
	public void setProcessInstanceEndTime(Date processInstanceEndTime) {
		this.processInstanceEndTime = processInstanceEndTime;
	}

}
