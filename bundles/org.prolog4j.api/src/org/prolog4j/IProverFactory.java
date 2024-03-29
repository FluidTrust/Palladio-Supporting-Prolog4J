/* 
 * Copyright (c) 2010 Miklos Espak
 * Copyright (c) 2004-2007 QOS.ch
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

import java.util.Collections;
import java.util.Map;

/**
 * <code>IProverFactory</code> instances manufacture {@link Prover} instances by
 * name.
 * <p>
 * Most users retrieve {@link Prover} instances through the static
 * {@link ProverManager#getProver(String)} method. An instance of this
 * interface is bound internally with {@link ProverManager} class at compile
 * time.
 */
public interface IProverFactory {

	/**
	 * Creates a new prover.
	 * 
	 * @return a new prover
	 * @throws ProverCreationException In case of an error while creating a prover
	 */
	default Prover createProver() throws ProverCreationException {
	    return createProver(Collections.emptyMap());
	}
	
	/**
	 * Creates a new prover using a set of given parameters.
	 * @param parameters The parameters to be used to create a prover.
	 * @return a new prover
	 * @throws ProverCreationException In case of an error while creating a prover
	 */
	public Prover createProver(Map<Object, Object> parameters) throws ProverCreationException;
	
	/**
	 * Creates a new conversion policy.
	 * 
	 * @return the created conversion policy
	 */
	public ConversionPolicy createConversionPolicy();
}
