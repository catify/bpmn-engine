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
import org.springframework.transaction.annotation.Transactional;

/**
 * The client node has an outgoing connection to its running and archive node. 
 * A client should be only allowed to access nodes that are in the scope of his client node.
 * 
 * @author chris
 *
 */
@NodeEntity
public class ClientNode {

	/**
	 * The graph id needed by Spring Data/Neo4j. Not to be accessed or used
	 * directly.
	 */
	@GraphId
	private Long graphId;

	/**
	 * The unique client id is globally unique. This means, if this id is queried
	 * there can be only one result.
	 */
	@Indexed
	private String uniqueClientId;
	
	/** The running process node of this client (not fetched). */
	@RelatedTo(type = "HAS_RUNNING_PROCESSES", direction = Direction.OUTGOING)
	private Set<RunningNode> runningProcessNodes = new HashSet<RunningNode>();
	
	/** The archived process node of this client (not fetched). */
	@RelatedTo(type = "HAS_ARCHIVED_PROCESSES", direction = Direction.OUTGOING)
	private Set<ArchiveNode> archivedProcessNodes = new HashSet<ArchiveNode>();

	
	/**
	 * Instantiates a new client node.
	 */
	public ClientNode() {
	}
	
	/**
	 * Instantiates a new client node.
	 *
	 * @param uniqueClientId the unique client id
	 */
	public ClientNode(String uniqueClientId) {
		this.uniqueClientId = uniqueClientId;
	}
	
	/**
	 * Add running process node.
	 *
	 * @param runningNode the running node
	 */
	@Transactional
	public void addRelationshipToRunningProcessNode(RunningNode runningNode) {
		this.runningProcessNodes.add(runningNode);
	}
	
	/**
	 * Add archived process node.
	 *
	 * @param archivedNode the archived node
	 */
	@Transactional
	public void addRelationshipToArchivedProcessNode(ArchiveNode archivedNode) {
		this.archivedProcessNodes.add(archivedNode);
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
	 * Gets the unique client id.
	 *
	 * @return the unique client id
	 */
	public String getUniqueClientId() {
		return uniqueClientId;
	}

	/**
	 * Sets the unique client id.
	 *
	 * @param uniqueClientId the new unique client id
	 */
	public void setUniqueClientId(String uniqueClientId) {
		this.uniqueClientId = uniqueClientId;
	}
}
