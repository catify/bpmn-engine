/**
 * 
 */
package com.catify.processengine.core.data.dataobjects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The NoDataObjectHandling class is bound to nodes that do not have a data
 * association (such as a data object). This class is used mainly to avoid the
 * need of every node to check if there is data bound to it and for debugging
 * purposes.
 * 
 * @author chris
 * 
 */
public class NoDataObjectHandling extends DataObjectService {

	/** The Constant LOG. */
	static final Logger LOG = LoggerFactory.getLogger(NoDataObjectHandling.class);
	
	/**
	 * Instantiates a new no data object handling.
	 */
	public NoDataObjectHandling() {
	}

	/* (non-Javadoc)
	 * @see com.catify.processengine.core.data.dataobjects.DataObjectService#saveObject(java.lang.String, java.lang.String, java.lang.Object)
	 */
	@Override
	public void saveObject(String uniqueProcessId, String instanceId,
			Object payloadObject) {
		LOG.debug(String
				.format("%s triggered saveObject method, but no DataObjectHandlingSPI set because no Data Object was associated in process definition.",
						this.getClass().getSimpleName()));
	}

	/* (non-Javadoc)
	 * @see com.catify.processengine.core.data.dataobjects.DataObjectService#loadObject(java.lang.String, java.lang.String)
	 */
	@Override
	public Object loadObject(String uniqueProcessId, String instanceId) {
		LOG.debug(String
				.format("%s triggered loadObject method, but no DataObjectHandlingSPI set because no Data Object was associated in process definition",
						this.getClass().getSimpleName()));

		return null;
	}

}
