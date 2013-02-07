/**
 * 
 */
package com.catify.processengine.core.nodes.eventdefinition;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;

import com.catify.processengine.core.messages.ActivationMessage;
import com.catify.processengine.core.messages.CommitMessage;
import com.catify.processengine.core.messages.DeactivationMessage;
import com.catify.processengine.core.messages.LinkEventMessage;
import com.catify.processengine.core.messages.TriggerMessage;

/**
 * Event definition for BPMN 2.0 link events (catching and throwing).
 * 
 * @author claus straube
 *
 */
public class LinkEventDefinition extends EventDefinition {

	static final Logger LOG = LoggerFactory.getLogger(LinkEventDefinition.class);
	
	private boolean isThrow;
	private List<String> sources = new ArrayList<String>();
	private String target;
	private String uniqueProcessId;
	private ActorRef eventActorRef;
	
	/**
	 * Constructor for a catching link event definition.
	 * 
	 * @param sources
	 * @param uniqueProcessId
	 * @param eventActorRef
	 */
	public LinkEventDefinition(List<String> sources, String uniqueProcessId, ActorRef eventActorRef) {
		this.isThrow = Boolean.FALSE;
		this.sources = sources;
		this.uniqueProcessId = uniqueProcessId;
		this.eventActorRef = eventActorRef;
	}
	
	/**
	 * Constructor for a throwing link event definition.
	 * 
	 * @param target
	 * @param uniqueProcessId
	 * @param eventActorRef
	 */
	public LinkEventDefinition(String target, String uniqueProcessId, ActorRef eventActorRef) {
		this.isThrow = Boolean.TRUE;
		this.target = target;
		this.uniqueProcessId = uniqueProcessId;
		this.eventActorRef = eventActorRef;
	}
	
	@PostConstruct
	void init() {
		// register to event bus
		actorSystem.eventStream().subscribe(this.getSelf(), LinkEventMessage.class);
	}
	
	/* (non-Javadoc)
	 * @see com.catify.processengine.core.nodes.eventdefinition.EventDefinition#activate(com.catify.processengine.core.messages.ActivationMessage)
	 */
	@Override
	protected CommitMessage<?> activate(ActivationMessage message) {
		if(this.isThrow) {
			LOG.debug(String.format("Received activate message. Sending now link '%s'.", target));
			actorSystem.eventStream().publish(new LinkEventMessage(target, uniqueProcessId, message.getProcessInstanceId()));
		}
		return createSuccessfullCommitMessage(message.getProcessInstanceId());
	}

	/* (non-Javadoc)
	 * @see com.catify.processengine.core.nodes.eventdefinition.EventDefinition#deactivate(com.catify.processengine.core.messages.DeactivationMessage)
	 */
	@Override
	protected CommitMessage<?> deactivate(DeactivationMessage message) {
		return createSuccessfullCommitMessage(message.getProcessInstanceId());
	}

	/* (non-Javadoc)
	 * @see com.catify.processengine.core.nodes.eventdefinition.EventDefinition#trigger(com.catify.processengine.core.messages.TriggerMessage)
	 */
	@Override
	protected CommitMessage<?> trigger(TriggerMessage message) {
		// the event node does the work
		return createSuccessfullCommitMessage(message.getProcessInstanceId());
	}
	
	/**
	 * Mailbox for all {@link LinkEventMessage}s.
	 * 
	 * @param message
	 */
	protected void listen(LinkEventMessage message) {
		/*
		 * if the message fits to the event node (same process and
		 * correct identifier), the activate catching event node
		 * and trigger it.
		 */
		if(this.sources.contains(message.getTarget()) && message.getUniqueProcessId().equals(this.uniqueProcessId)) {
			LOG.debug("Activate and trigger link event node.");
			String iid = message.getProcessInstanceId();
			eventActorRef.tell(new ActivationMessage(iid), getSender());
			eventActorRef.tell(new TriggerMessage(iid, null), getSender());
		}
	}
	
	@Override
	public void onReceive(Object message) throws Exception {
		LOG.debug(String.format("%s received %s", this.getSelf(), message
				.getClass().getSimpleName()));
		
		// process message and reply with a commit message to the underlying node event
		if (this.handle(message)) {
			// everything is done
		} else if (message instanceof LinkEventMessage) {
			this.listen((LinkEventMessage) message);
		} else {
			LOG.warn("Unhandled message received: " + message.getClass());
			unhandled(message);
		}
	}

}
