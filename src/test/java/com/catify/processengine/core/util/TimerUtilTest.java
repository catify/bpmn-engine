package com.catify.processengine.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
	public void testCycle() {
		// R5/PT1M -- 5 repetitions
		List<Long> c1 = TimerUtil.calculateTimeToFireForCycle(1000000000000L, "R5/PT1M");
		assertNotNull(c1);
		assertEquals(5, c1.size());
		assertTrue(c1.contains(1000000060000L));
		assertTrue(c1.contains(1000000120000L));
		assertTrue(c1.contains(1000000180000L));
		assertTrue(c1.contains(1000000240000L));
		assertTrue(c1.contains(1000000300000L));
		// R/PT1M -- unbounded repetitions: the next time to fire should be calculated
		List<Long> c2 = TimerUtil.calculateTimeToFireForCycle(1000000000000L, "R/PT1M");
		assertNotNull(c2);
		assertEquals(1, c2.size());
		assertTrue(c2.contains(1000000060000L));
		// R/2013-04-09T16:34:08Z/P1D -- unbounded cycle with start date
		
		// R/PT1H/2013-01-30T23:00:00Z -- unbounded cycle with end date
		List<Long> c4 = TimerUtil.calculateTimeToFireForCycle(TimerUtil.calculateTimeToFireForDate("2013-01-30T00:00:00Z"), "R/PT1H/2013-01-30T23:01:00Z");
		assertNotNull(c4);
		assertEquals(24, c4.size());		
		// convenient method
		List<Long> c3 = TimerUtil.calculateTimeToFireForCycle("R5/PT1M");
		assertNotNull(c3);
		assertEquals(5, c3.size());
	}

}
