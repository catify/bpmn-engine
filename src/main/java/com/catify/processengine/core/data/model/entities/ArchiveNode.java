package com.catify.processengine.core.data.model.entities;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

/**
 * The Class ArchiveNode will mirror the {@link RunningNode}s processes and flow nodes, which hold all archived instances.
 */
@NodeEntity
public class ArchiveNode {

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
	
	/** The process nodes of this client (not fetched). */
	@RelatedTo(type = "HAS_ARCHIVED_PROCESS", direction = Direction.OUTGOING)
	private Set<ProcessNode> archivedProcessNodes = new HashSet<ProcessNode>();
	
	
	/**
	 * Instantiates a new archive node.
	 */
	public ArchiveNode() {
	}
	
	/**
	 * Instantiates a new archive node.
	 *
	 * @param uniqueClientId the unique client id
	 */
	public ArchiveNode(String uniqueClientId) {
		this.setUniqueClientId(uniqueClientId);
	}
	
	/**
	 * Adds a relationship to a given process node.
	 *
	 * @param processNode the process node
	 */
	public void addRelationshipToProcessNode(ProcessNode processNode) {
		archivedProcessNodes.add(processNode);
	}

	/**
	 * Gets the archived process nodes.
	 *
	 * @return the archived process nodes
	 */
	public Set<ProcessNode> getArchivedProcessNodes() {
		return archivedProcessNodes;
	}

	/**
	 * Sets the archived process nodes.
	 *
	 * @param archivedProcessNodes the new archived process nodes
	 */
	public void setArchivedProcessNodes(Set<ProcessNode> archivedProcessNodes) {
		this.archivedProcessNodes = archivedProcessNodes;
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
