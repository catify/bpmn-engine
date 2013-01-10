package com.catify.processengine.core.integration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import com.catify.processengine.core.processdefinition.jaxb.TMetaData;
import com.catify.processengine.core.services.MessageDispatcherService;

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
	 * The implementation prefix is used to figure out if the chosen implementation is the right one. 
	 */
	protected String prefix = "implementationPrefix";
	
	/**
	 * Mapping between the uniqueFlowNoedId (which uniquely identifies a FlowNode in the bpmn engine) 
	 * and its message integration destination (eg. route).
	 * This map is used to dispatch messages between the engine and the service provider,
	 * so if used by receive(), send(), or requestReply() it needs to be filled on startReceive(), 
	 * startSend() and startRequestReply().
	 * <li> Key: 'uniqueFlowNoedId' <br>
	 * <li> Value: 'route'
	 */
	protected Map<String, String> flowNodeIdIntegrationMap = new HashMap<String, String>();

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
	 * Start a throwing message integration implementation (eg. route). A
	 * throwing message integration gets a message from the process engine to be
	 * sent by the message integration implementation (to some other service).
	 * In order to send messages with this implementation, the service provider
	 * needs to implement the {@linkplain dispatchIntegrationMessageToSpiImpl}
	 * method which will be invoked by the process engine for each message (see
	 * {@link MessageDispatcherService}).
	 * 
	 * @param uniqueFlowNodeId the unique flow node id
	 * @param messageIntegrationString
	 *            the message integration string defined in the bpmn process.xml
	 * @return true, if the messageIntegrationString could be processed, false
	 *         if the messageIntegrationString could not be processed (because
	 *         it belongs to another implementation or is erroneous)
	 */
	public abstract void startSend(
			final String uniqueFlowNodeId, String messageIntegrationString);
	
	/**
	 * Start a catching message integration implementation (eg. route). A
	 * catching message integration receives a message and then needs to fulfill
	 * the following steps: <li>create an Integration Message <li>send that
	 * integration message to the engine via the
	 * {@link MessageDispatcherService} <BR>
	 * <BR>
	 * 
	 * @param uniqueFlowNodeId
	 * 			  the unique flow node id
	 * @param messageIntegrationString
	 *            the message integration string defined in the bpmn process.xml
	 * @param tMetaDatas
	 * 			  the meta data list holds the meta data expressions 
	 * 			  of a flow node defined in the bpmn process.xml
	 * @return true, if the messageIntegrationString could be processed, false
	 *         if the messageIntegrationString could not be processed (because
	 *         it belongs to another implementation or is erroneous)
	 */
	public abstract void startReceive(
			final String uniqueFlowNodeId, String messageIntegrationString, List<TMetaData> tMetaDatas);
	
	/**
	 * Start a request reply integration implementation.
	 *
	 * @param uniqueFlowNodeId the unique flow node id
	 * @param messageIntegrationString
	 *            the message integration string defined in the bpmn process.xml
	 */
	public abstract void startRequestReply(
			final String uniqueFlowNodeId, String messageIntegrationString);

	/**
	 * Shut down an integration implementation (eg. route) of a given flow node.
	 * 
	 * @param uniqueFlowNodeId the unique flow node id
	 * @return true, if successful
	 */
	public abstract boolean shutDownIntegrationImplementation(final String uniqueFlowNodeId);
	
	/**
	 * Dispatch an integration message via the spi implementation. The
	 * {@link MessageDispatcherService} will call this method for outgoing
	 * messages. The SPI implementation must provide a way to map between the
	 * uniqueFlowNodeId and its message integration implementation
	 * (eg. route). The preferred way to do so, should be the use of
	 * the given hash map ({@link MessageDispatcherService}).
	 * @param integrationMessage
	 *            the integration message to be sent which is generated by the bpmn engine
	 */
	public abstract void send(IntegrationMessage integrationMessage);
	
	/**
	 * Dispatch message from outside to the process engine.
	 * @param integrationMessage
	 *            the integration message
	 * @param metaData
	 *            the meta data map holding the meta data names and values 
	 * 			  of a flow node to be saved in the process
	 */
	public abstract void receive(IntegrationMessage integrationMessage,
			Map<String, Object> metaData);
	
	/**
	 * Issue a request/reply via the spi implementation. The call must be synchronous and will 
	 * send the reply back to the request initiator. 
	 * @param message the message to be sent
	 *
	 * @return the object returned by the request
	 */
	public abstract Object requestReply(IntegrationMessage message);

	/**
	 * Convert a list of TMetaData (which is a jaxb generated type that only consists of two strings) 
	 * into a Map<String, String> that is easier to work with.
	 * <li>Key: 'metaDataName' <br>
	 * <li>Value: 'metaDataXpath'
	 * <br><br>
	 * 
	 * @param tMetaDatas
	 *            the list of TMetaData holding the meta data names and xpath expressions 
	 * 			  of a flow node defined in the bpmn process.xml
	 * @return the meta data names/xpaths map
	 */
	public Map<String, String> convertTMetaDataListToMap(
			List<TMetaData> tMetaDatas) {
		Map<String, String> metaData = new HashMap<String, String>();

		if(tMetaDatas != null) {
			for (TMetaData tMetaData : tMetaDatas) {
				metaData.put(tMetaData.getMetaDataKey(),
						tMetaData.getMetaDataXpath());
			}
		}

		return metaData;
	}

}
