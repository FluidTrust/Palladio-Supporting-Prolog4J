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
package org.prolog4j.tuprolog.impl;

import java.io.IOException;
import java.io.InputStream;

import org.prolog4j.AbstractProver;
import org.prolog4j.ConversionPolicy;
import org.prolog4j.Query;
import org.prolog4j.tuprolog.impl.libraries.ListsLibrary;

import alice.tuprolog.InvalidLibraryException;
import alice.tuprolog.InvalidTheoryException;
import alice.tuprolog.Prolog;
import alice.tuprolog.Theory;
import alice.tuprolog.event.ExceptionListener;
import alice.tuprolog.event.WarningListener;

/**
 * Represents a Prolog knowledge base and provides methods for solving queries
 * on it. The prover itself is not responsible for processing the solutions.
 */
public class TuPrologProver extends AbstractProver {

	/** Class version for serialization. */
	private static final long serialVersionUID = 1L;

	/**
	 * The tuProlog engine that is used for storing the knowledge base and
	 * solving queries on it.
	 */
	private final Prolog engine;

	/**
	 * Creates a tuProlog prover.
	 */
	public TuPrologProver(ConversionPolicy conversionPolicy) {
		super(conversionPolicy);
		engine = new Prolog();
		loadDefaultLibraries();
	}

    protected void loadDefaultLibraries() {
        loadLibrary(ListsLibrary.class.getName());
    }

	/**
	 * Returns the tuProlog engine used by the prover.
	 * 
	 * @return the tuProlog engine
	 */
	public Prolog getEngine() {
		return engine;
	}

	@Override
	public Query query(String goal) {
		return new TuPrologQuery(this, goal);
	}

	@Override
	public void loadLibrary(String className) {
		try {
			engine.loadLibrary(className);
		} catch (InvalidLibraryException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void loadTheory(InputStream input) throws IOException {
		try {
			engine.addTheory(new Theory(input));
		} catch (InvalidTheoryException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void addTheory(String theory) {
		try {
			ExceptionListener el = e -> System.err.println(e.getSource() + " | " + e.getMsg());
			WarningListener wl = w -> System.err.println(w.getSource() + " | " + w.getMsg());
			engine.addExceptionListener(el);
			engine.addWarningListener(wl);
			engine.addTheory(new Theory(theory));
			engine.removeExceptionListener(el);
			engine.removeWarningListener(wl);
		} catch (InvalidTheoryException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void addTheory(String... theory) {
		StringBuilder sb = new StringBuilder();
		for (String factOrRule : theory) {
			sb.append(factOrRule).append('\n');
		}
		try {
			ExceptionListener el = e -> System.err.println(e.getSource() + " | " + e.getMsg());
			WarningListener wl = w -> System.err.println(w.getSource() + " | " + w.getMsg());
			engine.addExceptionListener(el);
			engine.addWarningListener(wl);
			engine.addTheory(new Theory(sb.toString()));
			engine.removeExceptionListener(el);
			engine.removeWarningListener(wl);
		} catch (InvalidTheoryException e) {
			e.printStackTrace();
		}
	}

}
