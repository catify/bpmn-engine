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

import static akka.dispatch.Futures.sequence;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import scala.concurrent.Await;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.dispatch.Futures;
import akka.dispatch.OnFailure;
import akka.dispatch.OnSuccess;
import akka.pattern.Patterns;
import akka.util.Timeout;

import com.catify.processengine.core.messages.CommitMessage;
import com.catify.processengine.core.messages.DeactivationMessage;

/**
 * Util class for common operations like sending deactivation messages to other nodes
 * or to create commit messages used between Event and EvenDefinition.
 * 
 * @author christopher köster
 * 
 */
@Configurable
public class NodeUtils {
	
	static final Logger LOG = LoggerFactory.getLogger(NodeUtils.class);
	
	@Autowired
	protected ActorSystem actorSystem;
	
	/**
	 * Create a commit message with a given future.
	 * @param <T>
	 *
	 * @param future the future
	 * @param processInstanceId the process instance id
	 * @param self the self actor reference
	 * @param sender the sender actor reference
	 * @return the commit message
	 */
	public <T> CommitMessage<T> createCommitMessage(Future<T> future, String processInstanceId, ActorRef self, ActorRef sender) {
		return new CommitMessage<T>(future, processInstanceId, self.toString(), sender.toString());
	}
	
	/**
	 * Create a commit message with a 'successful' future. The future will be generated.
	 *
	 * @param processInstanceId the process instance id
	 * @param self the self actor reference
	 * @param sender the sender actor reference
	 * @return the commit message
	 */
	public CommitMessage<String> createSuccessfulCommitMessage(String processInstanceId, ActorRef self, ActorRef sender) {
		return new CommitMessage<String>(Futures.successful("Successful commit"), processInstanceId, self.toString(), sender.toString());
	}
	
	/**
	 * Create a commit message with a 'successful' future and a payload. The future will be generated.
	 *
	 * @param processInstanceId the process instance id
	 * @param self the self actor reference
	 * @param sender the sender actor reference
	 * @param payload the payload
	 * @return the commit message
	 */
	public CommitMessage<?> createSuccessfulCommitMessage(String processInstanceId, ActorRef self,ActorRef sender, Object payload) {
		return new CommitMessage<String>(Futures.successful("Successful commit"), processInstanceId, self.toString(), sender.toString(), payload);
	}
	
	/**
	 * Create a commit message with a 'failed' future and the given failure.
	 *
	 * @param failure the failure
	 * @param processInstanceId the process instance id
	 * @param self the self actor reference
	 * @param sender the sender actor reference
	 * @return the commit message
	 */
	public CommitMessage<?> createFailedCommitMessage(Throwable failure, String processInstanceId, ActorRef self, ActorRef sender) {
		return new CommitMessage<Object>(Futures.failed(failure), processInstanceId, self.toString(), sender.toString());
	}
	
	/**
	 * Reply commit message.
	 *
	 * @param commitMessage the commit message
	 * @param self the self actor reference
	 * @param sender the sender actor reference to reply to
	 */
	public void replyCommitMessage(CommitMessage<?> commitMessage, ActorRef self, ActorRef sender) {
		sender.tell(commitMessage, self);
	}
	
	/**
	 * Reply a CommitMessage with a 'successful' future. The future will be generated.
	 *
	 * @param processInstanceId the process instance id
	 * @param self the self actor reference
	 * @param sender the sender actor reference to reply to
	 */
	public void replySuccessfulCommit(String processInstanceId, ActorRef self, ActorRef sender) {
		this.replyCommitMessage(this.createSuccessfulCommitMessage(
				processInstanceId, self, sender), self, sender);
	}
	
	/**
	 * Deactivate nodes via DeactivationMessages (blocking operation). 
	 *
	 * @param actorReferences the actor references to deactivate
	 * @param processInstanceId the process instance id
	 * @param deactivationTimeout the deactivation timeout
	 * @param sender the sender actor reference to reply to
	 * @param self the self actor reference
	 * @return the future sequence
	 */
	public Future<Iterable<Object>> deactivateNodes(Iterable<ActorRef> actorReferences, String processInstanceId, Timeout deactivationTimeout, ActorRef sender, ActorRef self) {
		
		final ExecutionContext ec = actorSystem.dispatcher();
		
		Future<Iterable<Object>> futureSequence = sendDeactivationMessageToActors(
				actorReferences, processInstanceId, deactivationTimeout);
 
        // block until all futures came back
		try {
			Await.result(futureSequence, deactivationTimeout.duration());
			handleDeactivationSuccess(deactivationTimeout, ec, futureSequence, self);
			return futureSequence;
		} catch (Exception e) {
			handleDeactivationSuccess(deactivationTimeout, ec, futureSequence, self);
			return futureSequence;
		}
	}

	/**
	 * Send deactivation messages to actors.
	 *
	 * @param actorReferences the actor references
	 * @param processInstanceId the process instance id
	 * @param deactivationTimeout the deactivation timeout
	 * @return the future
	 */
	private Future<Iterable<Object>> sendDeactivationMessageToActors(
			Iterable<ActorRef> actorReferences, String processInstanceId,
			Timeout deactivationTimeout) {
		final ExecutionContext ec = actorSystem.dispatcher();

		List<Future<Object>> listOfFutureActorRefs = new ArrayList<Future<Object>>();
		
		for (ActorRef actor : actorReferences) {
			try {
				// make an asynchronous request ('Patterns.ask') to the event definition actor 
				listOfFutureActorRefs.add(Patterns.ask(actor, new DeactivationMessage(processInstanceId), deactivationTimeout));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// compose the sequence of the received commit futures
        Future<Iterable<Object>> futuresSequence = sequence(listOfFutureActorRefs, ec);
		return futuresSequence;
	}
	
	/**
	 * Handle deactivation success and print appropriate error logs.
	 *
	 * @param deactivationTimeout the deactivation timeout
	 * @param ec the ExecutionContext
	 * @param futureSequence the future sequence
	 * @param self the self actor reference
	 */
	private void handleDeactivationSuccess(final Timeout deactivationTimeout,
			final ExecutionContext ec,
			final Future<Iterable<Object>> futureSequence, final ActorRef self) {

		futureSequence.onSuccess(new OnSuccess<Iterable<Object>>() {
			  public void onSuccess(Iterable<Object> result) {
			    LOG.debug(String.format("Successfully deactivated other nodes - Deactivation initiated by %s.", self));
			  }
			}, ec);
		
		futureSequence.onFailure(new OnFailure() {
			  public void onFailure(Throwable failure) {
			    if (failure instanceof TimeoutException) {
			    	LOG.error(String.format("Timeout while waiting for deactivation commits - Deactivation initiated by %s. Timeout was set to %s", 
			    			self, deactivationTimeout.duration()));
			    } else {
			    	failure.printStackTrace();
			    }
			  }
			}, ec);
	}
}
