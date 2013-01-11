/**
 * *******************************************************
 * Copyright (C) 2013 catify <info@catify.com>
 * *******************************************************
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
