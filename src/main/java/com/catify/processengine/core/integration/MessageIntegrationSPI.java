package com.catify.processengine.core.integration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import com.catify.processengine.core.processdefinition.jaxb.TMetaData;
import com.catify.processengine.core.services.MessageDispatcherService;

// TODO: Auto-generated Javadoc
/**
 * The MessageIntegration Service Provider Interface allows to plug message
 * integration frameworks (like Apache Camel, Spring Integration) to the process
 * engine. The message integration is an extension to the bpmn specification to
 * ease the integration of catching and throwing nodes to the application
 * landscape.
 */
public abstract class MessageIntegrationSPI {

	 /**
 	 * Gets the message integration implementation.
 	 *
 	 * @param prefix the prefix used by the implementation
 	 * @return the message integration implementation
 	 */
 	public static MessageIntegrationSPI getMessageIntegrationImpl(String prefix) {
		 
	     for (MessageIntegrationSPI integrationProvider : messageIntegrationLoader) {
	    	 if (integrationProvider.getPrefix().equals(prefix)) {
				return integrationProvider;
	    	 }
	     }
	     // return null if prefix could not be matched with implementation
	     return null;
	 }
	 
	 /** The message integration loader. */
 	private static ServiceLoader<MessageIntegrationSPI> messageIntegrationLoader
     = ServiceLoader.load(MessageIntegrationSPI.class);
	
	/**
	 * You need to define your own implementation prefix as it is used to figure out
	 * if the chosen implementation is the right one 
	 */
	protected String prefix = "implementationPrefix";
	
	/**
	 * Map between the uniqueFlowNoedId, which uniquely identifies a FlowNode and its message integration implementation (eg. route).
	 */
	protected Map<String, String> flowNodeIdIntegrationImplMap = new HashMap<String, String>();
	
	protected Map<String, Object> metaDataValues;
	protected Map<String, String> metaDataXpaths;

	/**
	 * Gets the prefix that is used to identify the message integration
	 * implementation. This method will always be called before the engine tries
	 * to use on of the methods of the SPI implementation. The string returned
	 * therefore must be the same as defined in the xml file of that process.
	 * 
	 * @return the implementation prefix, provided by the implementation
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * Dispatch an integration message via the spi implementation. The
	 * {@link MessageDispatcherService} will call this method for outgoing
	 * messages. The SPI implementation must provide a way to map between the
	 * uniqueFlowNodeId and its message integration implementation
	 * (eg. route). The preferred way to do so, should be the use of
	 * the given hash map ({@link MessageDispatcherService}).
	 * 
	 * @param uniqueFlowNodeId the unique flow node id
	 * @param integrationMessage
	 *            the integration message
	 */
	public abstract void dispatchIntegrationMessageViaSpiImpl(final String uniqueFlowNodeId,
			IntegrationMessage integrationMessage);

	/**
	 * Start a catching message integration implementation (eg. route). A
	 * catching message integration receives a message and then needs to fulfill
	 * the following steps: <li>create an Integration Message <li>send that
	 * integration message to the engine via the
	 * {@link MessageDispatcherService} <BR>
	 * <BR>
	 * 
	 * @param messageIntegrationString
	 *            the message integration string defined in the bpmn.xml
	 * @return true, if the messageIntegrationString could be processed, false
	 *         if the messageIntegrationString could not be processed (because
	 *         it belongs to another implementation or is erroneous)
	 */
	public abstract void startCatchingIntegrationImplementation(
			final String uniqueFlowNodeId, String messageIntegrationString, List<TMetaData> metaDataList);

	/**
	 * Start a throwing message integration implementation (eg. route). A
	 * throwing message integration gets a message from the process engine to be
	 * sent by the message integration implementation (to some other service).
	 * In order to send messages with this implementation, the service provider
	 * needs to implement the {@linkplain dispatchIntegrationMessageToSpiImpl}
	 * method which will be invoked by the process engine for each message (see
	 * {@link MessageDispatcherService}).
	 * 
	 * @param messageIntegrationString
	 *            the message integration string defined in the bpmn.xml
	 * @param messageIntegrationString
	 * @param uniqueFlowNodeId the unique flow node id
	 * @return true, if the messageIntegrationString could be processed, false
	 *         if the messageIntegrationString could not be processed (because
	 *         it belongs to another implementation or is erroneous)
	 */
	public abstract void startThrowingIntegrationImplementation(
			final String uniqueFlowNodeId, String messageIntegrationString);
	
	/**
	 * Start a request reply integration implementation.
	 *
	 * @param uniqueFlowNodeId the unique flow node id
	 * @param messageIntegrationString the message integration string
	 */
	public abstract void startRequestReplyIntegrationImplementation(
			final String uniqueFlowNodeId, String messageIntegrationString);

	/**
	 * Shut down integration implementation (eg. route).
	 * 
	 * @param uniqueFlowNodeId
	 *            the integration id that uniquely identifies the integration
	 *            implementation (eg. route id)
	 * @return true, if successful
	 */
	public abstract boolean shutDownIntegrationImplementation(final String uniqueFlowNodeId);

	/**
	 * Dispatch message from outside to the process engine.
	 * 
	 * @param uniqueFlowNodeId
	 *            the integration id
	 * @param integrationMessage
	 *            the integration message
	 */
	public abstract void dispatchToEngine(final String uniqueFlowNodeId,
			IntegrationMessage integrationMessage, Map<String, Object> metaData);
	
	/**
	 * Gets the meta data specified in the integration implementation.
	 *
	 * @return the meta data
	 */
	public Map<String, Object> getMetaData() {
		return metaDataValues;
	}

	/**
	 * Sets the meta data specified in the integration implementation.
	 *
	 * @param metaData the meta data
	 */
	public void setMetaData(Map<String, Object> metaData) {
		this.metaDataValues = metaData;
	}

	/**
	 * Issue a request/reply via the spi implementation. The call must be synchronous and will 
	 * send the reply back to the request initiator. 
	 *
	 * @param uniqueFlowNodeId the unique flow node id
	 * @param message the message
	 * @return the object
	 */
	public abstract Object requestReplyViaSpiImpl(String uniqueFlowNodeId,
			IntegrationMessage message);
}
