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
package com.catify.processengine.core.nodes.eventdefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;

import com.catify.processengine.core.data.dataobjects.TimerBean;
import com.catify.processengine.core.data.dataobjects.TimerSPI;
import com.catify.processengine.core.messages.ActivationMessage;
import com.catify.processengine.core.messages.CommitMessage;
import com.catify.processengine.core.messages.DeactivationMessage;
import com.catify.processengine.core.messages.TriggerMessage;
import com.catify.processengine.core.util.TimerUtil;

/**
 * The TimerEventDefinition will constantly evaluate the due timers and create {@link TriggerMessage}s to fire those.
 * Saving and loading is handled via the configured {@link TimerSPI} service provider, while time calculations are done
 * via the {@link TimerUtil}.
 *
 * @author christopher köster
 * @author claus straube
 * 
 */
@Configurable
public class TimerEventDefinition extends EventDefinition {
	
	static final Logger LOG = LoggerFactory
			.getLogger(TimerEventDefinition.class);
	
	/** The timer spi implementation id set in the spring context. */
	@Value("${timer.implementation}")
	private String timerServiceProviderId;
	
	/** The timer polling interval in milliseconds. */
	@Value("${timer.interval}")
	private long timerPollingInterval;
	
	/** The event that is holding this event definition. */
	private ActorRef eventActorRef;
	
	private TimerTypes timerType;
	private String isoDate;
	
	private TimerSPI timerSPI;

	/**
	 * Instantiates a new timer event definition.
	 *
	 * @param eventNodeActorRef the underlying event node actor ref
	 * @param timerType the timer type
	 * @param isoDate the iso date
	 */
	public TimerEventDefinition(ActorRef eventNodeActorRef, TimerTypes timerType, String isoDate) {
		this.timerType = timerType;
		this.isoDate = isoDate;
		this.eventActorRef = eventNodeActorRef;
	}
	
	/**
	 * Inits the spring dependent fields, like TimerSPI service provider and the akka scheduler.
	 */
	@PostConstruct
	void initSpringDependentFields() {
		this.timerSPI = TimerSPI.getTimerImpl(timerServiceProviderId);
		
		// register cycle timers
		if (timerType == TimerTypes.TIMECYCLE) {
			List<Long> timeToFire = this.getTimeToFire();
			this.saveTimers(timeToFire);
		} 
		
		// init akka scheduler
		actorSystem.scheduler().schedule(
				Duration.create(10000, TimeUnit.MILLISECONDS), // delay until scheduler starts
				Duration.create(timerPollingInterval, TimeUnit.MILLISECONDS), // polling interval
				new Runnable() {
					@Override
					public void run() {
						// for each due timer create a trigger message and send it to the underlying event node
						List<TimerBean> dueTimers = timerSPI.loadDueTimers(getSelf().toString());
						for (TimerBean timerBean : dueTimers) {
							eventActorRef.tell(new TriggerMessage(timerBean.getProcessInstanceId(), null), getSender());
						}
					}
				},
				actorSystem.dispatcher());
	}
	
	/* (non-Javadoc)
	 * @see com.catify.processengine.core.nodes.eventdefinition.EventDefinition#acitivate(com.catify.processengine.core.messages.ActivationMessage)
	 */
	@Override
	protected CommitMessage<?> activate(ActivationMessage message) {

		// create and save next timer
		String processInstanceId = message.getProcessInstanceId();
		List<Long> timeToFire = this.getTimeToFire();
		
		this.saveTimers(processInstanceId, timeToFire);
		
		return createSuccessfullCommitMessage(message.getProcessInstanceId());
	}

	/* (non-Javadoc)
	 * @see com.catify.processengine.core.nodes.eventdefinition.EventDefinition#deactivate(com.catify.processengine.core.messages.DeactivationMessage)
	 */
	@Override
	protected CommitMessage<?> deactivate(DeactivationMessage message) {
		
		this.timerSPI.deleteTimer(this.getSelf().toString(), message.getProcessInstanceId());
		
		return createSuccessfullCommitMessage(message.getProcessInstanceId());
	}
	/* (non-Javadoc)
	 * @see com.catify.processengine.core.nodes.eventdefinition.EventDefinition#trigger(com.catify.processengine.core.messages.TriggerMessage)
	 */
	@Override
	protected CommitMessage<?> trigger(TriggerMessage message) {		
		// if this timer is unbounded set next timer
		if (timerType == TimerTypes.TIMECYCLE && TimerUtil.isUnboundedCycle(isoDate)) {
			List<Long> timeToFire = this.getTimeToFire();
			this.saveTimers(message.getProcessInstanceId(), timeToFire);
		}
		
		return createSuccessfullCommitMessage(message.getProcessInstanceId());
	}

	/**
	 * Save timers.
	 *
	 * @param processInstanceId the process instance id
	 * @param timeToFire the list of time to fire
	 */
	private void saveTimers(String processInstanceId, List<Long> timeToFire) {
		for (Long longToFire : timeToFire) {
			this.timerSPI.saveTimer(new TimerBean(longToFire, this.getSelf().toString(), processInstanceId));
		}
	}
	
	/**
	 * Save timers (with generated process instance id).
	 *
	 * @param timeToFire the time to fire
	 */
	private void saveTimers(List<Long> timeToFire) {
		for (Long longToFire : timeToFire) {
			this.timerSPI.saveTimer(new TimerBean(longToFire, this.getSelf().toString(), UUID.randomUUID().toString()));
		}
	}

	/**
	 * Gets the time to fire depending on the TimerTypes.
	 *
	 * @return the time to fire
	 */
	private List<Long> getTimeToFire() {
		List<Long> timeToFire = new ArrayList<Long>();
		
		switch (this.timerType) {
		case TIMEDATE:
			timeToFire.add(TimerUtil.calculateTimeToFireForDate(this.isoDate));
			break;
		case TIMEDURATION:
			timeToFire.add(TimerUtil.calculateTimeToFireForDuration(this.isoDate));
			break;
		case TIMECYCLE:
			timeToFire.addAll(TimerUtil.calculateTimeToFireForCycle(this.isoDate));
			break;
		default:
			LOG.error("Unrecognized Timer type. TimerEventDefinition will fail!");
			break;
		}
		return timeToFire;
	}

}
