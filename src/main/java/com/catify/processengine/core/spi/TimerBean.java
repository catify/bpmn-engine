package com.catify.processengine.core.spi;

/**
 * The {@link TimerBean} defines all attributes that
 * are needed to save a Timer via a {@link TimerSPI} 
 * implementation.
 * 
 * @author claus straube
 * 
 */
public class TimerBean {

	private long timeToFire;
	private String actorRef;
	private String processInstanceId;
	
	public TimerBean() {}
	
	public TimerBean(long timeToFire, String actorRef, String processInstanceId) {
		this.timeToFire = timeToFire;
		this.actorRef = actorRef;
		this.processInstanceId = processInstanceId;
	}
	
	public long getTimeToFire() {
		return timeToFire;
	}
	public void setTimeToFire(long timeToFire) {
		this.timeToFire = timeToFire;
	}
	public String getActorRef() {
		return actorRef;
	}
	public void setActorRef(String actorRef) {
		this.actorRef = actorRef;
	}
	public String getProcessInstanceId() {
		return processInstanceId;
	}
	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}
	
	
	
	
}
