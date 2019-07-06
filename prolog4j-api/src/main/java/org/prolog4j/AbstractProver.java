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
package org.prolog4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

/**
 * Serves as base class for prover implementation.
 */
public abstract class AbstractProver implements Prover, Serializable {

	private static final long serialVersionUID = 1L;

	@Override
	public final <A> Solution<A> solve(String goal, Object... actualArgs) {
		return query(goal).solve(actualArgs);
	}

	@Override
	public void assertz(String fact, Object... args) {
		Query q = query("assertz(" + fact.substring(0, fact.lastIndexOf('.')) + ").");
		q.solve(args);
	}

	@Override
	public void retract(String fact) {
		int lastDot = fact.lastIndexOf('.');
		int length = fact.length();
		if (lastDot == -1 || fact.substring(lastDot, length).trim().length() > 1) {
			lastDot = length;
		}
		query("retract(" + fact.substring(0, lastDot) + ").").solve();
	}

	@Override
	public void loadTheory(InputStream input) throws IOException {
		StringBuilder inputString = new StringBuilder();

		int i;
		while ((i = input.read()) != -1) {
			inputString.append((char) i);
		}

		String[] inputLines = inputString.toString().split(System.lineSeparator());
		addTheory(inputLines);
	}

	/** The default conversion policy used by the current implementation. */
	private static final ConversionPolicy GLOBAL_POLICY = ProverFactory.getConversionPolicy();

	/** The conversion policy of the prover. */
	private ConversionPolicy conversionPolicy = new LazyConversionPolicy();

	@Override
	public ConversionPolicy getConversionPolicy() {
		return conversionPolicy;
	}

	@Override
	public void setConversionPolicy(ConversionPolicy conversionPolicy) {
		this.conversionPolicy = conversionPolicy;
	}

	/**
	 * By default this policy delegates method calls to the global policy. At the
	 * first time when the policy is customized (a converter is added), it creates a
	 * new policy, and it will delegate the subsequent calls to this new policy.
	 */
	private class LazyConversionPolicy extends ConversionPolicy {

		/**
		 * The conversion requests will be delegated to this policy. Its value is the
		 * global policy by default.
		 */
		private ConversionPolicy delegate = GLOBAL_POLICY;

		@Override
		public <T> void addObjectConverter(Class<T> pattern, Converter<T> converter) {
			if (delegate == GLOBAL_POLICY) {
				delegate = ProverFactory.createConversionPolicy();
			}
			delegate.addObjectConverter(pattern, converter);
		}

		@Override
		public <T> void addListConverter(Class<T> pattern, Converter<List<?>> converter) {
			if (delegate == GLOBAL_POLICY) {
				delegate = ProverFactory.createConversionPolicy();
			}
			delegate.addListConverter(pattern, converter);
		}

		@Override
		public void addTermConverter(String pattern, Converter<Object> converter) {
			if (delegate == GLOBAL_POLICY) {
				delegate = ProverFactory.createConversionPolicy();
			}
			delegate.addTermConverter(pattern, converter);
		}

		@Override
		public Object convertObject(Object object) {
			return delegate.convertObject(object);
		}

		@Override
		public Object convertTerm(Object term) {
			return delegate.convertTerm(term);
		}

		@Override
		public <U, T> T convertTerm(U term, java.lang.Class<T> type) {
			return delegate.convertTerm(term, type);
		}

		@Override
		public boolean match(Object term1, Object term2) {
			return delegate.match(term1, term2);
		}

		@Override
		public Object term(int value) {
			return delegate.term(value);
		}

		@Override
		public Object term(double value) {
			return delegate.term(value);
		}

		@Override
		public Object term(String name) {
			return delegate.term(name);
		}

		@Override
		public Object term(String name, Object... args) {
			return delegate.term(name, args);
		}

		@Override
		public int intValue(Object term) {
			return delegate.intValue(term);
		}

		@Override
		public double doubleValue(Object term) {
			return delegate.doubleValue(term);
		}

		@Override
		protected String getName(Object compound) {
			return delegate.getName(compound);
		}

		@Override
		protected int getArity(Object compound) {
			return delegate.getArity(compound);
		}

		@Override
		protected Object getArg(Object compound, int index) {
			return delegate.getArg(compound, index);
		}

		@Override
		public boolean isAtom(Object term) {
			return delegate.isAtom(term);
		}

		@Override
		public boolean isCompound(Object term) {
			return delegate.isCompound(term);
		}

		@Override
		public boolean isDouble(Object term) {
			return delegate.isDouble(term);
		}

		@Override
		public boolean isInteger(Object term) {
			return delegate.isInteger(term);
		}
	}
}
