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
package com.catify.processengine.core.data.dataobjects;

import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.catify.processengine.core.nodes.FlowElement;

/**
 * The DataObjectHandlingSPI defines the methods needed to implement the storing 
 * and loading of data objects bound to process nodes.
 * 
 * @author christopher köster
 * 
 */
public abstract class DataObjectSPI {
	
	static final Logger LOG = LoggerFactory.getLogger(FlowElement.class);

	/**
	 * Custom implementation id to figure
	 * out if the chosen implementation is the right one.
	 */
	protected String implementationId;

	/**
	 * Gets the implementation id.
	 *
	 * @return the implementation id
	 */
	public String getImplementationId() {
		return implementationId;
	}

	/**
	 * Gets the object key. Helper method to easily find a valid unique key.
	 *
	 * @param uniqueProcessId the unique process id
	 * @param objectId the object id
	 * @param instanceId the instance id
	 * @return the object key
	 */
	public String getObjectKey(String uniqueProcessId, String objectId,
			String instanceId) {
		return new String(uniqueProcessId + objectId + instanceId);
	}

	/**
	 * Save a data object.
	 * 
	 * @param uniqueProcessId
	 *            the unique process id
	 * @param objectId
	 *            the object id
	 * @param instanceId
	 *            the instance id
	 * @param dataObject
	 *            the data object
	 */
	public abstract void saveObject(String uniqueProcessId, String objectId,
			String instanceId, Object dataObject);

	/**
	 * Load a data object.
	 * 
	 * @param uniqueProcessId
	 *            the unique process id
	 * @param objectId
	 *            the object id
	 * @param instanceId
	 *            the instance id
	 * @return the object
	 */
	public abstract Object loadObject(String uniqueProcessId, String objectId,
			String instanceId);
	
	public abstract void deleteObject(String uniqueProcessId, String objectId, String instanceId);
	
	 /**
	 * Gets the data object service provider.
	 *
	 * @param implementationIdSet the implementationId set in the spring context
	 * @return the data object implementation
	 */
	public static DataObjectSPI getDataObjectServiceProvider(String implementationIdSet) {
		 
	     for (DataObjectSPI dataObjectProvider : DataObjectHandlingLoader) {
	    	 LOG.debug("Data Object SPI Id set in config: " + dataObjectProvider.getImplementationId() 
	    			 + "; Implementing Data Object SPI Id found: " + implementationIdSet);
	    	 if (dataObjectProvider.getImplementationId().equals(implementationIdSet)) {
	    		LOG.debug("Data Object service provider used: " + dataObjectProvider);
				return dataObjectProvider;
	    	 }
	     }
	     
	     // if no provider could be found, return null
	     LOG.error("Could not find a data object service provider. Saving data objects will fail!");
	     return null;
	 }
	 
	 /** The spi loader. Will load the available implementations <em>once</em> when first used. */
	private static ServiceLoader<DataObjectSPI> DataObjectHandlingLoader
    = ServiceLoader.load(DataObjectSPI.class);
	
}
