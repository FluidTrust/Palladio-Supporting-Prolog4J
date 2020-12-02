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
    public static final int PRIORITY_LOWEST = 999; //TODO: rename to "highest"

    /**
     * Returns an Optional with an Executable.
     * 
     * The provider is REQUIRED to check whether or not the Executable can run on the system (dependencies etc.)
     * before an Executable is returned, if not Optional.empty() needs to be returned.
     * 
     * The Executable should be created in this method and not the constructor of the provider.
     * 
     * Multiple providers with different priorities are be registered to one ProverFactory and are iterated.
     * A providers getExecutable method may not be called if a higher priority provider can create an Executable.
     * This saves time as no specific system checks and object creation needs to be done!
     * 
     * @return 	Optional.of(new E) 	if an E can run on the system
     * 			Optional.empty() 	if the system requirements are not met
     */
    Optional<E> getExecutable();

}
