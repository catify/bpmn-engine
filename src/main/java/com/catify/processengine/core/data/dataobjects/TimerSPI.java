package com.catify.processengine.core.data.dataobjects;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TimerSPI} defines all methods that must be implemented to provide
 * a timer store. This store can persistent or in memory (depends on your needs).
 * 
 * 
 * @author claus straube
 *
 */
public abstract class TimerSPI {

	static final Logger LOG = LoggerFactory.getLogger(TimerSPI.class);

	/**
	 * Custom implementation id to figure
	 * out if the chosen implementation is the right one.
	 */
	protected String implementationId;

	/**
	 * Gets the implementation id.
	 *
	 * @return the implementation id
	 */
	public String getImplementationId() {
		return implementationId;
	}
	
	/**
	 * Saves the timer information into the database. Use
	 * a {@link TimerBean} to handover the needed information.
	 * 
	 * @param timer a filled {@link TimerBean}
	 */
	public abstract void saveTimer(TimerBean timer);
	
	/**
	 * Loads all due timers (<= the current time).
	 * 
	 * @param actorRef a akka AktorReference as {@link String}
	 * @return a {@link List} of due {@link TimerBean}s
	 */
	public abstract List<TimerBean> loadDueTimers(String actorRef);
	
	/**
	 * Delete one (or more) timer with given actorref an process
	 * instance id.
	 * 
	 * @param actorRef a akka AktorReference as {@link String}
	 * @param processInstanceId the current process instance id as {@link String}
	 */
	public abstract void deleteTimer(String actorRef, String processInstanceId);
	
}
