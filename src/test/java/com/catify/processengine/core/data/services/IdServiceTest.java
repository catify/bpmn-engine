package com.catify.processengine.core.data.services;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.catify.processengine.core.nodes.eventdefinition.EventDefinitionParameter;
import com.catify.processengine.core.processdefinition.jaxb.ObjectFactory;
import com.catify.processengine.core.processdefinition.jaxb.TExtensionElements;
import com.catify.processengine.core.processdefinition.jaxb.TFlowElement;
import com.catify.processengine.core.processdefinition.jaxb.TFlowNode;
import com.catify.processengine.core.processdefinition.jaxb.TIntermediateCatchEvent;
import com.catify.processengine.core.processdefinition.jaxb.TProcess;
import com.catify.processengine.core.processdefinition.jaxb.TSubProcess;
import com.catify.processengine.core.processdefinition.jaxb.TVersion;

public class IdServiceTest {

	private static final String PID = "4711";
	private static final String PNAME = "FOO";
	private static final String PVERSION = "1.2.1";
	private static final String CLIENT = "BAR";
	private static final String NID = "0815";
	private static final String NNAME = "NODE1";
	private static final String SUBP = "4712P014713P02";
	
	private static final String UPID = "BARFOO47111.2.1";
	private static final String UNID = "BARFOO47111.2.14712P014713P020815NODE1";

	@Test
	public void testGetUniqueProcessIdIdParams() {
		IdParams p1 = new IdParams(CLIENT, this.createProcess());
		assertEquals(UPID, IdService.getUniqueProcessId(p1));
	}

	@Test
	public void testGetUniqueProcessIdStringTProcess() {
		assertEquals(UPID, IdService.getUniqueProcessId(CLIENT, this.createProcess()));
	}

	@Test
	public void testGetUniqueProcessIdEventDefinitionParameter() {
		EventDefinitionParameter params = new EventDefinitionParameter(CLIENT, this.createProcess(), null, null);
		assertEquals(UPID, IdService.getUniqueProcessId(params));
	}

	@Test
	public void testGetArchivedUniqueProcessId() {
		assertEquals(IdService.ARCHIVEPREFIX + UPID, IdService.getArchivedUniqueProcessId(CLIENT, this.createProcess()));
	}

	@Test
	public void testGetUniqueFlowNodeIdStringTProcessArrayListOfTSubProcessTFlowNode() {
		assertEquals(UNID, IdService.getUniqueFlowNodeId(CLIENT, this.createProcessWithFlowNode(), this.createSubProcesses(), NID));
	}

	@Test
	public void testGetUniqueFlowNodeIdEventDefinitionParameter() {
		EventDefinitionParameter params = new EventDefinitionParameter(CLIENT, this.createProcess(), this.createSubProcesses(), this.createFlowNode());
		assertEquals(UNID, IdService.getUniqueFlowNodeId(params));
	}

	@Test
	public void testGetArchivedUniqueFlowNodeIdStringTProcessArrayListOfTSubProcessTFlowNode() {
		assertEquals(IdService.ARCHIVEPREFIX + UNID, IdService.getArchivedUniqueFlowNodeId(CLIENT, this.createProcess(), this.createSubProcesses(), this.createFlowNode()));
	}

	@Test
	public void testGetUniqueFlowNodeIdStringTProcessString() {
		// TODO --> Chris
		fail("chris will implement");
//		TProcess process = this.createProcessWithFlowNode();
//		this.addSubProcesses(process);
//		assertEquals(UNID, IdService.getUniqueFlowNodeId(CLIENT, process, NID));
	}

	@Test
	public void testGetUniqueFlowNodeIdStringTProcessArrayListOfTSubProcessString() {
		assertEquals(UNID, IdService.getUniqueFlowNodeId(CLIENT, this.createProcessWithFlowNode(), this.createSubProcesses(), NID));
	}

	@Test
	public void testGetArchivedUniqueFlowNodeIdStringTProcessString() {
		fail("chris will implement");
//		assertEquals(IdService.ARCHIVEPREFIX + UNID, IdService.getArchivedUniqueFlowNodeId(CLIENT, this.createProcessWithFlowNode(), NID));
	}

	@Test
	public void testGetArchivedUniqueFlowNodeIdStringTProcessArrayListOfTSubProcessString() {
		assertEquals(IdService.ARCHIVEPREFIX + UNID, IdService.getArchivedUniqueFlowNodeId(CLIENT, this.createProcessWithFlowNode(), this.createSubProcesses(), NID));
	}
	
	private TProcess createProcess(){
		return this.createProcess(PID, PNAME, PVERSION);
	}
	
	private TProcess createProcessWithFlowNode() {
		TProcess process = this.createProcess();
		this.addFlowNode(NID, NNAME, process);
		return process;
	}
	
	private TProcess createProcess(String id, String name, String version) {
		ObjectFactory factory = new ObjectFactory();
		TProcess process = factory.createTProcess();
		process.setId(id);
		process.setName(name);

		process.setExtensionElements(new TExtensionElements());
		List<Object> elements = process.getExtensionElements().getAny();

		TVersion tVersion = factory.createTVersion();
		tVersion.setVersion(version);

		elements.add(factory.createVersion(tVersion));

		return process;
	}
	
	private TFlowNode createFlowNode(String id, String name) {
		ObjectFactory factory = new ObjectFactory();
		TIntermediateCatchEvent tNode = factory.createTIntermediateCatchEvent();
		tNode.setId(id);
		tNode.setName(name);
		return tNode;
	}
	
	private TFlowNode createFlowNode() {
		return this.createFlowNode(NID, NNAME);
	}
	
	private void addFlowNode(String id, String name, TProcess process) {
		ObjectFactory factory = new ObjectFactory();		
		JAXBElement<TFlowNode> node = factory.createFlowNode(this.createFlowNode(id, name));
		process.getFlowElement().add(node);
	}
	
	private void addFlowNode(TProcess process) {
		this.addFlowNode(NID, NNAME, process);
	}
	
	private TSubProcess createSubProcess(String id, String name) {
		ObjectFactory factory = new ObjectFactory();
		TSubProcess subProcess = factory.createTSubProcess();
		subProcess.setId(id);
		subProcess.setName(name);
		return subProcess;
	}
	
	private List<TSubProcess> createSubProcesses() {
		ArrayList<TSubProcess> result = new ArrayList<TSubProcess>();
		result.add(this.createSubProcess("4712", "P01"));
		result.add(this.createSubProcess("4713", "P02"));
		return result;
	}
	
	private void addSubProcesses(TProcess process) {
		ObjectFactory factory = new ObjectFactory();
		process.getFlowElement().add(factory.createSubProcess(this.createSubProcess("4712", "P01")));
		process.getFlowElement().add(factory.createSubProcess(this.createSubProcess("4713", "P02")));
	}

}
