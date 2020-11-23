package org.prolog4j.swicli;

import java.util.Collections;
import java.util.Map;

import org.prolog4j.base.Executable;

public interface SWIPrologExecutable extends Executable {
	
	String getPath();
	
	default Map<String, String> getEnvironment() {
        return Collections.emptyMap();
    }

}
