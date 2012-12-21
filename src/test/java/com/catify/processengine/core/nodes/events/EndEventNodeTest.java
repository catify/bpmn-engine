/**
 * 
 */
package com.catify.processengine.core.nodes.events;

import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;

import com.catify.processengine.core.data.services.impl.IdService;
import com.catify.processengine.core.nodes.Base_nodes;
import com.catify.processengine.core.nodes.NodeFactory;
import com.catify.processengine.core.processdefinition.jaxb.TFlowNode;
import com.catify.processengine.core.processdefinition.jaxb.TProcess;
import com.catify.processengine.core.services.ActorReferenceService;

/**
 * @author chris
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:META-INF/spring/spring-context.xml" })
@Transactional
public class EndEventNodeTest extends Base_nodes {

	@Autowired
	ActorSystem actorSystem;
	
	@Autowired
	NodeFactory nodeFactory;
	
	@Test
	public void testUnmarshalling() throws FileNotFoundException, JAXBException {

		List<TProcess> processList = getProcessesFromXML("data/testprocess_throw.xml");

		TProcess process = processList.get(0);
		assertNotNull(process);
	}

	@Test
	public void testAkkaNodeCreation() throws FileNotFoundException,
			JAXBException {

		List<TProcess> processList = getProcessesFromXML("data/testprocess_throw.xml");
		final TProcess process_jaxb = processList.get(0);

		assertNotNull(process_jaxb);

		final TFlowNode flowNode_jaxb = extractFlowNodes(process_jaxb).get("_4");

		assertNotNull(flowNode_jaxb);

		ActorRef nodeServiceActor = actorSystem.actorOf(new Props(
				new UntypedActorFactory() {
					private static final long serialVersionUID = 1L;

					public UntypedActor create() {
							return nodeFactory.createServiceNode("clientId", 
									process_jaxb, null, flowNode_jaxb,
									extractSequenceFlows(process_jaxb));
					}
				}).withDispatcher("file-mailbox-dispatcher"), 
				ActorReferenceService.getActorReferenceString(IdService.getUniqueFlowNodeId("clientId", process_jaxb, null, flowNode_jaxb) + this.getClass().getSimpleName()));

		assertNotNull(nodeServiceActor);
	}
	
}
