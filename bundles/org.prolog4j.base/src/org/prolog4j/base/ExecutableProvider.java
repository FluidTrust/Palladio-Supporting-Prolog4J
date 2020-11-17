package org.prolog4j.base;

import java.util.Optional;

/**
 * Interface for providers of a prolog executable.
 * 
 * OSGi services have to provide the priority property {@link #PRIORITY_PROPERTY}. The lower the
 * value, the higher the priority is. The lowest priority is {@link #PRIORITY_LOWEST}.
 */
public interface ExecutableProvider<E extends Executable> {
	
	public static final String PRIORITY_PROPERTY = "priority";
    public static final int PRIORITY_LOWEST = 999;

    Optional<E> getExecutable();

}
