package com.catify.processengine.core.data.dataobjects;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock implementation of the {@link DataObjectSPI}.
 * 
 * @author claus straube
 *
 */
public class DataObjectSPIMock extends DataObjectSPI {

	private Map<String, Object> db;
	
	public DataObjectSPIMock() {
		this.db = new HashMap<String, Object>();
	}
	
	@Override
	public void saveObject(String uniqueProcessId, String objectId,
			String instanceId, Object dataObject) {
		this.db.put(this.getId(uniqueProcessId, objectId, instanceId), dataObject);
		LOG.debug(String.format("Saved object with key '%s'.", this.getId(uniqueProcessId, objectId, instanceId)));
	}

	@Override
	public Object loadObject(String uniqueProcessId, String objectId,
			String instanceId) {
		if(this.db.containsKey(this.getId(uniqueProcessId, objectId, instanceId))) {
			LOG.debug(String.format("Loaded object with key '%s'.", this.getId(uniqueProcessId, objectId, instanceId)));
			return this.db.get(this.getId(uniqueProcessId, objectId, instanceId));
		} else {
			LOG.error(String.format("can't load data object for processId --> %s | objectId --> %s | instanceId --> %s", uniqueProcessId, objectId, instanceId));
		}
		return null;
	}

	@Override
	public void deleteObject(String uniqueProcessId, String objectId,
			String instanceId) {
		if(this.db.containsKey(this.getId(uniqueProcessId, objectId, instanceId))) {
			LOG.debug(String.format("Removed object with key '%s'.", this.getId(uniqueProcessId, objectId, instanceId)));
			this.db.remove(this.getId(uniqueProcessId, objectId, instanceId));
		} else {
			LOG.error(String.format("can't remove data object for processId --> %s | objectId --> %s | instanceId --> %s", uniqueProcessId, objectId, instanceId));
		}
	}
	
	private String getId(String uniqueProcessId, String objectId,
			String instanceId) {
		return uniqueProcessId + "#" + objectId + "#" + instanceId;
	}

	public Map<String, Object> getDb() {
		return db;
	}
	
}
