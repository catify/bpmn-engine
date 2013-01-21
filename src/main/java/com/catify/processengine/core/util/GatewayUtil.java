package com.catify.processengine.core.util;

import com.catify.processengine.core.services.NodeInstanceMediatorService;

public class GatewayUtil {

	/**
	 * Increments the value of the incoming fired sequence flows
	 * and sets the "sequence flows fired" value inside 
	 * {@link NodeInstanceMediatorService}.
	 * 
	 * @param nims a instance of {@link NodeInstanceMediatorService}
	 * @param fired number of fired incoming flows
	 * @param iid instance id
	 * @return fired of incoming flows plus one
	 */
	public static int setFiredPlusOne(NodeInstanceMediatorService nims, String iid) {
		int fired = nims.getSequenceFlowsFired(iid);
		int firedplus = ++fired;
		nims.setSequenceFlowsFired(iid, firedplus);
		return firedplus;
	}
	
}
