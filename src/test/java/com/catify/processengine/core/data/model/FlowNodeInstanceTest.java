/**
 * 
 */
package com.catify.processengine.core.data.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.catify.processengine.core.data.model.entities.FlowNode;
import com.catify.processengine.core.data.model.entities.FlowNodeInstance;
import com.catify.processengine.core.data.repositories.FlowNodeInstanceRepository;
import com.catify.processengine.core.data.repositories.FlowNodeRepository;

/**
 * As this is a DAO only the critical methods will be tested (mostly spring data involving methods). 
 * 
 * @author chris
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:META-INF/spring/spring-context.xml" })
@Transactional
public class FlowNodeInstanceTest extends ModelTestBase {

	@Autowired
	private FlowNodeInstanceRepository flowNodeInstanceRepository;
	
	@Autowired
	private FlowNodeRepository flowNodeRepository;

	@Test
	public void testSaveAndLoad() {

		// create a flow node instance
		FlowNodeInstance flowNodeInstance = createFlowNodeInstance();

		// save it to the db
		flowNodeInstanceRepository.save(flowNodeInstance);

		// try to load it from the db
		flowNodeInstance = flowNodeInstanceRepository.findOne(flowNodeInstance
				.getGraphId());

		// check if the flow node instance could be loaded
		assertNotNull(flowNodeInstance);
	}

	@Test
	public void testInstanceOf() {

		// create a flow node instance
		FlowNodeInstance flowNodeInstance = createFlowNodeInstance();
		flowNodeInstanceRepository.save(flowNodeInstance);

		// create a FlowNode object
		FlowNode flowNode = createFlowNode(UNIQUE_FLOWNODE_ID, FLOWNODE_ID,
				FlOWNODE_TYPE, FLOWNODE_NAME);
		assertNotNull(flowNode);
		flowNodeRepository.save(flowNode);

		// create instanceOf relationship 
		flowNodeInstance.addAsInstanceOf(flowNode, "TESTINSTANCEID");
		flowNodeInstanceRepository.save(flowNodeInstance);
		
		// the flow node should now be accessible through the instanceOf relationship
		assertEquals(flowNode, flowNodeInstance.getHasInstanceRelationship().getNode());
	}
	
	@Test
	public void testFollowingInstance() {

		// create a flow node instance
		FlowNodeInstance flowNodeInstance = createFlowNodeInstance();
		flowNodeInstanceRepository.save(flowNodeInstance);

		// create a second flow node instance
		FlowNodeInstance followingInstance = createFlowNodeInstance();
		flowNodeInstanceRepository.save(followingInstance);


		// create followingInstance relationship 
		flowNodeInstance.addFollowingInstance(followingInstance);
		flowNodeInstanceRepository.save(flowNodeInstance);
		
		// the following flow node instance should now be accessible through the followingInstance relationship
		assertTrue(flowNodeInstance.getFollowingInstances().contains(followingInstance));
	}
	
}
