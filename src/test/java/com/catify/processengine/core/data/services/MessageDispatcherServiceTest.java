package com.catify.processengine.core.data.services;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.catify.processengine.core.integration.IntegrationMessage;
import com.catify.processengine.core.integration.MessageIntegrationSPI;
import com.catify.processengine.core.services.MessageDispatcherService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:META-INF/spring/spring-context.xml" })
@Transactional
public class MessageDispatcherServiceTest {
	
	@Mock
	private MessageIntegrationSPI integrationSPI;
	
	@InjectMocks
	private MessageDispatcherService messageDispatcherServiceMockInjected = new MessageDispatcherService(integrationSPI);
	
	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		
		assertNotNull(messageDispatcherServiceMockInjected);
	}
	
	@Test
	public void testMessageDispatcherService() {
		MessageDispatcherService mds = new MessageDispatcherService(integrationSPI);
		assertNotNull(mds);
	}

	@Test
	public void testDispatchToEngine() {
		IntegrationMessage integrationMessage = getNewIntegrationMessage();
		
		messageDispatcherServiceMockInjected.dispatchToEngine(integrationMessage, null);

		// test of akka actor message receiving etc. can only be made when the upcoming JavaTestKit of akka 2.1 is released
		// other functionality will be tested in integration tests
		// method invocations to test: targetNodeActor.tell(triggerMessage) and metaDataActor.tell(metaDataMessage)
	}

	@Test
	public void testDispatchViaIntegrationSPI() {
		IntegrationMessage integrationMessage = getNewIntegrationMessage();
		
		messageDispatcherServiceMockInjected.dispatchViaIntegrationSPI("uniqueFlowNodeId", integrationMessage);
		
		verify(integrationSPI).send(integrationMessage);
	}

	@Test
	public void testRequestReplyViaIntegrationSPI() {
		IntegrationMessage integrationMessage = getNewIntegrationMessage();
		
		messageDispatcherServiceMockInjected.requestReplyViaIntegrationSPI("uniqueFlowNodeId", integrationMessage);
		
		verify(integrationSPI).requestReply(integrationMessage);
	}

	IntegrationMessage getNewIntegrationMessage() {
		return new IntegrationMessage("uniqueProcessId", "uniqueFlowNodeId", "processInstanceId", null);
	}
}
