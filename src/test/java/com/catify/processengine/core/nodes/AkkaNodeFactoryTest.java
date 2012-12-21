/**
 * 
 */
package com.catify.processengine.core.nodes;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author chris
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:META-INF/spring/spring-context.xml" })
@Transactional
public class AkkaNodeFactoryTest {

	
	// Tests needed?
	@Test
	public void testCreateStartEventNode(){
	}
	
	
}
