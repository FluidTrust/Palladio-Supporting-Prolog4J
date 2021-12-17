package org.prolog4j.swicli;

import java.util.Collections;
import java.util.Map;

public interface SWIPrologExecutable {

    String getPath();
    
    Map<Object, Object> getParameters();
    
    default Map<String, String> getEnvironment() {
        return Collections.emptyMap();
    }
    
}
