package com.catify.processengine.core.nodes;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.UntypedActorFactory;

import com.catify.processengine.core.processdefinition.jaxb.TFlowNode;
import com.catify.processengine.core.processdefinition.jaxb.TProcess;
import com.catify.processengine.core.processdefinition.jaxb.TSequenceFlow;
import com.catify.processengine.core.processdefinition.jaxb.TSubProcess;

/**
 * A bridge to the NodeFactory. The bridge implements the UntypedActorFactory to be able to pass (non-final) parameters to it.
 * The create()-method of the UntypedActorFactory then calls the NodeFactory.createServiceNode(..) method to create the
 * correct Actor.
 * 
 * @author christopher köster
 * 
 */
@Configurable
public class ServiceNodeBridge implements UntypedActorFactory {

	private static final long serialVersionUID = 779471794057385901L;

	/** The node factory (which is to be bridged). */
	@Autowired
	private NodeFactory nodeFactory;

	String clientId;
	TProcess processJaxb;
	ArrayList<TSubProcess> subProcessesJaxb;
	TFlowNode flowNodeJaxb;
	List<TSequenceFlow> sequenceFlowsJaxb;
	ActorRef eventDefinitionActor;

	/**
	 * Instantiates a new service node bridge. See class java doc for details.
	 *
	 * @param clientId the client id
	 * @param finalProcessJaxb the process jaxb
	 * @param subProcessesJaxb the sub processes jaxb
	 * @param flowNodeJaxb the flow node jaxb
	 * @param sequenceFlowsJaxb the sequence flows jaxb
	 */
	public ServiceNodeBridge(String clientId,
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
		return nodeFactory.createServiceNode(clientId, processJaxb,
				subProcessesJaxb, flowNodeJaxb,
				sequenceFlowsJaxb);
	}
}