package com.catify.processengine.core.nodes.eventdefinition;

import com.catify.processengine.core.messages.ActivationMessage;
import com.catify.processengine.core.messages.DeactivationMessage;
import com.catify.processengine.core.messages.TriggerMessage;

/**
 * Interface for all synchronous event definitions. Classes inherited from Event.class will
 * encapsulate an event definition implementation and execute the provided
 * methods. New event definitions therefore only need to implement the abstract
 * methods activate(), deactivate() and fire() in order to be valid. The calling
 * event does not (need to) know the actually used event definition so new ones
 * can be plugged without altering any code in the encapsulating events. See GoF
 * 'strategy pattern'.
 * 
 * @author chris
 * 
 */
public interface SynchronousEventDefinition {

	/**
	 * Encapsulating 'event received an activation message'.
	 *
	 * @param message the triggering message ({@link ActivationMessage})
	 * @return the object returned by the synchronous request
	 */
	Object acitivate(ActivationMessage message);

	/**
	 * Encapsulating 'event received a deactivation message'.
	 * 
	 * @param message
	 *            the triggering message ({@link DeactivationMessage})
	 */
	void deactivate(DeactivationMessage message);

	/**
	 * Encapsulating 'event received a fire message'..
	 * 
	 * @param message
	 *            the triggering message ({@link TriggerMessage})
	 */
	void trigger(TriggerMessage message);

}
