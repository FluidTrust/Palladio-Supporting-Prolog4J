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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * The <code>ProverFactory</code> is a utility class producing Provers for
 * various Prolog APIs, most notably for tuProlog, jTrolog and JLog.
 * <p>
 * <code>ProverFactory</code> is essentially a wrapper around an
 * {@link IProverFactory} instance bound with <code>ProverFactory</code> at
 * compile time.
 * <p>
 * Please note that all methods in <code>ProverFactory</code> are static.
 */
@SuppressWarnings("deprecation")
@Component(immediate = true)
public final class ProverManager {
	private List<ProverInformation> availableProvers = new ArrayList<>();

	@Activate
	protected void activate() {
		loadAvailableProvers();
		printAvailableProvers();
	}

	private void printAvailableProvers() {
		availableProvers.stream().forEach(System.out::println);
	}

	private void loadAvailableProvers() {
		BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();

		ServiceReference[] refs = null;
		try {
			refs = context.getServiceReferences(IProverFactory.class.getName(), null);
			if (null != refs) {
				List<ProverInformation> loadedProvers = new ArrayList<>();
				for (ServiceReference ref : refs) {
					String proverId = ref.getProperty("id").toString();
					String proverName = ref.getProperty("name").toString();

					loadedProvers.add(new ProverInformation(proverId, proverName));
				}

				this.availableProvers.clear();
				this.availableProvers.addAll(loadedProvers);
			}
		} catch (InvalidSyntaxException e) {
			System.err.print("Error while loading available provers");
		}
	}

	public Prover createProver(String proverId) {
		BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();

		String filter = "(id=" + proverId + ")";

		ServiceReference[] refs = null;
		try {
			refs = context.getServiceReferences(IProverFactory.class.getName(), filter);
			if (refs == null) {
				throw new IllegalArgumentException("Unknown proverId");
			}
			return ((IProverFactory) context.getService(refs[0])).createProver();
		} catch (InvalidSyntaxException e) {
			throw new IllegalArgumentException("Unknown proverId");
		}
	}

	public List<ProverInformation> getAvailableProvers() {
		return Collections.unmodifiableList(this.availableProvers);
	}
}
