package org.prolog4j.swicli.enabler;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.prolog4j.swicli.SWIPrologExecutable;
import org.prolog4j.swicli.SWIPrologExecutableProvider;

@Component(property = SWIPrologExecutableProvider.PRIORITY_PROPERTY + " = "
        + SWIPrologExecutableProvider.PRIORITY_LOWEST)
public class DefaultSWIPrologExecutableProvider implements SWIPrologExecutableProvider {

    @Override
    public Optional<SWIPrologExecutable> getExecutable() {
    	
    	if(isSwiplInstalled()) {
    		return Optional.of(new DefaultSWIPrologExecutable());
    	} else {
    		return Optional.empty();
    	}
    }
    
    protected boolean isSwiplInstalled() {
        try {
            if (Runtime.getRuntime()
                .exec(DefaultSWIPrologExecutable.DEFAULT_SWI_CALL + " --version")
                .exitValue() == 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

}
