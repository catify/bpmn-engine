/**
 * 
 */
package com.catify.processengine.core.data.repositories;

import org.springframework.data.neo4j.repository.GraphRepository;

import com.catify.processengine.core.data.model.entities.RootNode;

/**
 * The Spring Data neo4j managed interface RootNodeRepository provides
 * convenient methods for accessing {@link RootNode}s in the database. For
 * implementation details please see {@link GraphRepository}.
 * 
 * @author chris
 */
public interface RootNodeRepository extends GraphRepository<RootNode> {

}
