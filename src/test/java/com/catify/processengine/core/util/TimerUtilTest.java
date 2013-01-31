package com.catify.processengine.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.*;


import java.util.List;


import org.junit.Test;

/**
 * Test class for {@link TimerUtil}.
 * 
 * @author claus straube
 *
 */
public class TimerUtilTest {
	
	@Test
	public void testDuration() {
		// PT1M
		long d1 = TimerUtil.calculateTimeToFireForDuration(1000000000000L, "PT1M");
		assertEquals(1000000060000L, d1);
		// PT2H1M10S
		long d2 = TimerUtil.calculateTimeToFireForDuration(1000000000000L, "PT2H1M10S");
		long x = 1000000000000L + 1000 * 10 + 1000 * 60 * 1 + 1000 * 60 * 60 * 2;
		assertEquals(x, d2);
		// convenient method
		long d3 = TimerUtil.calculateTimeToFireForDuration("PT1M");
		assertTrue(d3 > 1000000000000L);
	}
	
	@Test
	public void testDate() {
		// 2013-04-09T16:34:08Z
		long d1 = TimerUtil.calculateTimeToFireForDate("2013-04-09T16:34:08Z");
		assertEquals(1365525248000L, d1);
		// 2008-02-01T09:00:22+05:00
		long d2 = TimerUtil.calculateTimeToFireForDate("2008-02-01T09:00:22+05:00");
		assertEquals(1201838422000L, d2);
	}
	
	@Test
	public void testCycleWithBoundedRepetitions() {
		// R5/PT1M -- 5 repetitions
		List<Long> c1 = TimerUtil.calculateTimeToFireForCycle(1000000000000L, "R5/PT1M");
		assertNotNull(c1);
		assertEquals(5, c1.size());
		assertTrue(c1.contains(1000000060000L));
		assertTrue(c1.contains(1000000120000L));
		assertTrue(c1.contains(1000000180000L));
		assertTrue(c1.contains(1000000240000L));
		assertTrue(c1.contains(1000000300000L));
	}
	
	@Test
	public void testCycleWithUnboundedRepetitions() {
		// R/PT1M -- unbounded repetitions: the next time to fire should be calculated
		List<Long> c1 = TimerUtil.calculateTimeToFireForCycle(1000000000000L, "R/PT1M");
		assertNotNull(c1);
		assertEquals(1, c1.size());
		assertTrue(c1.contains(1000000060000L));
	}
	
	@Test
	public void testCycleWithUnboundedRepetitionsAndStartDateAfterNow() {
		// R/2013-04-09T16:34:08Z/P1D -- unbounded cycle with start date after now
		List<Long> c1 = TimerUtil.calculateTimeToFireForCycle(
				TimerUtil.calculateTimeToFireForDate("2013-01-29T00:00:00Z"),
				"R/2013-01-30T00:00:00Z/PT1H");
		assertNotNull(c1);
		assertEquals(1, c1.size());
		assertTrue(c1.contains(1359504000000L));
	}
	
	@Test
	public void testCycleWithUnboundedRepetitionsWithStartDateBeforeNow() {
		// R/2013-04-09T16:34:08Z/P1D -- unbounded cycle with start date before now
		List<Long> c1 = TimerUtil.calculateTimeToFireForCycle(
				TimerUtil.calculateTimeToFireForDate("2013-01-31T00:00:00Z"),
				"R/2013-01-30T00:00:00Z/PT1H");
		assertNotNull(c1);
		assertEquals(1, c1.size());
		assertTrue(c1.contains(1359594000000L));
	}
	
	@Test
	public void testCycleWithBoundedRepetitionsAndStartDate() {
		// R3/2013-04-09T16:34:08Z/P1D -- bounded cycle with start date
		List<Long> c1 = TimerUtil.calculateTimeToFireForCycle(
				TimerUtil.calculateTimeToFireForDate("2013-01-29T00:00:00Z"),
				"R10/2013-01-30T00:00:00Z/PT1H");
		assertNotNull(c1);
		assertEquals(11, c1.size());
	}
	
	@Test
	public void testCycleWithBoundedRepetitonsAndEndDate() {
		// R3/PT1H/2013-01-30T23:00:00Z -- bounded cycle with end date
		List<Long> c1 = TimerUtil.calculateTimeToFireForCycle(
				TimerUtil.calculateTimeToFireForDate("2013-01-30T00:00:00Z"),
				"R3/PT1H/2013-01-30T23:01:00Z");
		assertNotNull(c1);
		assertEquals(3, c1.size());
	}
	
	@Test
	public void testCycleWithUnboundedRepetitionsAndEndDate() {
		// R/PT1H/2013-01-30T23:00:00Z -- unbounded cycle with end date
		List<Long> c1 = TimerUtil.calculateTimeToFireForCycle(
				TimerUtil.calculateTimeToFireForDate("2013-01-30T00:00:00Z"),
				"R/PT1H/2013-01-30T23:01:00Z");
		assertNotNull(c1);
		assertEquals(24, c1.size());
	}
	
	@Test
	public void testCycleConvenientMethod() {
		// convenient method
		List<Long> c3 = TimerUtil.calculateTimeToFireForCycle("R5/PT1M");
		assertNotNull(c3);
		assertEquals(5, c3.size());
	}
	
	@Test
	public void testIsUnboundedCycle() {
		assertTrue(TimerUtil.isUnboundedCycle("R/2013-01-30T00:00:00Z/PT1H"));
		assertFalse(TimerUtil.isUnboundedCycle("R3/2013-01-30T00:00:00Z/PT1H"));
	}

}
