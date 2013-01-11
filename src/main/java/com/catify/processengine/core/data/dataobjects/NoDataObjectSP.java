/**
 * 
 */
package com.catify.processengine.core.data.dataobjects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The NoDataObjectSP class is used for node services that do not have a data
 * association (such as a data object). This class is used mainly to avoid the
 * need of every node to check if there is data bound to it and for debugging
 * purposes.
 * 
 * @author chris
 * 
 */
public class NoDataObjectSP extends DataObjectSPI {

	/** The Constant LOG. */
	static final Logger LOG = LoggerFactory.getLogger(NoDataObjectSP.class);

	@Override
	public void saveObject(String uniqueProcessId, String objectId,
			String instanceId, Object dataObject) {
		LOG.trace("Dummy saveObject method triggered. This method was called because the calling node service" +
				"had no data object association in its process definition.");
	}

	@Override
	public Object loadObject(String uniqueProcessId, String objectId,
			String instanceId) {
		LOG.trace("Dummy loadObject method triggered. This method was called because the calling node service" +
				"had no data object association in its process definition.");
		return null;
	}

	@Override
	public void deleteObject(String uniqueProcessId, String objectId,
			String instanceId) {
		LOG.trace("Dummy deleteObject method triggered. This method was called because the calling node service" +
				"had no data object association in its process definition.");
	}

}
