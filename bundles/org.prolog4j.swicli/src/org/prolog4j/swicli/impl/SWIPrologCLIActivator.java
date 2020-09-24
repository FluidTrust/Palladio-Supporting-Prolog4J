package org.prolog4j.swicli.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.palladiosimulator.supporting.prolog.api.PrologAPI;

public class SWIPrologCLIActivator implements BundleActivator {

    private static SWIPrologCLIActivator instance;
    private ServiceReference<PrologAPI> prologApiReference;
    private PrologAPI prologApi;
    
    @Override
    public void start(BundleContext ctx) throws Exception {
        setInstance(this);
        prologApiReference = ctx.getServiceReference(PrologAPI.class);
        prologApi = ctx.getService(prologApiReference);
    }

    @Override
    public void stop(BundleContext ctx) throws Exception {
        prologApi = null;
        ctx.ungetService(prologApiReference);
        prologApiReference = null;
        setInstance(null);
    }

    public static SWIPrologCLIActivator getInstance() {
        return instance;
    }

    private static void setInstance(SWIPrologCLIActivator instance) {
        SWIPrologCLIActivator.instance = instance;
    }

    public PrologAPI getPrologApi() {
        return prologApi;
    }

}
