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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

import scala.concurrent.Future;
import akka.actor.ActorSystem;
import akka.actor.UntypedActor;

import com.catify.processengine.core.messages.ActivationMessage;
import com.catify.processengine.core.messages.CommitMessage;
import com.catify.processengine.core.messages.DeactivationMessage;
import com.catify.processengine.core.messages.TriggerMessage;
import com.catify.processengine.core.nodes.NodeUtils;

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
	
	@Override
	public void onReceive(Object message) throws Exception {
		LOG.debug(String.format("%s received %s", this.getSelf(), message
				.getClass().getSimpleName()));
		
		// process message and reply with a commit message to the underlying node event
		if (message instanceof ActivationMessage) {
			new NodeUtils().replyCommitMessage(activate((ActivationMessage) message), getSelf(), getSender());
		} else if (message instanceof DeactivationMessage) {
			new NodeUtils().replyCommitMessage(deactivate((DeactivationMessage) message), getSelf(), getSender());
		} else if (message instanceof TriggerMessage) {
			new NodeUtils().replyCommitMessage(trigger((TriggerMessage) message), getSelf(), getSender());
		} else {
			unhandled(message);
		}
	}


	/**
	 * Encapsulating event received an activation message, take steps needed in
	 * the implemented event definition.
	 * 
	 * @param message
	 *            the triggering message ({@link ActivationMessage})
	 */
	protected abstract CommitMessage<?> activate(ActivationMessage message);

	/**
	 * Encapsulating event received a deactivation message, take steps needed in
	 * the implemented event definition.
	 * 
	 * @param message
	 *            the triggering message ({@link DeactivationMessage})
	 */
	protected abstract CommitMessage<?> deactivate(DeactivationMessage message);

	/**
	 * Encapsulating event received a fire message, take steps needed in the
	 * implemented event definition.
	 * 
	 * @param message
	 *            the triggering message ({@link TriggerMessage})
	 */
	protected abstract CommitMessage<?> trigger(TriggerMessage message);

	/**
	 * Create a commit message with a given future.
	 * @param <T>
	 *
	 * @param future the future
	 * @param processInstanceId the process instance id
	 * @return the commit message
	 */
	protected <T> CommitMessage<T> createCommitMessage(Future<T> future, String processInstanceId) {
		return new NodeUtils().createCommitMessage(future, processInstanceId, this.getSelf(), this.getSender());
	}
	
	/**
	 * Create a commit message with a 'successful' future.
	 *
	 * @param processInstanceId the process instance id
	 * @param future the future
	 * @return the commit message
	 */
	protected CommitMessage<?> createSuccessfullCommitMessage(String processInstanceId) {
		return new NodeUtils().createSuccessfulCommitMessage(processInstanceId, this.getSelf(), this.getSender());
	}
}
