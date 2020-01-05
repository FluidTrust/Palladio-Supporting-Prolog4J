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
package org.prolog4j.manager.impl;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.prolog4j.IProverFactory;
import org.prolog4j.ProverInformation;
import org.prolog4j.manager.IProverManager;

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
public final class ProverManager implements IProverManager {

    private final BidiMap<ProverInformation, IProverFactory> availableProvers = new DualHashBidiMap<>();

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void bindProverFactory(IProverFactory factory, Map<String, String> serviceProperties) {
        String proverId = serviceProperties.get("id");
        String proverName = serviceProperties.get("name");
        boolean proverNativeExecutables = Optional.ofNullable(serviceProperties.get("needsNativeExecutables"))
            .map(Boolean::parseBoolean)
            .orElse(false);
        availableProvers.put(new ProverInformation(proverId, proverName, proverNativeExecutables), factory);
    }

    public void unbindProverFactory(IProverFactory factory) {
        availableProvers.removeValue(factory);
    }

    public void updatedProverFactory(IProverFactory factory, Map<String, String> serviceProperties) {
        unbindProverFactory(factory);
        bindProverFactory(factory, serviceProperties);
    }

    public Map<ProverInformation, IProverFactory> getProvers() {
        return Collections.unmodifiableMap(availableProvers);
    }

}
