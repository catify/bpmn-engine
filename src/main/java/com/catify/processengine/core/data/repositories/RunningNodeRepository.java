package com.catify.processengine.core.data.repositories;

import org.springframework.data.neo4j.repository.GraphRepository;

import com.catify.processengine.core.data.model.entities.RunningNode;

/**
 * The Spring Data neo4j managed interface RunningNodeRepository provides 
 * convenient methods for accessing {@link RunningNode}s in the database. 
 * For implementation details please see {@link GraphRepository}.
 */
public interface RunningNodeRepository extends GraphRepository<RunningNode> {

}
