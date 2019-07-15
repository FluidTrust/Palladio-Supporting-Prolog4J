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
import java.util.HashMap;
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
@Component(immediate = true)
public final class ProverManager {
	public class ProverInformation {
		private String name;

		public ProverInformation(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}
	}

	@Activate
	protected void activate() {
		List<ProverInformation> availableProvers = getAvailableProvers();
		
		for(ProverInformation pi: availableProvers) {
			System.out.println(pi.getName());
		}
	}

	private List<ProverInformation> getAvailableProvers() {
		BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();

		// Passing null as the filter argument returns all services
		// implementing the interface
		ServiceReference[] refs = null;
		try {
			refs = context.getServiceReferences(IProverFactory.class.getName(), null);
			if (null != refs) {
				List<ProverInformation> prover = new ArrayList<>();
				for (ServiceReference ref : refs) {
					String proverName = ref.getProperty("implementation").toString();
					prover.add(new ProverInformation(proverName));
				}
				return prover;
			}
		} catch (InvalidSyntaxException e) {
			System.err.print("Kacke");
		}
		return new ArrayList<>();
	}
}
