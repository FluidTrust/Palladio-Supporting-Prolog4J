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

/**
 * Serves as base class for prover implementation.
 */
public abstract class AbstractProver implements Prover, Serializable {

	private static final long serialVersionUID = 1L;
	
	/** The conversion policy of the prover. */
	private final ConversionPolicy conversionPolicy;
	
	public AbstractProver(ConversionPolicy conversionPolicy) {
		this.conversionPolicy = conversionPolicy;
	}

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

		loadTheory(inputString.toString());
	}
	
	@Override
    public void loadTheory(String input) {
        String[] inputLines = input.split(System.lineSeparator());
        addTheory(inputLines);
    }

    @Override
	public ConversionPolicy getConversionPolicy() {
		return this.conversionPolicy;
	}
	
}
