package com.catify.processengine.core.data.repositories;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.neo4j.repository.IndexRepository;

import com.catify.processengine.core.data.model.entities.FlowNode;

/**
 * The Spring Data neo4j managed interface FlowNodeRepository provides
 * convenient methods for accessing {@link FlowNode}s in the database and has
 * been enhanced with custom Cypher queries. For implementation details please
 * see {@link GraphRepository}.
 */
public interface FlowNodeRepository extends GraphRepository<FlowNode>,
		IndexRepository<FlowNode> {

	@Query("start process=node:ProcessNode(uniqueProcessId={0}) match process-[r1:HAS*1..1000]->flownode where flownode.uniqueFlowNodeId = {1} return flownode")
	FlowNode findFlowNode(String uniqueProcessId, String uniqueFlowNodeId);
}
