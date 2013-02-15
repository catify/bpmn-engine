package com.catify.processengine.core.nodes.loops;

import com.catify.processengine.core.messages.ActivationMessage;
import com.catify.processengine.core.messages.DeactivationMessage;
import com.catify.processengine.core.messages.TriggerMessage;

public interface LoopTypeStrategy {

	void activate(ActivationMessage message); 
	void deactivate(DeactivationMessage message); 
	void trigger(TriggerMessage message); 
	
}
