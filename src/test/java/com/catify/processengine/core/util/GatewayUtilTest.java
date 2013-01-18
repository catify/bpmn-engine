package com.catify.processengine.core.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import com.catify.processengine.core.services.NodeInstanceMediatorService;

public class GatewayUtilTest {

	@Test
	public void testSetFiredPlusOne() {
		NodeInstanceMediatorService nims = mock(NodeInstanceMediatorService.class);
		nims.setSequenceFlowsFired("47", 0);
		int plusOne = GatewayUtil.setFiredPlusOne(nims, "47");
		assertEquals(1, plusOne);
	}

}
