package com.catify.processengine.core.util;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;

/**
 * 
 * 
 * @author claus straube
 *
 */
public class TimerUtil {
	
	private static final DateTimeFormatter 	dateFormatter = ISODateTimeFormat.dateTimeNoMillis();
	private static final PeriodFormatter 	periodformatter = ISOPeriodFormat.standard();
	
	/**
	 * Calculates the time stamp in millis for a 
	 * given ISO 8601 date (e.g. 2013-04-09T16:34:08Z). 
	 * The date must have a time zone.
	 * 
	 * @param isoDate ISO 8601 date as {@link String}
	 * @return time stamp in millis
	 */
	public static long calculateTimeToFireForDate(String isoDate) {
		DateTime time = dateFormatter.parseDateTime(isoDate);
		return time.getMillis();
	}
	
	/**
	 * Calculates a list of time stamps in millis. For each
	 * repetition a value will be calculated based on the time
	 * to fire time of the previous repetition. If the 'R' is 
	 * empty (e.g. R/PT1M), which means an unbounded number of repetitions,
	 * the next time to fire is calculated. 
	 * 
	 * @param now
	 * @param isoDate
	 * @return {@link List} of time stamps in millis
	 */
	public static List<Long> calculateTimeToFireForCycle(long now, String isoDate) {
		List<Long> result = new ArrayList<Long>();
		
		String[] split = isoDate.split("/");
		if(split.length < 2 || split.length > 3) {
			throw new IllegalArgumentException(
					"A ISO 8601 date for a cylce shout have a repeat and duration " +
					"section, or a repeat, date and duration section separated by a slash (e.g R5/PT3M).");
		}
		
		// repeat is always the first one
		String repeat = split[0]; 
		// get the repeats
		if (!repeat.startsWith("R")) {
			throw new IllegalArgumentException(
					"A ISO 8601 repeat should start with"
							+ " a 'R', followed by the number of cycles.");
		}
		// get all after the 'R'
		repeat = repeat.substring(1); 
		boolean unbounded = false;
		if (repeat.equals("")) {
			/*
			 * if we have a unbounded number of repetitions,
			 * calculate the next fire time.
			 */
			repeat = "1";
			unbounded = true;
		}
		int r = Integer.parseInt(repeat);
		DateTime baseTime = new DateTime(now);

		if(split.length == 2) {
			Period period = periodformatter.parsePeriod(split[1]);
			// calculate the timestamps for the cycles
			for (int i = 0; i < r; i++) {
				baseTime = baseTime.plus(period);
				result.add(baseTime.getMillis());
			}
		}
		
		// we have start or end date
		if(split.length == 3) {
			
			if (split[1].startsWith("P") && unbounded) {
				/*
				 * calculate all times to fire, until the 
				 * end date is reached.
				 */
				DateTime end = dateFormatter.parseDateTime(split[2]);
				Period period = periodformatter.parsePeriod(split[1]);
				while(baseTime.isBefore(end)) {
					baseTime = baseTime.plus(period);
					result.add(baseTime.getMillis());
				}
				
			} else if (split[2].startsWith("P")) {
				// cycle with start date
				DateTime start = dateFormatter.parseDateTime(split[1]);
				if(start.isBefore(now)) {
					/*
					 * if the start date is in the past,
					 * calculate the next time to fire based
					 * on the 'now' time.
					 */
					Period period = periodformatter.parsePeriod(split[1]);
					baseTime = baseTime.plus(period);
					result.add(baseTime.getMillis());
				} else {
					/* 
					 * if the start date is in the future,
					 * the first time to fire is the start
					 * date. 
					 */					
					result.add(start.getMillis());
				}
			} else {
				throw new IllegalArgumentException(
						"Either the middle or last section of a cycle with start or end date must define a duration.");
			}
			
		}
		
		return result;
	}
	
	/**
	 * 
	 * 
	 * @param isoDate
	 * @return
	 */
	public static List<Long> calculateTimeToFireForCycle(String isoDate) {
		return calculateTimeToFireForCycle(System.currentTimeMillis(), isoDate);
	}
	
	public static long calculateTimeToFireForDuration(long now, String isoDate) {
		DateTime dateTime = new DateTime(now);
		Period period = periodformatter.parsePeriod(isoDate);
		dateTime = dateTime.plus(period);
		return dateTime.getMillis();
	}
	
	public static long calculateTimeToFireForDuration(String isoDate) {
		return calculateTimeToFireForDuration(System.currentTimeMillis(), isoDate);
	}
	
}
