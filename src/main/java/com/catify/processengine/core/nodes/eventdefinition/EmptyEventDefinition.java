/**
 * 
 */
package com.catify.processengine.core.nodes.eventdefinition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.catify.processengine.core.messages.ActivationMessage;
import com.catify.processengine.core.messages.DeactivationMessage;
import com.catify.processengine.core.messages.TriggerMessage;

/**
 * The Class EmptyEventDefinition is for Events that do not define an EventDefinition, which is valid in bpmn.
 *
 * @author chris
 */
public class EmptyEventDefinition implements EventDefinition {

	static final Logger LOG = LoggerFactory.getLogger(EmptyEventDefinition.class);
	
	/* (non-Javadoc)
	 * @see com.catify.processengine.core.nodes.eventdefinition.EventDefinition#acitivate(com.catify.processengine.core.messages.ActivationMessage)
	 */
	@Override
	public void acitivate(ActivationMessage message) {
		LOG.debug(String.format("%s received %s", this.getClass().getSimpleName(), message
				.getClass().getSimpleName()));
	}

	/* (non-Javadoc)
	 * @see com.catify.processengine.core.nodes.eventdefinition.EventDefinition#deactivate(com.catify.processengine.core.messages.DeactivationMessage)
	 */
	@Override
	public void deactivate(DeactivationMessage message) {
		LOG.debug(String.format("%s received %s", this.getClass().getSimpleName(), message
				.getClass().getSimpleName()));
	}

	/* (non-Javadoc)
	 * @see com.catify.processengine.core.nodes.eventdefinition.EventDefinition#trigger(com.catify.processengine.core.messages.TriggerMessage)
	 */
	@Override
	public void trigger(TriggerMessage message) {
		LOG.debug(String.format("%s received %s", this.getClass().getSimpleName(), message
				.getClass().getSimpleName()));
	}

}
