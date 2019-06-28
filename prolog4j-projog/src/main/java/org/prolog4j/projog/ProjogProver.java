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

import java.io.IOException;
import java.io.InputStream;

import org.prolog4j.AbstractProver;
import org.prolog4j.Query;

import org.projog.api.Projog;
import org.projog.api.QueryStatement;

/**
 * Represents a Prolog knowledge base and provides methods for solving queries
 * on it. The prover itself is not responsible for processing the solutions.
 */
public class ProjogProver extends AbstractProver {

	/** Class version for serialization. */
	private static final long serialVersionUID = 1L;

	/**
	 * The projog engine that is used for storing the knowledge base and solving
	 * queries on it.
	 */
	private final Projog engine;

	/**
	 * Creates a projog prover.
	 */
	ProjogProver() {
		super();
		engine = new Projog();
	}

	/**
	 * Returns the projog engine used by the prover.
	 * 
	 * @return the projog engine
	 */
	public Projog getEngine() {
		return engine;
	}

	@Override
	public Query query(String goal) {
		return new ProjogQuery(this, goal);
	}

	@Override
	public void loadLibrary(String className) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void loadTheory(InputStream input) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addTheory(String theory) {
		QueryStatement q = engine.query("assertz(" + theory + ").");
		q.getResult();
	}

	@Override
	public void addTheory(String... theory) {
		for(int i = 0;i < theory.length;i++) {
			addTheory(theory[i]);
		}
	}

}
