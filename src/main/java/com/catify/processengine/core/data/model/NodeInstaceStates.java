package com.catify.processengine.core.data.model;

/**
 * The Class NodeInstaceStates is a utility class which holds the known node instance states.
 */
public class NodeInstaceStates {

	private NodeInstaceStates() {
	}
	
	/** The ACTIVE_STATE marks nodes that are currently active (eg. a catch event waiting to be triggered). */
	public static final String ACTIVE_STATE = "ACTIVE";

	/** The PASSED_STATE marks nodes that have successfully been completed. */
	public static final String PASSED_STATE = "PASSED";

	/** The DEACTIVATED_STATE marks nodes that have been in {@link ACTIVE_STATE} but then have been deactivated by other nodes or services. */
	public static final String DEACTIVATED_STATE = "DEACTIVATED";

	/** The INACTIVE_STATE marks nodes that have been instantiated but not called by any node or service. */
	public static final String INACTIVE_STATE = "INACTIVE";

}
