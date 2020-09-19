package org.prolog4j.swicli;

import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ServiceScope;
import org.prolog4j.ConversionPolicy;
import org.prolog4j.IProverFactory;
import org.prolog4j.Prover;
import org.prolog4j.swicli.impl.PrologAPIWrapper;
import org.prolog4j.swicli.impl.SWIPrologCLIConversionPolicy;
import org.prolog4j.swicli.impl.SWIPrologCLIProver;

@Component(immediate = true, property = { "id=org.prolog4j.swicli.proverfactory", "name=SWI Prolog CLI Interpreter",
        "needsNativeExecutables=true" }, scope = ServiceScope.SINGLETON)
public class SWIPrologCLIProverFactory implements IProverFactory {

    protected static class PrioritizedProvider implements Comparable<PrioritizedProvider> {
        private final int priority;
        private final SWIPrologExecutableProvider provider;

        public PrioritizedProvider(int priority, SWIPrologExecutableProvider provider) {
            super();
            this.priority = priority;
            this.provider = provider;
        }

        public int getPriority() {
            return priority;
        }

        public SWIPrologExecutableProvider getProvider() {
            return provider;
        }

        @Override
        public int compareTo(PrioritizedProvider o) {
            return priority - o.priority;
        }

    }

    private final PrologAPIWrapper prologApiWrapper = new PrologAPIWrapper();
    private final SortedSet<PrioritizedProvider> executableProviders = new TreeSet<>();

    @Override
    public Prover createProver() {
        return new SWIPrologCLIProver(createConversionPolicy(), prologApiWrapper.getPrologApi(), getExecutable());
    }

    @Override
    public ConversionPolicy createConversionPolicy() {
        return new SWIPrologCLIConversionPolicy(prologApiWrapper.getPrologApi()
            .getParser());
    }
    
    protected SWIPrologExecutable getExecutable() {
        for (var executableProvider : executableProviders) {
            Optional<SWIPrologExecutable> executable = executableProvider.getProvider().getExecutable();
            if (!executable.isEmpty()) {
                return executable.get();
            }
        }
        return null;
    }
    
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addProvider(SWIPrologExecutableProvider provider, Map<Object, Object> properties) {
        var priority = properties.get(SWIPrologExecutableProvider.PRIORITY_PROPERTY);
        if (priority != null || priority instanceof Integer) {
            executableProviders.add(new PrioritizedProvider((Integer)priority, provider));
        }
    }

    public void removeProvider(SWIPrologExecutableProvider provider) {
        executableProviders.removeIf(p -> p.getProvider().equals(provider));
    }

}
