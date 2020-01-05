/* 
 * Copyright (c) 2010 Miklos Espak
 * All rights reserved.
 * 
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 * 
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 * 
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.prolog4j.swi.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.jpl7.PrologException;
import org.jpl7.Term;
import org.jpl7.Util;
import org.prolog4j.ConversionPolicy;
import org.prolog4j.InvalidQueryException;
import org.prolog4j.Query;
import org.prolog4j.Solution;

/**
 * The tuProlog implementation of the Query class.
 */
public class SWIPrologQuery extends Query {
	
	/** The tuProlog prover used to process this query. */
	private final SWIPrologProver prover;
	
	/** The conversion policy of the prover that is used for solving this query. */
	private final ConversionPolicy cp;
	
	private String myGoal;
	private Term swiGoal;
	
	/**
	 * Creates a SWI-Prolog query object.
	 * 
	 * @param prover the SWI-Prolog prover to process the query
	 * @param goal the SWI-Prolog goal to be solved
	 */
	SWIPrologQuery(SWIPrologProver prover, String goal) {
		super(goal);
		this.prover = prover;
		this.cp = prover.getConversionPolicy();
		
		myGoal = getGoal();
		for(String ph: getPlaceholderNames()) {
			// Replace variables of form '?variablename' with ' ? '
			// Because Util.textParamsToTerm needs it that way
			Pattern finder = Pattern.compile("^"+ph+"|[^a-zA-Z0-9]"+ph+"[^a-zA-Z0-9]|"+ph+"$");
			Matcher m = finder.matcher(myGoal);
			if(m.find()) {
				String s = m.group();
				String ns = s.replace(ph, " ? ");
				myGoal = myGoal.replace(s, ns);
			}
		}
	}
	

	@Override
	public <A> Solution<A> solve(Object... actualArgs) {
		Term[] argTerms = Stream.of(actualArgs)
				.map(arg -> (Term)cp.convertObject(arg))
				.toArray(Term[]::new);
		
		try {
			swiGoal = Util.textParamsToTerm(myGoal, argTerms);
		} catch (PrologException exc) {
			throw new InvalidQueryException(getGoal());
		}
		return new SWIPrologSolution<A>(prover, swiGoal);
	}

	@Override
	public Query bind(int argument, Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Query bind(String variable, Object value) {
		throw new UnsupportedOperationException();
	}

}
