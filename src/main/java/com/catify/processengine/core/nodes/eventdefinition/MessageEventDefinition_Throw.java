package com.catify.processengine.core.nodes.eventdefinition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.catify.processengine.core.integration.IntegrationMessage;
import com.catify.processengine.core.integration.MessageIntegrationSPI;
import com.catify.processengine.core.messages.ActivationMessage;
import com.catify.processengine.core.messages.DeactivationMessage;
import com.catify.processengine.core.messages.TriggerMessage;
import com.catify.processengine.core.nodes.NodeFactory;
import com.catify.processengine.core.processdefinition.jaxb.TMessageIntegration;
import com.catify.processengine.core.services.MessageDispatcherService;

// TODO: Auto-generated Javadoc
/**
 * Each (akka) node that has a throwing message event definition instantiates
 * and binds an object of the MessageEventDefinition_Throw class. This class
 * implements the messaging part of the throwing message event node. Each time
 * the node gets an activation message its activation method is triggered, which
 * dispatches a message via a message integration SPI implementation. The
 * message can have a payload object, which is defined in the process.xml and
 * loaded from the data store. For instantiation of this node see {@link NodeFactory}.
 */
public class MessageEventDefinition_Throw implements EventDefinition {

	static final Logger LOG = LoggerFactory
			.getLogger(MessageEventDefinition_Throw.class);

	private final String uniqueProcessId;
	private final String uniqueFlowNodeId;

	/** The actor ref string of the actor that holds this event definition. */
	private final String actorRefString;

	private MessageIntegrationSPI integrationSPI;
	private MessageDispatcherService messageDispatcherService = null;

	/**
	 * Instantiates a new throwing message event definition.
	 * 
	 * @param uniqueProcessId
	 *            the unique process id
	 * @param uniqueFlowNodeId
	 *            the unique flow node id
	 * @param actorRefString
	 *            the actor reference string
	 * @param messageIntegration
	 *            the message event definition
	 */
	public MessageEventDefinition_Throw(String uniqueProcessId,
			String uniqueFlowNodeId, String actorRefString,
			TMessageIntegration messageIntegration) {

		this.uniqueProcessId = uniqueProcessId;
		this.uniqueFlowNodeId = uniqueFlowNodeId;
		this.actorRefString = actorRefString;

		if (messageIntegration != null) {
			this.integrationSPI = MessageIntegrationSPI
					.getMessageIntegrationImpl(messageIntegration.getPrefix());
			this.messageDispatcherService = new MessageDispatcherService(
					this.integrationSPI);
			registerMessageEventDefinition_throw(messageIntegration);
		}
	}

	@Override
	public void acitivate(ActivationMessage message) {

		// get the data from the data store that is associated with this node
		Object data;
		if (message.getPayload() != null) {
			data = message.getPayload();
		} else {
			data = "no payload";
		}

		// create an IntegrationMessage to be send to the message dispatcher
		IntegrationMessage integrationMessage = new IntegrationMessage(
				this.uniqueProcessId, this.uniqueFlowNodeId,
				message.getProcessInstanceId(), data);

		// dispatch that message via the integration spi
		messageDispatcherService.dispatchViaIntegrationSPI(
				this.uniqueFlowNodeId, integrationMessage);
	}

	@Override
	public void deactivate(DeactivationMessage message) {
		// deactivation is done on process level
	}

	@Override
	public void trigger(TriggerMessage message) {
		LOG.warn(
				"WARNING %s sent to throwing node. By definition this is not allowed to happen and is most likely an error",
				message);
	}

	/**
	 * Register throwing message event definition.
	 * 
	 * @param messageIntegration
	 *            the jaxb message integration
	 */
	public final void registerMessageEventDefinition_throw(
			TMessageIntegration messageIntegration) {
		// start the message integration implementation for this flow node (like
		// routes etc.)
		integrationSPI.startThrowingIntegrationImplementation(
				this.uniqueFlowNodeId,
				messageIntegration.getIntegrationstring());

		// add it to the message dispatchers mapping table
		MessageDispatcherService.uniqueFlowNodeIdToActorRefMap.put(
				this.uniqueFlowNodeId, this.actorRefString);
	}

}
