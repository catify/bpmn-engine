package com.catify.processengine.core;

import java.util.Iterator;

import javax.xml.bind.JAXBElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import akka.actor.ActorSystem;

import com.catify.processengine.core.messages.ActivationMessage;
import com.catify.processengine.core.messages.TriggerMessage;
import com.catify.processengine.core.processdefinition.jaxb.TDefinitions;
import com.catify.processengine.core.processdefinition.jaxb.TProcess;
import com.catify.processengine.core.processdefinition.jaxb.TRootElement;

/**
 * Delegates the initialization of the node instance services and their data
 * beans defined in a given process.
 * 
 * @author chris
 * 
 */
@Configurable
public class ProcessInitializer {

	static final Logger LOG = LoggerFactory.getLogger(ProcessInitializer.class);

	@Autowired
	private ActorSystem actorSystem;

	// FIXME: for testing purposes only
//	public static Date start;

	/**
	 * Initialize process(es) of a jaxb definition object. A definition object
	 * can hold multiple process objects. (root element of bpmn definition) (immer eingelesen, da notwendige infos nicht im datenmodell)
	 * 
	 * @param definition
	 *            {@link TDefinitions}
	 */
	public void initializeProcessDefinition(String clientId,
			TDefinitions definition) {
		for (Iterator<JAXBElement<? extends TRootElement>> iterator = definition
				.getRootElement().iterator(); iterator.hasNext();) {

			TRootElement rootElement = iterator.next().getValue();
			TProcess process = null;

			if (rootElement.getClass().equals(TProcess.class)) {
				process = (TProcess) rootElement;
				this.initializeProcess(clientId, process);
			}
		}
	}

	/**
	 * Initialize a process with a jaxb process object. A process consists of
	 * flow nodes and sequence flows.
	 * 
	 * @param clientId
	 *            the client id
	 * @param processJaxb
	 *            the process jaxb {@link TProcess}
	 */
	public void initializeProcess(String clientId, TProcess processJaxb) {
		LOG.debug(String.format("Instantiating %s:%s with %s elements",
				processJaxb.getClass().getSimpleName(), processJaxb.getId(),
				processJaxb.getFlowElement().size()));

		// initialize data entities (data representation)
		EntityInitialization entityInit = new EntityInitialization();
		entityInit.initializeProcess(clientId, processJaxb);

		
		// FIXME: testing only
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// causes the dependency cycle and will be removed after implementation of unit tests
//		start = new Date();
		for (int i = 0; i < 1; i++) {
			actorSystem.actorFor(
//					"user/TestClient_PID:1_PushTest_Version:1.0_NID:2_StartEvent").tell(
//					new TriggerMessage());
					
					"user/Client_9nullProcessVersion_11Start_Event").tell(
					new TriggerMessage());
		}

	}

}
