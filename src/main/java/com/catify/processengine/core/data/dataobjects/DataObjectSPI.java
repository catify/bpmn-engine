package com.catify.processengine.core.data.dataobjects;

import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.catify.processengine.core.nodes.FlowElement;

/**
 * The DataObjectHandlingSPI defines the methods needed to implement the storing 
 * and loading of data objects bound to process nodes.
 * 
 * @author chris
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
	 * Gets the spi implementation.
	 *
	 * @param implementationId the implementationId used by the implementation
	 * @return the message integration implementation
	 */
	public static DataObjectSPI getDataObjectHandlingImpl(String implementationId) {
		 
	     for (DataObjectSPI dataObjectProvider : DataObjectHandlingLoader) {
	    	 LOG.debug("Applications Data Object SPI Id: " + dataObjectProvider.getImplementationId() + " Implementing Data Object SPI Id: " + implementationId);
	    	 if (dataObjectProvider.getImplementationId().equals(implementationId)) {
	    		LOG.debug("Data Object service provider found: " + dataObjectProvider);
				return dataObjectProvider;
	    	 }
	     }
	     
	     // if no provider could be found, return null
	     return null;
	 }
	 
	 /** The spi loader. Will load the available implementations <em>once</em> when first used. */
	private static ServiceLoader<DataObjectSPI> DataObjectHandlingLoader
    = ServiceLoader.load(DataObjectSPI.class);
	
}
