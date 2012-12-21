/**
 * 
 */
package com.catify.processengine.core.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import akka.actor.UntypedActor;

import com.catify.processengine.core.integration.MessageIntegrationSPI;
import com.catify.processengine.core.messages.MetaDataMessage;

/**
 * The MetaDataService asynchronously writes the meta data collected in the
 * {@link MessageIntegrationSPI} implementation.
 * 
 * @author chris
 */
@Configurable
public class MetaDataService extends UntypedActor {

	@Autowired
	private ProcessInstanceMediatorService processInstanceMediatorService;
	
	public MetaDataService() {
		processInstanceMediatorService = new ProcessInstanceMediatorService();
	}

	/**
	 * On receiving of a MetaDataMessage this actor will write the meta data to
	 * the process instance node
	 */
	@Override
	public void onReceive(Object message) {
		if (message instanceof MetaDataMessage) {

			MetaDataMessage metaMessage = (MetaDataMessage) message;

			processInstanceMediatorService.setMetaDataProperties(
					metaMessage.getUniqueProcessID(),
					metaMessage.getProcessInstanceId(),
					metaMessage.getMetaData());

		} else {
			unhandled(message);
		}
	}

}
