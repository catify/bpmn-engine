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
package com.catify.processengine.core.messages;

import scala.concurrent.Future;

/**
 * The CommitMessage is send as a reply to actors which initiate a 
 * (series of) future(s) and are waiting for a reply.
 * @param <T>
 * 
 * @author christopher köster
 * 
 */
public class CommitMessage<T> extends Message {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The self actor ref. */
	private String selfActorRef;
	
	/** The sending actor ref. */
	private String sendingActorRef;
	
	/** The future. */
	private Future<T> future;
	
	
	/**
	 * Instantiates a new commit message.
	 *
	 * @param future the future
	 * @param processInstanceId the process instance id
	 * @param selfActorRef the self actor ref
	 * @param sendingActorRef the sending actor ref
	 */
	public CommitMessage(Future<T> future, String processInstanceId, String selfActorRef, String sendingActorRef) {
//		this.future = future;
		this.processInstanceId = processInstanceId;
		this.selfActorRef = selfActorRef;
		this.sendingActorRef = sendingActorRef;
	}

	/**
	 * Gets the self actor ref.
	 *
	 * @return the self actor ref
	 */
	public String getSelfActorRef() {
		return selfActorRef;
	}

	/**
	 * Sets the self actor ref.
	 *
	 * @param selfActorRef the new self actor ref
	 */
	public void setSelfActorRef(String selfActorRef) {
		this.selfActorRef = selfActorRef;
	}

	/**
	 * Gets the sending actor ref.
	 *
	 * @return the sending actor ref
	 */
	public String getSendingActorRef() {
		return sendingActorRef;
	}

	/**
	 * Sets the sending actor ref.
	 *
	 * @param sendingActorRef the new sending actor ref
	 */
	public void setSendingActorRef(String sendingActorRef) {
		this.sendingActorRef = sendingActorRef;
	}

	/**
	 * Gets the future.
	 *
	 * @return the future
	 */
	public Future<T> getFuture() {
		return future;
	}

	/**
	 * Sets the future.
	 *
	 * @param future the new future
	 */
	public void setFuture(Future<T> future) {
		this.future = future;
	}
	
}
