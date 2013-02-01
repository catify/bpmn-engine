package com.catify.processengine.core.nodes.eventdefinition;

/**
 * The Enum TimerTypes used in TimerEventDefinition. 
 * It is originated in the bpmn/jaxb TTimerEventDefinition and holds all possible timer variations.
 * 
 * @author christopher köster
 * 
 */
public enum TimerTypes {

	TIMEDATE, 
	TIMEDURATION,
	TIMECYCLE

}
