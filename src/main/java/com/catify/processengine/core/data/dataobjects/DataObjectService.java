package com.catify.processengine.core.data.dataobjects;

import org.springframework.stereotype.Component;

/**
 * Wrapper class for saving/loading a data object within a process. Any class
 * that needs to access such an object needs to bind an object of this class.
 * The actual implementation of the data access is provided via the {@link DataObjectSPI}.
 * 
 * @author chris
 * 
 */
@Component
public class DataObjectService {

	/** The data object implementation id set in the spring context. */
	private String dataObjectServiceProviderIdSetting = "voldemort"; // FIXME: setting this in Spring does not work, see issue #1 in github

	public String getDataObjectServiceProviderIdSetting() {
		return dataObjectServiceProviderIdSetting;
	}

	public void setDataObjectServiceProviderIdSetting(
			String dataObjectServiceProviderIdSetting) {
		this.dataObjectServiceProviderIdSetting = dataObjectServiceProviderIdSetting;
	}

	/** The data input object id. */
	private String dataInputObjectId;
	
	/** The data output object id. */
	private String dataOutputObjectId;
	
	/** The data object handling spi. */
	private DataObjectSPI dataObjectHandlingSPI;

	/**
	 * Instantiates a new data object service.
	 */
	public DataObjectService() {
		this.dataObjectHandlingSPI = DataObjectSPI
				.getDataObjectHandlingImpl(this.dataObjectServiceProviderIdSetting);
	}
	
//	@PostConstruct
//	void initAnnotations() {
//		this.dataObjectHandlingSPI = DataObjectSPI
//				.getDataObjectHandlingImpl(this.dataObjectServiceProviderIdSetting);
//	}
	
	/**
	 * Instantiates a new data object service.
	 *
	 * @param dataInputObjectId the data input object id
	 * @param dataOutputObjectId the data output object id
	 */
	public DataObjectService(String dataInputObjectId, String dataOutputObjectId) {
		this.dataInputObjectId = dataInputObjectId;
		this.dataOutputObjectId = dataOutputObjectId;
		this.dataObjectHandlingSPI = DataObjectSPI
				.getDataObjectHandlingImpl(this.dataObjectServiceProviderIdSetting);
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
		this.dataObjectHandlingSPI.saveObject(uniqueProcessId,
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
		return this.dataObjectHandlingSPI.loadObject(uniqueProcessId,
				this.dataInputObjectId, instanceId);
	}
	
	/**
	 * Delete the data object(s) of the service node.
	 *
	 * @param uniqueProcessId the unique process id
	 * @param instanceId the instance id
	 */
	public void deleteObject(String uniqueProcessId, String instanceId) {
		if (this.dataInputObjectId != null) {
			this.dataObjectHandlingSPI.deleteObject(uniqueProcessId,
					this.dataInputObjectId, instanceId);
		}
		
		if (this.dataOutputObjectId != null) {
			this.dataObjectHandlingSPI.deleteObject(uniqueProcessId,
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
			this.dataObjectHandlingSPI.deleteObject(uniqueProcessId,
					dataObjectId, instanceId);
		}
	}

	/**
	 * Gets the data object handling implementation.
	 *
	 * @return the data object handling implementation
	 */
	public String getDataObjectHandlingImpl() {
		return dataObjectServiceProviderIdSetting;
	}

	/**
	 * Sets the data object handling implementation.
	 *
	 * @param dataObjectHandlingImpl the new data object handling implementation
	 */
	public void setDataObjectHandlingImpl(String dataObjectHandlingImpl) {
		this.dataObjectServiceProviderIdSetting = dataObjectHandlingImpl;
	}
	
	/**
	 * Sets the data object handling spi.
	 *
	 * @param dataObjectHandlingSPI the new data object handling spi
	 */
	public void setDataObjectHandlingSPI(
			DataObjectSPI dataObjectHandlingSPI) {
		this.dataObjectHandlingSPI = dataObjectHandlingSPI;
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
