/**
 * 
 */
package com.catify.processengine.core.data.repositories;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;

import com.catify.processengine.core.data.model.entities.ProcessInstanceNode;

/**
 * The Spring Data neo4j managed interface ProcessInstanceNodeRepository provides 
 * convenient methods for accessing {@link ProcessInstanceNode}s in the database and 
 * has been enhanced with custom Cypher queries.
 * For implementation details please see {@link GraphRepository}.
 *
 * @author chris
 */
public interface ProcessInstanceNodeRepository extends GraphRepository<ProcessInstanceNode>{
	
	/**
	 * Find a process instance based on the unique process id and instance id.
	 *
	 * @param uniqueProcessId the unique process id
	 * @param processInstanceId the process instance id
	 * @return the process instance node
	 */
	@Query("start process=node:ProcessNode(uniqueProcessId={0}) match process-[r:HAS_PROCESS_INSTANCE]->processInctanceNode where processInctanceNode.instanceId = {1} return processInctanceNode")
	ProcessInstanceNode findProcessInstanceNode(String uniqueProcessId, String processInstanceId);
}
