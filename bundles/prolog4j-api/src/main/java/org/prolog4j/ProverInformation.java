package org.prolog4j;

public class ProverInformation {
    private final String name;
    private final String id;
    private final boolean needsNativeExecutables;

    public ProverInformation(String id, String name, boolean needsNativeExecutables) {
        this.name = name;
        this.id = id;
        this.needsNativeExecutables = needsNativeExecutables;
    }

    public String getName() {
        return this.name;
    }

    public String getId() {
        return this.id;
    }

    public boolean needsNativeExecutables() {
        return needsNativeExecutables;
    }

}
