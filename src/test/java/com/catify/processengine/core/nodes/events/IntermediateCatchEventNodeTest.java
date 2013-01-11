/**
 * -------------------------------------------------------
 * Copyright (C) 2013 catify <info@catify.com>
 * -------------------------------------------------------
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
public class IntermediateCatchEventNodeTest extends Base_nodes {

	@Autowired
	ActorSystem actorSystem;
	
	@Autowired
	NodeFactory nodeFactory;
	
	@Test
	public void testUnmarshalling() throws FileNotFoundException, JAXBException {

		List<TProcess> processList = getProcessesFromXML("data/testprocess.xml");

		TProcess process = processList.get(0);
		assertNotNull(process);
	}

	@Test
	public void testAkkaNodeCreation() throws FileNotFoundException,
			JAXBException {

		List<TProcess> processList = getProcessesFromXML("data/testprocess.xml");
		final TProcess process_jaxb = processList.get(0);

		assertNotNull(process_jaxb);

		final TFlowNode flowNode_jaxb = extractFlowNodes(process_jaxb).get("_3");

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
