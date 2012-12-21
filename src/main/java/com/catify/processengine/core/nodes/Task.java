package com.catify.processengine.core.nodes;

import com.catify.processengine.core.data.dataobjects.DataObjectService;


public abstract class Task extends Activity {

	protected DataObjectService dataObjectHandling;
	
	public DataObjectService getDataObjectHandling() {
		return dataObjectHandling;
	}
	
	public void setDataObjectHandling(DataObjectService dataObjectHandling) {
		this.dataObjectHandling = dataObjectHandling;
	}

}
