package com.catify.processengine.core.nodes;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import akka.actor.Actor;
import akka.actor.UntypedActorFactory;

import com.catify.processengine.core.processdefinition.jaxb.TFlowNode;
import com.catify.processengine.core.processdefinition.jaxb.TProcess;
import com.catify.processengine.core.processdefinition.jaxb.TSequenceFlow;
import com.catify.processengine.core.processdefinition.jaxb.TSubProcess;

/**
 * A bridge to the {@link TaskNodeFactory}. The bridge implements the UntypedActorFactory to be able to pass (non-final) parameters to it.
 * The create()-method of the UntypedActorFactory then calls the TaskNodeFactory.createServiceNode(..) method to create the
 * correct Actor.
 * 
 * @author christopher köster
 * 
 */
@Configurable
public class TaskNodeBridge implements UntypedActorFactory {

	private static final long serialVersionUID = 779471794057385901L;

	/** The task node factory (which is to be bridged). */
	@Autowired
	private TaskNodeFactory taskNodeFactory;

	private String clientId;
	private TProcess processJaxb;
	private ArrayList<TSubProcess> subProcessesJaxb;
	private TFlowNode flowNodeJaxb;
	private List<TSequenceFlow> sequenceFlowsJaxb;

	/**
	 * Instantiates a new {@link TaskNodeBridge} and calls the {@link TaskNodeFactory}.createServiceNode(..) method. 
	 * See class java docs for details.
	 *
	 * @param clientId the client id
	 * @param finalProcessJaxb the process jaxb
	 * @param subProcessesJaxb the sub processes jaxb
	 * @param flowNodeJaxb the flow node jaxb
	 * @param sequenceFlowsJaxb the sequence flows jaxb
	 */
	public TaskNodeBridge(String clientId,
			TProcess finalProcessJaxb,
			ArrayList<TSubProcess> subProcessesJaxb,
			TFlowNode flowNodeJaxb,
			List<TSequenceFlow> sequenceFlowsJaxb) {
		super();
		this.clientId = clientId;
		this.processJaxb = finalProcessJaxb;
		this.subProcessesJaxb = subProcessesJaxb;
		this.flowNodeJaxb = flowNodeJaxb;
		this.sequenceFlowsJaxb = sequenceFlowsJaxb;
	}

	@Override
	public Actor create() throws Exception {
		return taskNodeFactory.createServiceNode(clientId, processJaxb,
				subProcessesJaxb, flowNodeJaxb,
				sequenceFlowsJaxb);
	}
}