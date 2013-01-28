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
package com.catify.processengine.core.nodes.eventdefinition;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

import scala.concurrent.duration.Duration;

import akka.actor.ActorSystem;
import akka.actor.UntypedActor;
import akka.util.Timeout;

import com.catify.processengine.core.messages.ActivationMessage;
import com.catify.processengine.core.messages.DeactivationMessage;
import com.catify.processengine.core.messages.Message;
import com.catify.processengine.core.messages.TriggerMessage;

/**
 * Interface for all event definitions. Classes inherited from Event.class will
 * encapsulate an event definition implementation and execute the provided
 * methods. New event definitions therefore only need to implement the abstract
 * methods activate(), deactivate() and fire() in order to be valid. The calling
 * event does not (need to) know the actually used event definition so new ones
 * can be plugged without altering any code in the encapsulating events. See GoF
 * 'strategy pattern'. <br>
 * 
 * @author chris
 * 
 */
@Configurable
public abstract class EventDefinition extends UntypedActor {

	static final Logger LOG = LoggerFactory.getLogger(EventDefinition.class);
	
	@Autowired
	protected ActorSystem actorSystem;
	
	/** The timeout in seconds. Note: This value is only available after construction is completed. */
	@Value("${core.eventDefinitionTimeout}")
	protected long timeoutInSeconds;
	
	/** The default timeout for a synchronous call to an EventDefinition. Note: This value is only available after construction is completed. */
	protected Timeout eventDefinitionTimeout;
	
	@Override
	public void onReceive(Object message) throws Exception {
		LOG.debug(String.format("%s received %s", this.getSelf(), message
				.getClass().getSimpleName()));
		
			if (message instanceof ActivationMessage) {
				activate((ActivationMessage) message);
			} else if (message instanceof DeactivationMessage) {
				deactivate((DeactivationMessage) message);
			} else if (message instanceof TriggerMessage) {
				trigger((TriggerMessage) message);
			} else {
				unhandled(message);
			}
			
		// reply commit message to calling node after method execution
		this.replyCommit((Message) message);
	}
	
	@PostConstruct
	void initSpringDependentProperties() {
		eventDefinitionTimeout = new Timeout(Duration.create(timeoutInSeconds, "seconds"));
	}

	/**
	 * Encapsulating event received an activation message, take steps needed in
	 * the implemented event definition.
	 * 
	 * @param message
	 *            the triggering message ({@link ActivationMessage})
	 */
	protected abstract void activate(ActivationMessage message);

	/**
	 * Encapsulating event received a deactivation message, take steps needed in
	 * the implemented event definition.
	 * 
	 * @param message
	 *            the triggering message ({@link DeactivationMessage})
	 */
	protected abstract void deactivate(DeactivationMessage message);

	/**
	 * Encapsulating event received a fire message, take steps needed in the
	 * implemented event definition.
	 * 
	 * @param message
	 *            the triggering message ({@link TriggerMessage})
	 */
	protected abstract void trigger(TriggerMessage message);
	
	/**
	 * Reply a commit to the calling service node. This method must be called 
	 * when the EventDefinition actor completes, because EventDefinitions calls
	 * are blocking. 
	 *
	 * @param message the message
	 */
	protected void replyCommit(Message message) {
		this.getSender().tell(message, this.getSelf());
	}

}
