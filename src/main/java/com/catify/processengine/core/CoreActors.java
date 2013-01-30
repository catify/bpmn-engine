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
/**
 * 
 */
package com.catify.processengine.core;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.SmallestMailboxRouter;

import com.catify.processengine.core.services.MetaDataService;
import com.catify.processengine.core.services.ProcessInstanceCleansingService;

/**
 * Core actors are actors not bound to a process (or sub nodes of a process).
 * They resist in the application context and handle operations like writing
 * meta data or archive process instances etc.
 * 
 * @author christopher köster
 * 
 */
public class CoreActors {
	
	/** The actor system. */
	private ActorSystem actorSystem;

	/** The meta data actor writes the meta data provided asynchronously to the graph db. */
	private ActorRef metaDataActor;
	
	/** The process instance cleansing actor will either delete the process instance or archive it. */
	private ActorRef processInstanceCleansingActor;
	
	/**
	 * Instantiates the core actors. (mostly wired by spring)
	 *
	 * @param actorSystem the actor system
	 * @param metaDataActorName the meta data actor name
	 * @param instances the number of instances
	 * @param processInstanceCleansingActorName the process instance cleansing actor name
	 */
	public CoreActors(ActorSystem actorSystem, String metaDataActorName, int instances, String processInstanceCleansingActorName) {
		this.actorSystem = actorSystem;
		this.metaDataActor = this.actorSystem.actorOf(
				new Props(MetaDataService.class).withRouter(
						new SmallestMailboxRouter(instances))
						.withDispatcher("file-mailbox-dispatcher"),
						metaDataActorName);
		this.processInstanceCleansingActor = this.actorSystem.actorOf(
				new Props(ProcessInstanceCleansingService.class).withRouter(
						new SmallestMailboxRouter(instances))
						.withDispatcher("file-mailbox-dispatcher"),
						processInstanceCleansingActorName);
	}

	/**
	 * Gets the actor system.
	 *
	 * @return the actor system
	 */
	public ActorSystem getActorSystem() {
		return actorSystem;
	}

	/**
	 * Sets the actor system.
	 *
	 * @param actorSystem the new actor system
	 */
	public void setActorSystem(ActorSystem actorSystem) {
		this.actorSystem = actorSystem;
	}

	/**
	 * Gets the meta data actor.
	 *
	 * @return the meta data actor
	 */
	public ActorRef getMetaDataActor() {
		return metaDataActor;
	}

	/**
	 * Sets the meta data actor.
	 *
	 * @param metaDataActor the new meta data actor
	 */
	public final void setMetaDataActor(ActorRef metaDataActor) {
		this.metaDataActor = metaDataActor;
	}
	
	/**
	 * Gets the process instance cleansing actor.
	 *
	 * @return the process instance cleansing actor
	 */
	public ActorRef getProcessInstanceCleansingActor() {
		return processInstanceCleansingActor;
	}

	/**
	 * Sets the process instance cleansing actor.
	 *
	 * @param processInstanceCleansingActor the new process instance cleansing actor
	 */
	public void setProcessInstanceCleansingActor(
			ActorRef processInstanceCleansingActor) {
		this.processInstanceCleansingActor = processInstanceCleansingActor;
	}
}
