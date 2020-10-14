package org.prolog4j.base;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.palladiosimulator.supporting.prolog.api.PrologAPI;

public class Activator implements BundleActivator {

	private static Activator instance;
	private static BundleContext context;
	private ServiceReference<PrologAPI> prologApiReference;
	private PrologAPI prologApi;

	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		setInstance(this);
		Activator.context = bundleContext;
		prologApiReference = bundleContext.getServiceReference(PrologAPI.class);
        prologApi = bundleContext.getService(prologApiReference);
	}

	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
		prologApi = null;
		bundleContext.ungetService(prologApiReference);
        prologApiReference = null;
        setInstance(null);
	}
	
	public static Activator getInstance() {
        return instance;
    }
	
	 private static void setInstance(Activator instance) {
		 Activator.instance = instance;
    }

    public PrologAPI getPrologApi() {
        return prologApi;
    }

}
