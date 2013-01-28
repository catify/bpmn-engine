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

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

/**
 * Wrapper class for saving/loading a data object within a process. Any class
 * that needs to access such an object needs to bind an object of this class.
 * The actual implementation of the data access is provided via the {@link DataObjectSPI}.
 * 
 * @author chris
 * 
 */
@Configurable
public class DataObjectService {

	/** The data object implementation id set in the spring context. */
	@Value("${datastore.implementation}")
	private String dataObjectServiceProviderIdSetting;

	/** The data input object id. */
	private String dataInputObjectId;
	
	/** The data output object id. */
	private String dataOutputObjectId;
	
	/** The data object spi. */
	private DataObjectSPI dataObjectServiceProvider;

	/**
	 * Instantiates a new uninitialized data object service. Should not be used directly.
	 */
	public DataObjectService() {
	}
	
	/**
	 * Instantiates a new data object service that saves data via the user configured
	 * data object service provider. The service provider will be loaded from the 
	 * classpath or the java extension directory.
	 *
	 * @param dataInputObjectId the data input object id
	 * @param dataOutputObjectId the data output object id
	 */
	public DataObjectService(String dataInputObjectId, String dataOutputObjectId) {
		this.dataInputObjectId = dataInputObjectId;
		this.dataOutputObjectId = dataOutputObjectId;
	}
	
	/**
	 * Inits the dataObjectServiceProvider after construction, because the @Value 
	 * annotated fields get filled by spring <b>after</b> construction.
	 */
	@PostConstruct
	void initAnnotations() {
		if (this.dataObjectServiceProvider == null) {
			this.dataObjectServiceProvider = DataObjectSPI
					.getDataObjectServiceProvider(this.dataObjectServiceProviderIdSetting);
		}
	}
	
	/**
	 * Instantiates a new data object service that saves data via a given
	 * data object service provider.
	 *
	 * @param dataObjectServiceProvider the data object service provider
	 */
	public DataObjectService(DataObjectSPI dataObjectServiceProvider) {
		this.dataObjectServiceProvider = dataObjectServiceProvider;
	}

	/**
	 * Save a data object to the service node.
	 *
	 * @param uniqueProcessId the unique process id
	 * @param instanceId the instance id
	 * @param dataObject the data object
	 */
	public void saveObject(String uniqueProcessId, String instanceId,
			Object dataObject) {
		this.dataObjectServiceProvider.saveObject(uniqueProcessId,
				this.dataOutputObjectId, instanceId, dataObject);
	}

	/**
	 * Load the data object of the service node.
	 *
	 * @param uniqueProcessId the unique process id
	 * @param instanceId the instance id
	 * @return the data object loaded
	 */
	public Object loadObject(String uniqueProcessId, String instanceId) {
		return this.dataObjectServiceProvider.loadObject(uniqueProcessId,
				this.dataInputObjectId, instanceId);
	}
	
	/**
	 * Load the data object of the service node.
	 *
	 * @param uniqueProcessId the unique process id
	 * @param instanceId the instance id
	 * @param dataObjectId the data object id
	 * @return the data object loaded
	 */
	public Object loadObject(String uniqueProcessId, String instanceId,String dataObjectId) {
		return this.dataObjectServiceProvider.loadObject(uniqueProcessId, dataObjectId, instanceId);
	}
	
	/**
	 * Delete the data object(s) of the service node.
	 *
	 * @param uniqueProcessId the unique process id
	 * @param instanceId the instance id
	 */
	public void deleteObject(String uniqueProcessId, String instanceId) {
		if (this.dataInputObjectId != null) {
			this.dataObjectServiceProvider.deleteObject(uniqueProcessId,
					this.dataInputObjectId, instanceId);
		}
		
		if (this.dataOutputObjectId != null) {
			this.dataObjectServiceProvider.deleteObject(uniqueProcessId,
					this.dataOutputObjectId, instanceId);
		}
	}
	
	/**
	 * Delete the given data object by its id.
	 *
	 * @param uniqueProcessId the unique process id
	 * @param instanceId the instance id
	 */
	public void deleteObject(String uniqueProcessId, String dataObjectId, String instanceId) {
		if (dataObjectId != null) {
			this.dataObjectServiceProvider.deleteObject(uniqueProcessId,
					dataObjectId, instanceId);
		}
	}

	/**
	 * Gets the data object service providers id.
	 *
	 * @return the data object service provider
	 */
	public String getDataObjectServiceProviderId() {
		return dataObjectServiceProviderIdSetting;
	}

	/**
	 * Sets the data object service provider id.
	 *
	 * @param dataObjectServiceProviderId the new data object service provider id
	 */
	public void setDataObjectServiceProviderId(String dataObjectServiceProviderId) {
		this.dataObjectServiceProviderIdSetting = dataObjectServiceProviderId;
	}
	
	/**
	 * Sets the data object service provider.
	 *
	 * @param dataObjectServiceProvider the new data object service provider
	 */
	public void setDataObjectServiceProvider(
			DataObjectSPI dataObjectServiceProvider) {
		this.dataObjectServiceProvider = dataObjectServiceProvider;
	}
	
	public String getDataObjectServiceProviderIdSetting() {
		return dataObjectServiceProviderIdSetting;
	}

	public void setDataObjectServiceProviderIdSetting(
			String dataObjectServiceProviderIdSetting) {
		this.dataObjectServiceProviderIdSetting = dataObjectServiceProviderIdSetting;
	}

	/**
	 * Gets the data input object id.
	 *
	 * @return the data input object id
	 */
	public String getDataInputObjectId() {
		return dataInputObjectId;
	}

	/**
	 * Sets the data input object id.
	 *
	 * @param dataInputObjectId the new data input object id
	 */
	public void setDataInputObjectId(String dataInputObjectId) {
		this.dataInputObjectId = dataInputObjectId;
	}
	

	/**
	 * Gets the data output object id.
	 *
	 * @return the data output object id
	 */
	public String getDataOutputObjectId() {
		return dataOutputObjectId;
	}

	/**
	 * Sets the data output object id.
	 *
	 * @param dataOutputObjectId the new data output object id
	 */
	public void setDataOutputObjectId(String dataOutputObjectId) {
		this.dataOutputObjectId = dataOutputObjectId;
	}

}
