package org.prolog4j.swicli.impl;

import org.palladiosimulator.supporting.prolog.api.PrologAPI;
import org.palladiosimulator.supporting.prolog.api.impl.PrologAPIImpl;

public class PrologAPIWrapper {

    private final PrologAPI prologApi;

    public PrologAPIWrapper() {
        if (SWIPrologCLIActivator.getInstance() != null) {
            prologApi = SWIPrologCLIActivator.getInstance().getPrologApi();
        } else {
            prologApi = new PrologAPIImpl();
        }
    }

    public PrologAPI getPrologApi() {
        return prologApi;
    }
    
}
