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
package com.catify.processengine.core.data.services;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests {@link IdParams}.
 * 
 * @author claus straube
 *
 */
public class IdParamsTest {

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
	public void testProcessParamsConstructorForProcess() {
		IdParams params = new IdParams(CLIENT, PNAME, PVERSION, PID);
		this.checkProcessParams(params);
	}

	@Test
	public void testProcessParamsConstructorForProcessAndNode() {
		IdParams params = new IdParams(CLIENT, PNAME, PVERSION, PID, SUBP, NID, NNAME);
		this.checkProcessParams(params);
		this.checkNodeParams(params);
	}

	@Test
	public void testGetUniqueProcessId() {
		assertEquals(UPID, new IdParams(CLIENT, PNAME, PVERSION, PID).getUniqueProcessId());
	}

	@Test
	public void testGetUniqueProcessIdWithPrefix() {
		assertEquals("Archived:" + UPID, new IdParams(CLIENT, PNAME, PVERSION, PID).getUniqueProcessId("Archived:"));
	}

	@Test
	public void testGetUniqueFlowNodeId() {
		assertEquals(UNID, new IdParams(CLIENT, PNAME, PVERSION, PID, SUBP, NID, NNAME).getUniqueFlowNodeId());
	}

	@Test
	public void testGetUniqueFlowNodeIdString() {
		assertEquals("Archived:" + UNID, new IdParams(CLIENT, PNAME, PVERSION, PID, SUBP, NID, NNAME).getUniqueFlowNodeId("Archived:"));
	}
	
	private void checkProcessParams(IdParams params) {
		assertEquals(PID, params.getProcessId());
		assertEquals(PNAME, params.getProcessName());
		assertEquals(PVERSION, params.getProcessVersion());
		assertEquals(CLIENT, params.getClientId());
	}
	
	private void checkNodeParams(IdParams params) {
		assertEquals(NID, params.getFlowNodeId());
		assertEquals(NNAME, params.getFlowNodeName());
		assertEquals(SUBP, params.getSubProcesses());
	}

}
