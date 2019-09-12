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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.projog.api.QueryResult;
import org.projog.core.ProjogException;
import org.projog.core.term.Term;
import org.prolog4j.ConversionPolicy;
import org.prolog4j.Solution;
import org.prolog4j.SolutionIterator;
import org.prolog4j.UnknownVariableException;

/**
 * The <tt>Solution</tt> class is responsible for traversing through the
 * solutions of a query.
 * 
 * @param <S> the type of the values of the variable that is of special interest
 */
public class ProjogSolution<S> extends Solution<S> {

	/**
	 * The conversion policy of the tuProlog prover that is used for solving this
	 * query.
	 */
	private final ConversionPolicy cp;

	/** This object provides the bindings for one solution of the query. */
	private QueryResult solution;

	/** True if the query has a solution, otherwise false. */
	private boolean success;

	/**
	 * Creates an object, using which the solutions of a query can be accessed.
	 * 
	 * @param prover the tuProlog prover
	 * @param goal   the goal to be solved
	 */
	ProjogSolution(ProjogProver prover, QueryResult goal) {
		this.cp = prover.getConversionPolicy();
		this.solution = goal;

		try {
			this.success = goal.next();
		} catch (ProjogException e) {
			this.success = false;
			return;
		}
		
		List<String> variableIds = new ArrayList<String>(goal.getVariableIds());
		if (variableIds.size() > 0) {
			on(variableIds.get(variableIds.size()-1));
		}
	}

	@Override
	public boolean isSuccess() {
		return success;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <A> A get(String variable) {
		if (clazz == null) {
			Term term;
			try {
				term = solution.getTerm(variable);
			} catch (ProjogException e) {
				throw new UnknownVariableException(variable);
			}
			return (A) cp.convertTerm(term);
		}
		return (A) get(variable, clazz);
	}

	@Override
	public <A> A get(String variable, Class<A> type) {
		try {
			Term term;
			try {
				term = solution.getTerm(variable);
			} catch (ProjogException e) {
				throw new UnknownVariableException(variable);
			}
			return cp.convertTerm(term, type);
		} catch (ProjogException e) {
			throw new NoSuchElementException();
		}
	}

	@Override
	public void collect(Collection<?>... collections) {
		SolutionIterator<S> it = iterator();
		Iterator<String> variables = solution.getVariableIds().iterator();

		while (it.hasNext() && variables.hasNext()) {
			it.next();
			for (int i = 0; i < collections.length; ++i) {
				collections[i].add(it.get(variables.next()));
			}
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List<?>[] toLists() {
		List<?>[] lists = new List<?>[solution.getVariableIds().size() - 1];
		for (int i = 0; i < lists.length; ++i) {
			lists[i] = new ArrayList();
		}
		collect(lists);
		return lists;
	}

	@Override
	protected boolean fetch() {
		return solution.next();
	}

}
