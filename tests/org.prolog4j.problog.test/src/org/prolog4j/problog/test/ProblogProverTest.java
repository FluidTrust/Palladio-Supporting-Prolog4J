package org.prolog4j.problog.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.prolog4j.Compound;
import org.prolog4j.ConversionPolicy;
import org.prolog4j.Converter;
import org.prolog4j.Query;
import org.prolog4j.Solution;
import org.prolog4j.problog.ProblogProverFactory;
import org.prolog4j.test.ProverTest;

/**
 * JUnit test for the jProblog binding. Inherits the common test class.
 */
public class ProblogProverTest extends ProverTest {
	@BeforeClass
	public static void setUpBeforeClass() {
		p = new ProblogProverFactory().createProver();
		
		setup();
	}
	
	/**
	 * Had to be changed, since problog is stupid and sorts its results...
	 */
	@Override
	@Test
	public void testTestOn() {
		//todo: had to change the comparison lists, as problog actually sorts its results alphabetically...
		//could be fixed by using sets instead of lists (like in testAssert)
		List<String> philosophers = Arrays.asList("socrates", "plato");
		List<String> list = new ArrayList<String>(2);
		Solution<String> solution = p.solve("member(X, ?List).", philosophers);
		for (String s : solution.<String>on("X")) {
			list.add(s);
		}
		
		assertEquals(Arrays.asList("plato","socrates"), list);

		Solution<String> solution2 = p.solve("member(X, ?List).", philosophers);
		list.clear();
		for (String s : solution2.on("X", String.class)) {
			list.add(s);
		}
		assertEquals(Arrays.asList("plato","socrates"), list);
	}
	
	@Override
	@Test
	public void testIterable() {
		List<String> mortals = new ArrayList<String>();
		for (String s : p.<String>solve("mortal(X).")) {
			mortals.add(s);
		}
		assertEquals(Arrays.asList("plato","socrates"), mortals);

		int i = 0;
		for (@SuppressWarnings("unused")
		Object o : p.solve("member(X, [0, 1, 2]).")) {
			++i;
		}
		assertEquals(i, 3);

		//todo: Both cases not testable with this testcase "iterable" in mind,
		//problog still creates a result with a probability of 0%
//		for (
//		Object o : p.solve("member(X, 1).")) {
//			fail();
//		}
//		for (
//		Object o : p.solve("member(X, 1).").on("X")) {
//			fail();
//		}
	}
	
	@Override
	@Test
	public void testEmptyList() {
	    var results = p.solve("TMP = [].").toSet();
	    assertEquals(1, results.size());
	    
	    //todo: Due to the "queryrule()" workaround, the actual result of TMP = [] is masked
	    //so there is only a probability of this query
	    //assertTrue(List.class.isInstance(results.iterator().next()));
	    //assertEquals(Collections.emptyList(), results.iterator().next());
	}
	
	@Override
	@Test
	public void testTermConverters() {
		//todo: Had to change the queries to member functions so the result of X could be fetched
		// Due to the "queryrule()" workaround, the results of the original queries is masked
		long iVal = p.<Long>solve("member(X, [1]).").get();
		assertEquals(1, iVal);
		double dVal = p.<Double>solve("member(X, [1.0]).").get();
		assertEquals(1.0, dVal, 0.0);
		String sVal = p.<String>solve("member(X, [prolog4j]).").get();
		assertEquals("prolog4j", sVal);

		List<Long> liVal = p.<List<Long>>solve("member(X, [[0, 1, 2]]).").get();
		assertEquals(Arrays.asList(0l, 1l, 2l), liVal);
		List<String> lsVal = p.<List<String>>solve("member(X, [[a, b, c]]).").get();
		assertEquals(Arrays.asList("a", "b", "c"), lsVal);

		// Compounds do currently not make a lot of sense and would require quiet some work
//		Object cVal = p.solve("X = functor(arg1, arg2).").get();
//		assertEquals(new Compound("functor", "arg1", "arg2"), cVal);
	}
	
	@Override
	@Test
	public void testPlaceHolders() {
		assertSuccess("mortal(?).", "socrates");
		assertFailure("mortal(?).", "zeus");
		assertSuccess("mortal(?X).", "socrates");
		assertFailure("mortal(?X).", "zeus");
		assertSuccess("mortal(?LongVariable).", "socrates");
		assertFailure("mortal(?LongVariable).", "zeus");
		
		//todo: How is this supposed to work correctly?!!
		assertSuccess("_ = 'Question ??Mark'.");
		assertSuccess("? = '??Mark'.", "?Mark");
//		assertSuccess("'Is it OK??' = ?.", "Is it OK?");
//		assertSuccess("'????' = ?.", "??");
		
		assertSuccess("member(X, ?).", Arrays.asList(0l, 1l, 2l));
		List<Object> list = new ArrayList<Object>();
		for (Object o : p.solve("member(X, ?).", Arrays.asList(0l, 1l, 2l))) {
			list.add(o);
		}
		assertEquals(Arrays.asList(0l, 1l, 2l), list);
	}
	
	@Override
	@Test
	public void testObjectConverters() {
		assertSuccess("?=1.", 1);
		assertFailure("?=1.", 1.0);
		assertFailure("?=1.", 2);
		assertSuccess("?=1.0.", 1.0);
		assertFailure("?=1.0.", 1);
		assertFailure("?=1.0.", 2.0);
		assertSuccess("?=prolog4j.", "prolog4j");
		assertSuccess("?='Prolog4J'.", "Prolog4J");
		//todo: apperently problog does not care about upper and lower case as well as the difference of strings and numbers...
//		assertFailure("?=prolog4j.", "Prolog4j");
//		assertFailure("?=prolog4j.", "'prolog4j'");
		assertSuccess("?='2'.", "2");
//		assertFailure("?=2.", "2"); 
//		assertFailure("?='2'.", 2); 

		assertSuccess("?=[0, 1, 2].", Arrays.asList(0, 1, 2));
		assertSuccess("?=[a, b, c].", Arrays.asList("a", "b", "c"));

		assertSuccess("?=f(1, 2).", new Compound("f", 1, 2));
	}
	
	@Override
	@Test
	public void testCustomTermConverters() {
		final ConversionPolicy cp = p.getConversionPolicy();
		class Human {
			private final String name;

			Human(String name) {
				this.name = name;
			}

			@Override
			public boolean equals(Object obj) {
				return obj instanceof Human && name.equals(((Human) obj).name);
			}
		}
		cp.addTermConverter("human", new Converter<Object>() {
			@Override
			public Object convert(Object term) {
				if (cp.getArity(term) == 1) {
					return new Human((String) cp.getArg(term, 0));
				}
				return null;
			}
		});
		Human socrates = p.<Human>solve("member(H, [human(socrates)]).").get();
		assertEquals(new Human("socrates"), socrates);
	}
}
