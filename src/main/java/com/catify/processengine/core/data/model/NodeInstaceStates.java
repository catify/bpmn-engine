/**
 * *******************************************************
 * Copyright (C) 2013 catify <info@catify.com>
 * *******************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.catify.processengine.core.data.model;

/**
 * The Class NodeInstaceStates is a utility class which holds the known node instance states.
 * 
 * @author christopher köster
 * 
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
