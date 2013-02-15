package com.catify.processengine.core.nodes.loops;

import akka.actor.ActorRef;

import com.catify.processengine.core.data.dataobjects.DataObjectHandling;
import com.catify.processengine.core.messages.ActivationMessage;
import com.catify.processengine.core.messages.DeactivationMessage;
import com.catify.processengine.core.messages.TriggerMessage;

/**
 * The Class NonLoop implements the basic task behavior when no looping is specified in the bpmn process.
 */
public class NonLoop implements LoopTypeStrategy {

	/**
	 * Instantiates a new non loop strategy implementation.
	 *
	 * @param uniqueProcessId the unique process id
	 * @param taskWrapper the task wrapper
	 * @param taskAction the task action
	 * @param dataObjectHandling the data object handling
	 */
	public NonLoop(String uniqueProcessId, ActorRef taskWrapper,
			ActorRef taskAction, DataObjectHandling dataObjectHandling) {
		super();
		this.uniqueProcessId = uniqueProcessId;
		this.taskWrapper = taskWrapper;
		this.taskAction = taskAction;
		this.dataObjectHandling = dataObjectHandling;
	}

	/** The unique process id. 
	 * @see com.catify.processengine.core.data.model.entities.ProcessNode#uniqueProcessId */
	protected String uniqueProcessId;
	
	/** The task wrapper actor reference to the loop type strategy
	 * which binds the {@link LoopTypeStrategy}) implementation. */
	protected ActorRef taskWrapper;
	
	/** The task action actor reference to the task that implements
	 * the bpmn task behavior (service task, receive task etc.). */
	protected ActorRef taskAction;

	/** The data object handling wraps the {@link DataObjectHandling}. */
	protected DataObjectHandling dataObjectHandling;

	/* (non-Javadoc)
	 * @see com.catify.processengine.core.nodes.loops.LoopTypeStrategy#activate(com.catify.processengine.core.messages.ActivationMessage)
	 */
	@Override
	public void activate(ActivationMessage message) {
		message.setPayload(this.getDataObjectHandling().loadObject(this.getUniqueProcessId(), message.getProcessInstanceId()));
			
		taskAction.tell(message, taskWrapper);
	}

	/* (non-Javadoc)
	 * @see com.catify.processengine.core.nodes.loops.LoopTypeStrategy#deactivate(com.catify.processengine.core.messages.DeactivationMessage)
	 */
	@Override
	public void deactivate(DeactivationMessage message) {
		taskAction.tell(message, taskWrapper);
	}

	/* (non-Javadoc)
	 * @see com.catify.processengine.core.nodes.loops.LoopTypeStrategy#trigger(com.catify.processengine.core.messages.TriggerMessage)
	 */
	@Override
	public void trigger(TriggerMessage message) {
		this.getDataObjectHandling().saveObject(this.getUniqueProcessId(), message.getProcessInstanceId(), message.getPayload());
		
		taskAction.tell(message, taskWrapper);
	}

	/**
	 * Gets the unique process id.
	 *
	 * @return the unique process id
	 */
	protected String getUniqueProcessId() {
		return uniqueProcessId;
	}

	/**
	 * Sets the unique process id.
	 *
	 * @param uniqueProcessId the new unique process id
	 */
	protected void setUniqueProcessId(String uniqueProcessId) {
		this.uniqueProcessId = uniqueProcessId;
	}

	/**
	 * Sets the data object handling.
	 *
	 * @param dataObjectHandling the new data object handling
	 */
	protected void setDataObjectHandling(DataObjectHandling dataObjectHandling) {
		this.dataObjectHandling = dataObjectHandling;
	}
	
	
	/**
	 * Gets the task wrapper.
	 *
	 * @return the task wrapper
	 */
	public ActorRef getTaskWrapper() {
		return taskWrapper;
	}

	/**
	 * Sets the task wrapper.
	 *
	 * @param taskWrapper the new task wrapper
	 */
	public void setTaskWrapper(ActorRef taskWrapper) {
		this.taskWrapper = taskWrapper;
	}

	/**
	 * Gets the task action.
	 *
	 * @return the task action
	 */
	public ActorRef getTaskAction() {
		return taskAction;
	}

	/**
	 * Sets the task action.
	 *
	 * @param taskAction the new task action
	 */
	public void setTaskAction(ActorRef taskAction) {
		this.taskAction = taskAction;
	}

	/**
	 * Gets the data object handling.
	 *
	 * @return the data object handling
	 */
	public DataObjectHandling getDataObjectHandling() {
		return dataObjectHandling;
	}


}
