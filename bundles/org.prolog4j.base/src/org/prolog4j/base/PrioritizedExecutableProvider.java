package org.prolog4j.base;

public class PrioritizedExecutableProvider<E extends ExecutableProvider> implements Comparable<PrioritizedExecutableProvider> {
    private final int priority;
    private final E provider;

    public PrioritizedExecutableProvider(int priority, E provider) {
        super();
        this.priority = priority;
        this.provider = provider;
    }

    public int getPriority() {
        return priority;
    }

    public E getProvider() {
        return provider;
    }

    @Override
    public int compareTo(PrioritizedExecutableProvider o) {
        return priority - o.priority;
    }

}