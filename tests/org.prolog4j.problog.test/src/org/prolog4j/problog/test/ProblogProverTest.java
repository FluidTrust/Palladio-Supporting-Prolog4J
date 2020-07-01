package org.prolog4j.problog.test;

import org.junit.BeforeClass;
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

}
