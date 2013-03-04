package com.catify.processengine.core.nodes.loops;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.catify.processengine.core.data.dataobjects.DataObjectHandling;

/**
 * The Class NonLoop implements the basic task behavior when no looping is specified in the bpmn process.
 */
public class LoopBehaviorService {

	public static final Logger LOG = LoggerFactory
			.getLogger(LoopBehaviorService.class);


	/**
	 * Load payload from associated data object (if any).
	 *
	 * @param processInstanceId the process instance id
	 * @param uniqueProcessId the unique process id
	 * @param dataObjectHandling the data object handling
	 * @return the object
	 */
	public static Object loadPayloadFromDataObject(String processInstanceId, String uniqueProcessId, DataObjectHandling dataObjectHandling) {
		return dataObjectHandling.loadObject(uniqueProcessId, processInstanceId);
	}
	
	/**
	 * Save payload to associated data object (if any).
	 *
	 * @param processInstanceId the process instance id
	 * @param payload the payload
	 * @param uniqueProcessId the unique process id
	 * @param dataObjectHandling the data object handling
	 */
	public static void savePayloadToDataObject(String processInstanceId, Object payload, String uniqueProcessId, DataObjectHandling dataObjectHandling) {
		dataObjectHandling.saveObject(uniqueProcessId, processInstanceId, payload);
	}

}
