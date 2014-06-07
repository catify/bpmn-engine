package com.catify.processengine.core.spi;

import com.catify.processengine.core.spi.TimerBean;
import com.catify.processengine.core.spi.TimerSPI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Mock implementation for {@link TimerSPI}.
 * 
 * @author claus straube
 *
 */
public class TimerSPIMock extends TimerSPI {

	List<TimerBean> db;
	
	public TimerSPIMock() {
		this.db = new ArrayList<TimerBean>();
	}
	
	
	@Override
	public void saveTimer(TimerBean timer) {
		this.db.add(timer);
		LOG.debug(String.format("saved timer (%s) for --> %s | %s", timer.getTimeToFire(), timer.getActorRef(), timer.getProcessInstanceId()));
	}

	@Override
	public List<TimerBean> loadDueTimers(String actorRef) {
		List<TimerBean> result = new ArrayList<TimerBean>();
		Iterator<TimerBean> it = this.db.iterator();
		long now = System.currentTimeMillis();
		while (it.hasNext()) {
			TimerBean timer = (TimerBean) it.next();
			if(timer.getTimeToFire() <= now && actorRef.equals(timer.getActorRef())) {
				result.add(timer);
				LOG.debug(String.format("found due timer (%s) for --> %s | %s", timer.getTimeToFire(), timer.getActorRef(), timer.getProcessInstanceId()));
			}
		}
		return result;
	}

	@Override
	public void deleteTimer(String actorRef, String processInstanceId) {
		Iterator<TimerBean> it = this.db.iterator();
		while (it.hasNext()) {
			TimerBean timer = (TimerBean) it.next();
			if(actorRef.equals(timer.getActorRef()) && processInstanceId.equals(timer.getProcessInstanceId())){
				this.db.remove(timer);
				LOG.debug(String.format("removed timer (%s) for --> %s | %s", timer.getTimeToFire(), timer.getActorRef(), timer.getProcessInstanceId()));
			}
		}
	}

}
