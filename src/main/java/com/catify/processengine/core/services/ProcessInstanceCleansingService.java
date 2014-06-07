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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import akka.actor.UntypedActor;

import com.catify.processengine.core.spi.DataObjectHandling;
import com.catify.processengine.core.messages.ArchiveMessage;
import com.catify.processengine.core.messages.DeletionMessage;

/**
 * The ProcessInstanceCleansingService either deletes or archives a process instance asynchronously.
 * 
 * @author christopher köster
 * 
 */
@Configurable
public class ProcessInstanceCleansingService extends UntypedActor {

	static final Logger LOG = LoggerFactory
			.getLogger(ProcessInstanceCleansingService.class);
	
	@Autowired
	private ProcessInstanceMediatorService processInstanceMediatorService;
	
	private DataObjectHandling dataObjectService = new DataObjectHandling();
	
	public ProcessInstanceCleansingService() {
		
	}

	/**
	 * On receiving of a MetaDataMessage this actor will write the meta data to
	 * the process instance node
	 */
	@Override
	public void onReceive(Object message) {
		
		LOG.debug(this.getSelf() + " received " + message.getClass().getSimpleName());
		
		// the handling of the data objects saved by a process instance must be evaluated, see redmine #113
		if (message instanceof ArchiveMessage) {
			processInstanceMediatorService.archiveProcessInstance(
					((ArchiveMessage) message).getUniqueProcessId(),
					((ArchiveMessage) message).getProcessInstanceId(), 
					((ArchiveMessage) message).getEndTime());
			LOG.debug(String.format("Archived process instance with instance id '%s'", ((ArchiveMessage) message).getProcessInstanceId()));
		} else if (message instanceof DeletionMessage) {
			processInstanceMediatorService.deleteProcessInstance(
					((DeletionMessage) message).getUniqueProcessId(),
					((DeletionMessage) message).getProcessInstanceId());
			
			for (String dataObjectId : ((DeletionMessage) message).getDataObjectIds()) {
				this.getDataObjectService().deleteObject(((DeletionMessage) message).getUniqueProcessId(), 
						dataObjectId, ((DeletionMessage) message).getProcessInstanceId());
			}
			LOG.debug(String.format("Deleted process instance with instance id '%s'", ((ArchiveMessage) message).getProcessInstanceId()));
		} else {
			unhandled(message);
		}
	}

	/**
	 * Gets the data object service.
	 *
	 * @return the data object service
	 */
	public DataObjectHandling getDataObjectService() {
		return dataObjectService;
	}

	/**
	 * Sets the data object service.
	 *
	 * @param dataObjectService the new data object service
	 */
	public void setDataObjectService(DataObjectHandling dataObjectService) {
		this.dataObjectService = dataObjectService;
	}

}
