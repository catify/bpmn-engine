package com.catify.processengine.core.messageintegration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.catify.processengine.core.integration.IntegrationMessage;
import com.catify.processengine.core.integration.MessageIntegrationSPI;
import com.catify.processengine.core.processdefinition.jaxb.TMetaData;
import com.catify.processengine.core.services.MessageDispatcherService;

/**
 * Mock implementation of integration spi.
 * 
 * @author claus straube
 *
 */
public class MessageIntegrationSPIMock extends MessageIntegrationSPI {

	public String startSendUniqueFlowNodeId;
	public String startSendMessageIntegrationString;
	public String startReceiveUniqueFlowNodeId;
	public String startReceiveMessageIntegrationString;
	public List<TMetaData> startReceiveTMetaDatas;
	public String startRequestReplyMessageIntegrationString;
	public String startRequestReplyUniqueFlowNodeId;
	public List<IntegrationMessage> sends = new ArrayList<IntegrationMessage>();
	public List<IntegrationMessage> receives = new ArrayList<IntegrationMessage>();
	public List<IntegrationMessage> requestReplys = new ArrayList<IntegrationMessage>();
	private MessageDispatcherService messageDispatcherService;
	
	public static final String MOCK_PREFIX = "integration_mock";
	
	public MessageIntegrationSPIMock() {
		this.prefix = MOCK_PREFIX;
		this.messageDispatcherService = new MessageDispatcherService(this);
	}
	
	@Override
	public void startSend(String uniqueFlowNodeId,
			String messageIntegrationString) {
		this.startSendUniqueFlowNodeId = uniqueFlowNodeId;
		this.startSendMessageIntegrationString = messageIntegrationString;
	}

	@Override
	public void startReceive(String uniqueFlowNodeId,
			String messageIntegrationString, List<TMetaData> tMetaDatas) {
		this.startReceiveMessageIntegrationString = messageIntegrationString;
		this.startReceiveUniqueFlowNodeId = uniqueFlowNodeId;
		this.startReceiveTMetaDatas = tMetaDatas;
	}

	@Override
	public void startRequestReply(String uniqueFlowNodeId,
			String messageIntegrationString) {
		this.startRequestReplyUniqueFlowNodeId = uniqueFlowNodeId;
		this.startRequestReplyMessageIntegrationString = messageIntegrationString;
	}

	@Override
	public boolean shutDownIntegrationImplementation(String uniqueFlowNodeId) {
		return false;
	}

	@Override
	public void send(IntegrationMessage integrationMessage) {
		this.sends.add(integrationMessage);
	}

	@Override
	public void receive(IntegrationMessage integrationMessage,
			Map<String, Object> metaData) {
		this.receives.add(integrationMessage);
		this.messageDispatcherService.dispatchToEngine(integrationMessage, metaData);
	}

	@Override
	public Object requestReply(IntegrationMessage message) {
		this.requestReplys.add(message);
		return message;
	}

}
