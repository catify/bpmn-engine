package com.catify.processengine.core.services;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;

import com.catify.processengine.core.spi.DataObjectHandling;
import com.catify.processengine.core.spi.DataObjectSPI;

/**
 * 
 * 
 * @author claus straube
 * @author christopher köster
 * 
 */
public class ExpressionService {

	public static final JexlEngine JEXL = new JexlEngine();
	public static final Logger LOG = LoggerFactory.getLogger(ExpressionService.class);
	
	/**
	 * Creates a new JEXL engine instance.
	 * 
	 * @return a new {@link JexlEngine}
	 */
	public static JexlEngine createJexlEngine() {
		return new JexlEngine();
	}
	
	/**
	 * Evaluates, what data objects are used inside a 
	 * JEXL expression. So we have only to load the objects
	 * that are really used.
	 * 
	 * @param expression as {@link String} (foo.a > bar.b)
	 * @param dataObjectIds as {@link String} in a {@link Set}
	 * @return all data object ids that are available in the process and use inside the expression as {@link Set}.
	 */
	public static Set<String> evaluateUsedObjects(String expression, Set<String> dataObjectIds) {
		Set<String> result = new HashSet<String>();
		
		for (String id : dataObjectIds) {
			if(expression.contains(id)) {
				result.add(id);
			}
		}
		
		return result;
	}
	
	/**
	 * Evaluates all data objects (object ids) used inside a
	 * list of different expressions. If an object foo occurs 
	 * in multible expressions, its exactly one object id for
	 * foo inside the result set.
	 * 
	 * @param expressions
	 * @param dataObjectIds
	 * @return
	 */
	public static Set<String> evaluateAllUsedObjects(List<String> expressions, Set<String> dataObjectIds) {
		Set<String> result = new HashSet<String>();
		
		for (String expression : expressions) {
			Set<String> oids = evaluateUsedObjects(expression, dataObjectIds);
			result.addAll(oids);
		}
		
		return result;
	}
	
	/**
	 * Converts a JEXL {@link String} into a JEXL {@link Expression}. Take a look on the
	 * <a href="http://commons.apache.org/jexl/reference/syntax.html">Apache Commons JEXL page</a> 
	 * to get more information.
	 * 
	 * @param expression as {@link String} (foo.a > bar.b)
	 * @param jexlEngine a external jexlEngine instance
	 * @return expression as {@link Expression}
	 */
	public static Expression createJexlExpression(String expression, JexlEngine jexlEngine) {
		return jexlEngine.createExpression(expression);
	}
	
	/**
	 * Converts a JEXL {@link String} into a JEXL {@link Expression}. Take a look on the
	 * <a href="http://commons.apache.org/jexl/reference/syntax.html">Apache Commons JEXL page</a> 
	 * to get more information.
	 * 
	 * @param expression as {@link String} (foo.a > bar.b)
	 * @return expression as {@link Expression}
	 */
	public static Expression createJexlExpression(String expression) {
		// there can be null values - e.g. within default sequences
		Expression result = null;
		if(expression != null) {
			LOG.debug(String.format("Creating JEXL expression from '%s'.", expression));
			result = JEXL.createExpression(expression);
		}
		return result;
	}
	
	/**
	 * Converts a {@link Set} of JEXL {@link String}s into a {@link Set} of 
	 * JEXL {@link Expression}s. Take a look on the
	 * <a href="http://commons.apache.org/jexl/reference/syntax.html">Apache Commons JEXL page</a> 
	 * to get more information.
	 * 
	 * @param expressions the {@link String} expressions in a {@link Set}
	 * @param jexlEngine a external jexlEngine instance
	 * @return the {@link Expression} in a {@link Set}
	 */
	public static Set<Expression> createJexlExpressions(Set<String> expressions, JexlEngine jexlEngine) {
		Set<Expression> result = new HashSet<Expression>();
		
		for (String expression : expressions) {
			result.add(createJexlExpression(expression, jexlEngine));
		}
		
		return result;
	}
	
	/**
	 * Converts a {@link Set} of JEXL {@link String}s into a {@link Set} of 
	 * JEXL {@link Expression}s. Take a look on the
	 * <a href="http://commons.apache.org/jexl/reference/syntax.html">Apache Commons JEXL page</a> 
	 * to get more information.
	 * 
	 * @param expressions the {@link String} expressions in a {@link Set}
	 * @return the {@link Expression} in a {@link Set}
	 */
	public static Set<Expression> createJexlExpressions(Set<String> expressions) {
		Set<Expression> result = new HashSet<Expression>();
		
		for (String expression : expressions) {
			result.add(createJexlExpression(expression));
		}
		
		return result;
	}
	
