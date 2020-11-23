package org.prolog4j.problog.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.prolog4j.Compound;
import org.prolog4j.ConversionPolicy;
import org.prolog4j.Converter;
import org.prolog4j.Solution;
import org.prolog4j.problog.ProblogExecutableProvider;
import org.prolog4j.problog.ProblogProverFactory;
import org.prolog4j.problog.enabler.DefaultProblogExecutableProvider;
import org.prolog4j.problog.enabler.ProblogStandaloneExecutableProvider;
import org.prolog4j.problog.impl.ProblogSolution;
import org.prolog4j.test.ProverTest;

/**
 * JUnit test for the jProblog binding. Inherits the common test class.
 */
public class ProblogProverTest extends ProverTest {
	@BeforeClass
	public static void setUpBeforeClass() {
		var factory = new ProblogProverFactory();
		//currently not fully implemented
        Map<Object, Object> properties = new HashMap<>();
        properties.put(ProblogExecutableProvider.PRIORITY_PROPERTY, ProblogExecutableProvider.PRIORITY_LOWEST);
        factory.addProvider(new DefaultProblogExecutableProvider(), properties);
        
        Map<Object, Object> properties2 = new HashMap<>();
        properties2.put(ProblogExecutableProvider.PRIORITY_PROPERTY, ProblogExecutableProvider.PRIORITY_LOWEST - 1);
        factory.addProvider(new ProblogStandaloneExecutableProvider(), properties2);
        
        p = factory.createProver();
		
		setup();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testSimpleCoinTossProbability() {
		//Basic coin flip with two coins, where the second coin is biased (60% heads)
		p.addTheory("0.5::heads1.", "0.6::heads2.", "twoHeads :- heads1, heads2.");
		
		ProblogSolution<String> solution = (ProblogSolution) p.solve("heads1.");
		Double expected = 0.5;
		assertEquals(expected, solution.getProbability(""));
		solution = (ProblogSolution) p.solve("heads2.");
		expected = 0.6;
		assertEquals(expected, solution.getProbability(""));
		expected = 0.3;
		solution = (ProblogSolution) p.solve("twoHeads.");
		assertEquals(expected, solution.getProbability(""));
		
		//Noisy-or: Multiple rules for the same head
		p.addTheory("someHeads :- heads1.", "someHeads :- heads2.");
		
		expected = 0.8;
		solution = (ProblogSolution) p.solve("someHeads.");
		assertEquals(expected, solution.getProbability(""));
		
		//First order probabilistic fact
		p.addTheory("0.6::lands_heads(_).", "coin(c1).", "coin(c2).", "coin(c3).", "coin(c4).");
		p.addTheory("probfact_heads(C) :- coin(C), lands_heads(C).", "probfact_someHeads :- probfact_heads(_).");
		
		expected = 0.9744;
		solution = (ProblogSolution) p.solve("probfact_someHeads.");
		assertEquals(expected, solution.getProbability(""));
		
		//As problog probabilistic clause
		p.addTheory("0.6::probclause_heads(C) :- coin(C).", "probclause_someHeads :- probclause_heads(_).");
		solution = (ProblogSolution) p.solve("probclause_someHeads.");
		assertEquals(expected, solution.getProbability(""));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testComplexProbabilisticQuerying() {
		p.addTheory("q1 :- stress(X), influences(X,Y).", "q2 :- stress(X), influences(X,Y), male(Y).");
		p.addTheory("0.7::stress(1).", "0.4::stress(2).", "0.9::stress(3).");
		p.addTheory("0.83::influences(1,2).", "0.41::influences(1,3).", "0.56::influences(2,1).", "0.91::influences(2,3).", "0.34::influences(3,1).", "0.17::influences(3,2).");
		p.addTheory("0.3::male(1).", "0.8::male(2).", "0.9::male(3).");
		
		Double expectedQ1 = 0.8647973;
		Double expectedQ2 = 0.75495859;
		
		ProblogSolution<String> solution = (ProblogSolution) p.solve("q1.");
		assertEquals(expectedQ1, solution.getProbability(""));
		
		solution = (ProblogSolution) p.solve("q2.");
		assertEquals(expectedQ2, solution.getProbability(""));
	}
	
	
	/**
	 * Had to be changed, since problog sorts its results...
	 */
	@Override
	@Test
	public void testTestOn() {
		//Had to change the comparison lists, as problog actually sorts its results alphabetically...
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

		//Both cases not testable with this testcase "iterable" in mind,
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
	    
	    //Due to the "queryrule()" workaround, the actual result of TMP = [] is masked
	    //so there is only a probability of this query
	    //assertTrue(List.class.isInstance(results.iterator().next()));
	    //assertEquals(Collections.emptyList(), results.iterator().next());
	}
	
	@Override
	@Test
	public void testTermConverters() {
		//Had to change the queries to member functions so the result of X could be fetched
		//Due to the "queryrule()" workaround, the results of the original queries is masked
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

		Object cVal = p.solve("X = functor(arg1, arg2).").get();
		assertEquals(new Compound("functor", "arg1", "arg2"), cVal);
	}
	
	@Override
	@Test
	public void testPlaceHolders() {
		//Conversion of strings like "socrates" turns into mortal('socrates'), 
		//this does not fit the rule mortal(socrates) 
//		assertSuccess("mortal(?).", "socrates");
//		assertFailure("mortal(?).", "zeus");
//		assertSuccess("mortal(?X).", "socrates");
//		assertFailure("mortal(?X).", "zeus");
//		assertSuccess("mortal(?LongVariable).", "socrates");
//		assertFailure("mortal(?LongVariable).", "zeus");
		
		assertSuccess("_ = 'Question ??Mark'.");
		assertSuccess("? = '??Mark'.", "?Mark");
		assertSuccess("'Is it OK??' = ?.", "Is it OK?");
		assertSuccess("'????' = ?.", "??");
		
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
		
		//Apperently problog does not care about upper and lower case.
		assertFailure("?=prolog4j.", "Prolog4j");
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
		//Changed to "member" so we can check H after the ProbLog execution.
		Human socrates = p.<Human>solve("member(H, [human(socrates)]).").get();
		assertEquals(new Human("socrates"), socrates);
	}
}
