package org.prolog4j.swicli;

import java.util.Optional;

/**
 * Interface for providers of the SWI prolog executable.
 * 
 * OSGi services have to provide the priority property {@link #PRIORITY_PROPERTY}. The lower the
 * value, the higher the priority is. The lowest priority is {@link #PRIORITY_LOWEST}.
 */
public interface SWIPrologExecutableProvider {

    public static final String PRIORITY_PROPERTY = "priority";
    public static final int PRIORITY_LOWEST = 999;

    Optional<SWIPrologExecutable> getExecutable();

}