	/**
	 * Converts a {@link Map} of JEXL {@link String}s into a {@link Map} of 
	 * JEXL {@link Expression}s. Take a look on the
	 * <a href="http://commons.apache.org/jexl/reference/syntax.html">Apache Commons JEXL page</a> 
	 * to get more information.
	 * 
	 * @param expressions
	 * @return
	 */
	public static Map<ActorRef,Expression> createJexlExpressions(Map<ActorRef,String> expressions) {
		Map<ActorRef,Expression> result = new TreeMap<ActorRef, Expression>();
		
		Iterator<ActorRef> it = expressions.keySet().iterator();
		while (it.hasNext()) {
			ActorRef actorRef = it.next();
			if(expressions != null) {
				result.put(actorRef, createJexlExpression(expressions.get(actorRef)));
			} else {
				System.out.println("###################### expressions NULL!!!");
			}
		}
		
		return result;
	}
	
	/**
	 * Evaluates a given JEXL {@link Expression} with the given
	 * objects (loaded over the {@link DataObjectSPI}).
	 * 
	 * @param expression a JEXL {@link Expression}
	 * @param dataObjectIds all needed data objects ids (see {@link ExpressionService.evaluateUsedObjects})
	 * @param dataObjectHandler the connector to the data
	 * @param uniqueProcessId the process id
	 * @param instanceId the instance id
	 * @return the result of the JEXL evaluation
	 */
	public static Object evaluate(Expression expression, 
			Set<String> dataObjectIds, 
			DataObjectHandling dataObjectHandler,
			String uniqueProcessId, 
			String instanceId) {
		
		JexlContext jc = fillContext(dataObjectIds, dataObjectHandler, uniqueProcessId, instanceId);
		
		// evaluate the expression
		Object result = expression.evaluate(jc);
        
		return result;
	}
	
	/**
	 * Evaluates a given JEXL {@link Expression} and returns it result.
	 *
	 * @param expression a JEXL {@link Expression}
	 * @return the result of the JEXL evaluation
	 */
	public static Object evaluate(Expression expression) {
		
		JexlContext jc = new MapContext();
		jc.set("cardinality", expression);
		
		// evaluate the expression
		Object result = expression.evaluate(jc);
        
		return result;
	}
	
	public static JexlContext fillContext(Set<String> dataObjectIds, 
			DataObjectHandling dataObjectHandler,
			String uniqueProcessId, 
			String instanceId) {
		// create context
		JexlContext jc = new MapContext();
				
		// fill context
		for (String id : dataObjectIds) {
			jc.set(id, dataObjectHandler.loadObject(uniqueProcessId, instanceId, id));
		}
		
		return jc;
	}
	
	public static JexlContext fillContext(Set<String> dataObjectIds, 
			DataObjectHandling dataObjectHandler,
			String uniqueProcessId, 
			String instanceId,
			int loopCount) {
		// create context
		JexlContext jc = fillContext(dataObjectIds, dataObjectHandler, uniqueProcessId, instanceId);
		
		jc.set("$LOOPCOUNTER", loopCount);
		
		return jc;
	}
	
	/**
	 * Evaluates a given JEXL {@link Expression} with the given
	 * objects (loaded over the {@link DataObjectSPI}).
	 * 
	 * @param expression a JEXL {@link Expression}
	 * @param dataObjectIds all needed data objects ids (see {@link ExpressionService.evaluateUsedObjects})
	 * @param dataObjectHandler the connector to the data
	 * @param uniqueProcessId the process id
	 * @param instanceId the instance id
	 * @return the result of the JEXL evaluation (must be boolean - otherwise an {@link IllegalArgumentException} will be thrown
	 */
	public static boolean evaluateToBoolean(Expression expression, 
			Set<String> dataObjectIds, 
			DataObjectHandling dataObjectHandler,
			String uniqueProcessId, 
			String instanceId) {
		
		JexlContext context = fillContext(dataObjectIds, dataObjectHandler, uniqueProcessId, instanceId);
		return evaluateToBoolean(expression, context);
	}
	
	/**
	 * Evaluates a given JEXL {@link Expression} with the given
	 * {@link JexlContext}.
	 * 
	 * @param expression a JEXL expression.
	 * @param context the filled JEXL context.
	 * @return
	 */
	public static boolean evaluateToBoolean(Expression expression, JexlContext context) {
		if(expression != null) {
			Object result = expression.evaluate(context);
			LOG.debug(String.format("Evaluated expression '%s' to result '%s'.", expression.getExpression(), result));
			
			if(result instanceof Boolean) {
				return (Boolean) result;
			} else {
				throw new IllegalArgumentException(String.format("Your JEXL expression '%s' does not have a boolean result.", expression.getExpression()));
			}
		}
		return false;
	}
	
	
}
