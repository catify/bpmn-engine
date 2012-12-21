/**
 * 
 */
package com.catify.processengine.core.data.repositories;

import org.springframework.data.neo4j.repository.GraphRepository;

import com.catify.processengine.core.data.model.entities.ClientNode;

/**
 * The Spring Data neo4j managed interface ClientNodeRepository provides 
 * convenient methods for accessing {@link ClientNode}s in the database. 
 * For implementation details please see {@link GraphRepository}.
 *
 * @author chris
 */
public interface ClientNodeRepository extends GraphRepository<ClientNode> {

}
