package com.catify.processengine.core.data.repositories;

import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.neo4j.repository.IndexRepository;

import com.catify.processengine.core.data.model.entities.ProcessNode;

/**
 * The Spring Data neo4j managed interface ProcessRepository provides convenient
 * methods for accessing {@link }s in the database. For implementation details
 * please see {@link GraphRepository}.
 */
public interface ProcessRepository extends GraphRepository<ProcessNode>,
		IndexRepository<ProcessNode> {
}
