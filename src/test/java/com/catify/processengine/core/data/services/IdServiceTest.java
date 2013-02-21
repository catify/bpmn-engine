package com.catify.processengine.core.data.services;

import static org.junit.Assert.*;

import java.util.List;

import javax.xml.bind.JAXBElement;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.catify.processengine.core.nodes.eventdefinition.EventDefinitionParameter;
import com.catify.processengine.core.processdefinition.jaxb.ObjectFactory;
import com.catify.processengine.core.processdefinition.jaxb.TExtensionElements;
import com.catify.processengine.core.processdefinition.jaxb.TProcess;
import com.catify.processengine.core.processdefinition.jaxb.TVersion;

public class IdServiceTest {

	private static final String PID = "4711";
	private static final String PNAME = "FOO";
	private static final String PVERSION = "1.2.1";
	private static final String CLIENT = "BAR";
	private static final String NID = "0815";
	private static final String NNAME = "NODE1";
	private static final String SUBP = "P01P02";
	
	private static final String UPID = "BARFOO47111.2.1";
	private static final String UNID = "BARFOO47111.2.1P01P020815NODE1";

	@Test
	public void testGetUniqueProcessIdIdParams() {
		EventDefinitionParameter params = new EventDefinitionParameter(CLIENT, this.createProcess(), null, null);
		assertEquals(UPID, IdService.getUniqueProcessId(params));
	}

	@Test
	public void testGetUniqueProcessIdStringTProcess() {
		assertEquals(UPID, IdService.getUniqueProcessId(CLIENT, this.createProcess()));
	}

	@Test
	public void testGetUniqueProcessIdEventDefinitionParameter() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetArchivedUniqueProcessId() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetUniqueFlowNodeIdStringTProcessArrayListOfTSubProcessTFlowNode() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetUniqueFlowNodeIdEventDefinitionParameter() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetArchivedUniqueFlowNodeIdStringTProcessArrayListOfTSubProcessTFlowNode() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetUniqueFlowNodeIdStringTProcessString() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetUniqueFlowNodeIdStringTProcessArrayListOfTSubProcessString() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetArchivedUniqueFlowNodeIdStringTProcessString() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetArchivedUniqueFlowNodeIdStringTProcessArrayListOfTSubProcessString() {
		fail("Not yet implemented");
	}
	
	private TProcess createProcess() {
		TProcess process = new TProcess();
		process.setId(PID);
		process.setName(PNAME);
		
		process.setExtensionElements(new TExtensionElements());
		List<Object> elements = process.getExtensionElements().getAny();

		TVersion tVersion = new ObjectFactory().createTVersion();
		tVersion.setVersion(PVERSION);
		
		JAXBElement<TVersion> version = new ObjectFactory().createVersion(tVersion);

		elements.add(version);
		
		return process;
	}

}
