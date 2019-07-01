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
package org.prolog4j.projog;

import org.projog.api.Projog;
import org.projog.api.QueryResult;
import org.projog.api.QueryStatement;
import org.projog.core.term.Term;
import org.prolog4j.ConversionPolicy;
import org.prolog4j.InvalidQueryException;
import org.prolog4j.Query;
import org.prolog4j.Solution;

/**
 * The tuProlog implementation of the Query class.
 */
public class ProjogQuery extends Query {
	
	/** The projog prover used to process this query. */
	private final ProjogProver prover;
	
	/** The conversion policy of the prover that is used for solving this query. */
	private final ConversionPolicy cp;
	
	/** The projog engine used to process this query. */
	private final Projog engine;

	/** The projog representation of the goal to be solved. */
	private QueryResult goal;
	
	/**
	 * Creates a TuProlog query object.
	 * 
	 * @param prover the tuProlog prover to process the query
	 * @param goal the Prolog goal to be solved
	 */
	ProjogQuery(ProjogProver prover, String goal) {
		super(goal);
		this.prover = prover;
		this.cp = prover.getConversionPolicy();
		this.engine = prover.getEngine();
	}

	@Override
	public <A> Solution<A> solve(Object... actualArgs) {
		if(actualArgs.length != getPlaceholderNames().size()) {
			throw new InvalidQueryException(getGoal());
		}
		
		QueryStatement qs =  engine.query(getGoal());
		this.goal = qs.getResult();
		for(int i = 0; i < actualArgs.length; i++) {
			this.goal.setTerm(getPlaceholderNames().get(i), (Term) cp.convertObject(actualArgs[i]));
		}
		return new ProjogSolution<A>(prover, this.goal);
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
