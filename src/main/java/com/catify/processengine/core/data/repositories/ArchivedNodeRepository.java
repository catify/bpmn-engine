package com.catify.processengine.core.data.repositories;

import org.springframework.data.neo4j.repository.GraphRepository;

import com.catify.processengine.core.data.model.entities.ArchiveNode;

/**
 * The Spring Data neo4j managed interface ArchivedNodeRepository provides 
 * convenient methods for accessing {@link ArchiveNode}s in the database. 
 * For implementation details please see {@link GraphRepository}.
 */
public interface ArchivedNodeRepository extends GraphRepository<ArchiveNode> {

}
