package org.prolog4j.base;

import java.util.Collections;
import java.util.Map;

public interface Executable {
    
    default Map<String, String> getEnvironment() {
        return Collections.emptyMap();
    }

}
