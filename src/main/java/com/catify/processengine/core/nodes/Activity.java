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
package com.catify.processengine.core.nodes;

import java.util.List;

import akka.actor.ActorRef;

/**
 * The abstract class Activity is a base class for all bpmn activities.
 * 
 * @author christopher köster
 * 
 */
public abstract class Activity extends FlowElement {
	
	/** The boundary event connected to this activity. */
	protected List<ActorRef> boundaryEvents;

	public List<ActorRef> getBoundaryEvents() {
		return boundaryEvents;
	}

	public void setBoundaryEvents(List<ActorRef> boundaryEvents) {
		this.boundaryEvents = boundaryEvents;
	}
	
}
