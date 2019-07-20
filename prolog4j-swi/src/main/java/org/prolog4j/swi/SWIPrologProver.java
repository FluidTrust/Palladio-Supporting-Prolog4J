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
package org.prolog4j.swi;

import org.prolog4j.AbstractProver;
import org.prolog4j.ConversionPolicy;
import org.prolog4j.Query;

/**
 * Represents a Prolog knowledge base and provides methods for solving queries
 * on it. The prover itself is not responsible for processing the solutions.
 */
public class SWIPrologProver extends AbstractProver {

	/** Class version for serialization. */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates an SWI-Prolog prover.
	 */
	SWIPrologProver(ConversionPolicy conversionPolicy) {
		super(conversionPolicy);
	}

	@Override
	public Query query(String goal) {
		return new SWIPrologQuery(this, goal);
	}

	@Override
	public void loadLibrary(String className) {
		throw new UnsupportedOperationException();
	}

	public void loadTheory(String filename) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addTheory(String theory) {
		org.jpl7.Query query = new org.jpl7.Query("assertz", new org.jpl7.Term[]{org.jpl7.Util.textToTerm(theory)});
		query.hasSolution();
	}

	@Override
	public void addTheory(String... theory) {
		for (String clause: theory) {
			addTheory(clause);
		}
	}

}
