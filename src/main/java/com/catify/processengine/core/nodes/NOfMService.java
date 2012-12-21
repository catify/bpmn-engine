package com.catify.processengine.core.nodes;

import com.catify.processengine.core.messages.Message;

public interface NOfMService {

	/**
	 * Process n of m condition:
	 * <br> 1. Increment the flows fired. 
	 * <br> 2. Check if the gateway should trigger (n = m) 
	 *
	 * @param message the message
	 * @return true, if successful
	 */
	boolean checkNOfMCondition(Message message, int flowsFired);

	int incrementSequenceFlowsFired(Message message, int flowsFired);

}
