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
