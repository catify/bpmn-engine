package com.catify.processengine.core.data.dataobjects;

import java.util.List;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TimerSPI} defines all methods that must be implemented to provide
 * a timer store. This store can be persistent or in-memory (depending on your needs).
 * 
 * @author claus straube
 * @author christopher köster
 * 
 */
public abstract class TimerSPI {

	static final Logger LOG = LoggerFactory.getLogger(TimerSPI.class);

	 /** The timer spi loader. */
	private static ServiceLoader<TimerSPI> timerSPILoader = ServiceLoader.load(TimerSPI.class);
	
	 /**
	 * Gets the timer spi implementation.
	 *
	 * @param prefix the prefix used by the implementation
	 * @return the message integration implementation
	 */
	public static TimerSPI getTimerImpl(String implementationId) {
		 
	     for (TimerSPI timerServiceProvider : timerSPILoader) {
	    	 if (timerServiceProvider.getImplementationId().equals(implementationId)) {
				return timerServiceProvider;
	    	 }
	     }
	     // return null if no implementationId could be matched with the found implementations
		LOG.error(String
				.format("The timer service provider '%s' could not be found on the classpath. Timers will fail!",
						implementationId));
		return null;
	 }
	
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
	 * Loads all due timers (<= the current time) and removes them from the database.
	 * 
	 * @param actorRef a akka AktorReference as {@link String}
	 * @return a {@link List} of due {@link TimerBean}s
	 */
	public abstract List<TimerBean> loadDueTimers(String actorRef);
	
	/**
	 * Delete one timer with given actorref an process
	 * instance id.
	 * 
	 * @param actorRef a akka AktorReference as {@link String}
	 * @param processInstanceId the current process instance id as {@link String}
	 */
	public abstract void deleteTimer(String actorRef, String processInstanceId);
}
