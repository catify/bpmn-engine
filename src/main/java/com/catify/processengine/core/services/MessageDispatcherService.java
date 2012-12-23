package com.catify.processengine.core.services;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import com.catify.processengine.core.integration.IntegrationMessage;
import com.catify.processengine.core.integration.MessageIntegrationSPI;
import com.catify.processengine.core.messages.MetaDataMessage;
import com.catify.processengine.core.messages.TriggerMessage;
import com.catify.processengine.core.nodes.eventdefinition.EventDefinition;

/**
 * The MessageDispatcherService dispatches messages between the nodes of the
 * process engine and the {@link MessageIntegrationSPI} implementation.
 */
@Configurable
public class MessageDispatcherService {

	static final Logger LOG = LoggerFactory.getLogger(MessageDispatcherService.class);

	/**
	 * Holds the message integration implementation.
	 * 
	 * @see EventDefinition
	 */
	private MessageIntegrationSPI integrationSPI;
	
	@Autowired // autowiring does not work when needed in constructor, see redmine #98
	private ActorSystem actorSystem; //= ActorSystem.create("ProcessEngine");
	
	private ActorRef metaDataActor;

	/** The unique flow node id to actor ref map. */
	public static Map<String, String> uniqueFlowNodeIdToActorRefMap = new HashMap<String, String>();

	public MessageDispatcherService(MessageIntegrationSPI integrationSPI) {
		this.integrationSPI = integrationSPI;
//		this.metaDataActor = this.actorSystem.actorFor("akka://ProcessEngine/user/metaDataWriter");
	}
	
	/**
	 * Dispatch messages from message integration to the engine.
	 * 
	 * @param uniqueFlowNodeId
	 *            the unique flow node id
	 * @param integrationMessage
	 *            the message that should be dispatched to the engine
	 */
	public void dispatchToEngine(String uniqueFlowNodeId,
			IntegrationMessage integrationMessage, Map<String, Object> metaData) {

		// get the actor to send the integration message to
		String targetNodeActorString = uniqueFlowNodeIdToActorRefMap
				.get(uniqueFlowNodeId);
		ActorRef targetNodeActor = this.actorSystem
				.actorFor("user/" + targetNodeActorString);
		
		// create the integration message
		TriggerMessage triggerMessage = new TriggerMessage(
				integrationMessage.getProcessInstanceId(),
				integrationMessage.getPayload());

		LOG.debug("Message Dispatcher sending trigger message to " + targetNodeActor);
		
		// send the integration message to the actor
		targetNodeActor.tell(triggerMessage);
		
		// send the meta data to the meta data actor (if it is not a start event)
		if (integrationMessage.getProcessInstanceId() != null) {
			MetaDataMessage metaDataMessage = new MetaDataMessage(integrationMessage.getProcessId(), integrationMessage.getProcessInstanceId(), metaData);
			
			this.metaDataActor = this.actorSystem.actorFor("akka://ProcessEngine/user/metaDataWriter");
			
			LOG.debug("Message Dispatcher sending meta data message to " + this.metaDataActor);
			
			metaDataActor.tell(metaDataMessage);
		}
	}

	/**
	 * Dispatch messages from the engine via the integration spi implementation.
	 * 
	 * @param integrationPrefix
	 *            the integration prefix
	 * @param uniqueFlowNodeId
	 *            the unique flow node id
	 * @param message
	 *            the message that should be dispatched via the spi
	 *            implementation
	 */
	public void dispatchViaIntegrationSPI(final String uniqueFlowNodeId, final IntegrationMessage message) {
		integrationSPI.dispatchIntegrationMessageViaSpiImpl(uniqueFlowNodeId,
				message);
	}

	/**
	 * Dispatch messages from the engine via the integration spi implementation and return the response.
	 * 
	 * @param integrationPrefix
	 *            the integration prefix
	 * @param uniqueFlowNodeId
	 *            the unique flow node id
	 * @param message
	 *            the message that should be dispatched via the spi
	 *            implementation
	 */
	public Object requestReplyViaIntegrationSPI(final String uniqueFlowNodeId, final IntegrationMessage message) {
		return integrationSPI.requestReplyViaSpiImpl(uniqueFlowNodeId,
				message);
	}
}
