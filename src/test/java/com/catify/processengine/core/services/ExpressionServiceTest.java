package com.catify.processengine.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlEngine;
import org.junit.Test;

import com.catify.processengine.core.data.dataobjects.DataObjectService;

/**
 * Tests the {@link ExpressionService}.
 * 
 * @author claus straube
 *
 */
public class ExpressionServiceTest {

	private static final String PID = "1";
	private static final String IID = "47";

	@Test
	public void testCreateJexlEngine() {
		JexlEngine jexlEngine = ExpressionService.createJexlEngine();
		assertNotNull(jexlEngine);
		assertNotNull(ExpressionService.JEXL);
	}
	
	@Test
	public void testEvaluateUsedObjects() {
		Set<String> ids = createIdSet("foo", "car", "bar", "dar");
		Set<String> usedObjects = ExpressionService.evaluateUsedObjects("foo.a > bar.b", ids);
		assertNotNull(usedObjects);
		assertEquals(2, usedObjects.size());
		assertTrue(usedObjects.contains("foo"));
		assertTrue(usedObjects.contains("bar"));
	}
	
	@Test
	public void testEvaluateAllUsedObjects() {
		Set<String> ids = createIdSet("foo", "bar", "aar", "dar", "for");
		List<String> expressions = new ArrayList<String>();
		expressions.add("foo.a > bar.b");
		expressions.add("foo.a == dar.x");
		expressions.add("foo.a > 10000");
		Set<String> usedObjects = ExpressionService.evaluateAllUsedObjects(expressions, ids);
		assertNotNull(usedObjects);
		assertEquals(3, usedObjects.size());
		assertTrue(usedObjects.contains("foo"));
		assertTrue(usedObjects.contains("bar"));
		assertTrue(usedObjects.contains("dar"));
	}

	@Test
	public void testCreateJexlExpression() {
		// internal engine
		Expression exp1 = ExpressionService.createJexlExpression("foo.a > bar.b");
		assertNotNull(exp1);
		assertEquals("foo.a > bar.b", exp1.getExpression());
		
		// external engine
		Expression exp2 = ExpressionService.createJexlExpression("foo.a > bar.b", ExpressionService.createJexlEngine());
		assertNotNull(exp2);
		assertEquals("foo.a > bar.b", exp2.getExpression());
	}

	@Test
	public void testCreateJexlExpressions() {
		Set<String> exps = new HashSet<String>();
		exps.add("foo.a > bar.b");
		exps.add("foo.a == bar.b");
		
		// internal engine
		Set<Expression> exps1 = ExpressionService.createJexlExpressions(exps);
		assertNotNull(exps1);
		assertEquals(2, exps1.size());
		
		// external engine
		Set<Expression> exps2 = ExpressionService.createJexlExpressions(exps, ExpressionService.createJexlEngine());
		assertNotNull(exps2);
		assertEquals(2, exps2.size());
	}

	@Test
	public void testEvaluate() {
		Expression exp1 = ExpressionService.createJexlExpression("foo.a > bar.b");
		Expression exp2 = ExpressionService.createJexlExpression("foo.a + bar.b");	
		Set<String> ids = createIdSet("foo", "bar");
		DataObjectService dos = this.getDataObjectServiceMock();
		
		Object result1 = ExpressionService.evaluate(exp1, ids, dos, PID, IID);
		assertNotNull(result1);
		assertTrue(result1 instanceof Boolean);
		assertFalse((Boolean) result1);
		
		Object result2 = ExpressionService.evaluate(exp2, ids, dos, PID, IID);
		assertNotNull(result2);
		assertTrue(result2 instanceof Integer);
		assertEquals(8, result2); // 4 + 3
		
	}

	@Test
	public void testEvaluateToBoolean() {
		Expression exp = ExpressionService.createJexlExpression("foo.a > bar.b");	
		Set<String> ids = createIdSet("foo", "bar");
		
		Object result = ExpressionService.evaluateToBoolean(exp, ids, this.getDataObjectServiceMock(), PID, IID);
		assertNotNull(result);
		assertTrue(result instanceof Boolean);
		assertFalse((Boolean) result);
	}
	
	private DataObjectService getDataObjectServiceMock() {
		DataObjectService dos = mock(DataObjectService.class);
		when(dos.loadObject(PID, IID, "foo")).thenReturn(new Foo(3));
		when(dos.loadObject(PID, IID, "bar")).thenReturn(new Bar(5));
		
		return dos;
	}
	
	private Set<String> createIdSet(String... id) {
		Set<String> ids = new HashSet<String>();
		
		for (int i = 0; i < id.length; i++) {
			ids.add(id[i]);
		}
		
		return ids;
	}
	
	public class Foo {
		
		public Foo(int a) {
			this.a = a;
		}
		
		int a;

		public int getA() {
			return a;
		}

		public void setA(int a) {
			this.a = a;
		}
		
	}
	
	public class Bar {
		
		public Bar(int b) {
			this.b = b;
		}
		
		int b;

		public int getB() {
			return b;
		}

		public void setB(int b) {
			this.b = b;
		}
	
	}

}
