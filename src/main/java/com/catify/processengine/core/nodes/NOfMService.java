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
