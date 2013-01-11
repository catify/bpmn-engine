package com.catify.processengine.core.nodes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.catify.processengine.core.data.dataobjects.DataObjectService;
import com.catify.processengine.core.nodes.eventdefinition.EventDefinition;

/**
 * Abstract class for all events.
 * 
 * @author chris
 * 
 */
public abstract class Event extends FlowElement {

	static final Logger LOG = LoggerFactory.getLogger(Event.class);

	/**
	 * Holds the event definition implementation.
	 * 
	 * @see EventDefinition
	 */
	protected EventDefinition eventDefinition;
	
	protected DataObjectService dataObjectHandling;

	public EventDefinition getEventDefinition() {
		return this.eventDefinition;
	}

	public void setEventDefinition(EventDefinition eventDefinition) {
		this.eventDefinition = eventDefinition;
	}
	
	public DataObjectService getDataObjectService() {
		return dataObjectHandling;
	}

	public void setDataObjectHandling(DataObjectService dataObjectHandling) {
		this.dataObjectHandling = dataObjectHandling;
	}
}
