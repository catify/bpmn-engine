/**
 * 
 */
package com.catify.processengine.core.services;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

/**
 * The ActorReferenceService creates actor references based on the uniqueFlowNodeId (cleaned of any characters not allowed by akka). 
 * 
 * @author chris
 * 
 */
@Configurable
public class ActorReferenceService {

	public static final Logger LOG = LoggerFactory
			.getLogger(ActorReferenceService.class);
	
	@Autowired
	private ActorSystem actorSystem;

	/**
	 * Get an actor reference from the unique flow node id.
	 *
	 * @param uniqueFlowNodeId the unique flow node id
	 * @return the actor reference
	 */
	public ActorRef getActorReference(String uniqueFlowNodeId) {
		// note: the "user/" prefix is needed for all user created ActorRefs
		return this.actorSystem
				.actorFor("user/"
						+ getActorReferenceString(uniqueFlowNodeId));
	}

	/**
	 * Get an actor reference string from the unique flow node id.
	 *
	 * @param uniqueFlowNodeId the unique flow node id
	 * @return the actor reference as string
	 */
	public static String getActorReferenceString(String uniqueFlowNodeId) {
			// replace invalid characters and return a valid actor reference string
			return getAkkaComplientString(uniqueFlowNodeId);
	}
	
	/**
	 * Get an akka compliant actor reference string from any input string.
	 * This method is used to eliminate all characters from a string that are forbidden to exist in an akka actor reference.
	 *
	 * @param unfilteredString the unfiltered string
	 * @return the actor reference as string
	 */
	public static String getAkkaComplientString(String unfilteredString) {
			// replace invalid characters and return a valid actor reference string
			return Pattern.compile("[^-\\w:@&=+,.!~*'_;]")
					.matcher(unfilteredString).replaceAll("_");
	}
}


