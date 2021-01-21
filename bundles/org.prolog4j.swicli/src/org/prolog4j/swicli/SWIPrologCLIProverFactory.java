package org.prolog4j.swicli;

import java.util.HashMap;
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
import org.prolog4j.base.MetaModelBasedConversionPolicy;
import org.prolog4j.base.PrioritizedExecutableProvider;
import org.prolog4j.base.PrologAPIWrapper;
import org.prolog4j.swicli.impl.SWIPrologCLIProver;

@Component(property = { "id=org.prolog4j.swicli.proverfactory", "name=SWI Prolog CLI Interpreter",
        "needsNativeExecutables=true" }, scope = ServiceScope.SINGLETON)
public class SWIPrologCLIProverFactory implements IProverFactory {

    public static class SWIPrologExecutableProviderStandalone {
        private final Map<Object, Object> properties = new HashMap<>();
        private final SWIPrologExecutableProvider provider;
        
        public SWIPrologExecutableProviderStandalone(SWIPrologExecutableProvider provider, int priority) {
            this.provider = provider;
            this.properties.put(SWIPrologExecutableProvider.PRIORITY_PROPERTY, priority);
        }
        
        public void addProperty(Object key, Object value) {
            this.properties.put(key, value);
        }

        public Map<Object, Object> getProperties() {
            return properties;
        }

        public SWIPrologExecutableProvider getProvider() {
            return provider;
        }
    }

    private final PrologAPIWrapper prologApiWrapper = new PrologAPIWrapper();
    private final SortedSet<PrioritizedExecutableProvider<SWIPrologExecutableProvider>> executableProviders = new TreeSet<>();

    /**
     * Default constructor to be used by OSGi.
     */
    public SWIPrologCLIProverFactory() {
        // intentionally left empty
    }
    
    /**
     * Constructor to be used in standalone scenarios without OSGi.
     * @param providers An iterable of providers to be used for initialization.
     */
    public SWIPrologCLIProverFactory(Iterable<SWIPrologExecutableProviderStandalone> providers) {
        for (var provider : providers) {
            addProvider(provider.getProvider(), provider.getProperties());
        }
    }
    
    @Override
    public Prover createProver() {
        return new SWIPrologCLIProver(createConversionPolicy(), prologApiWrapper, getExecutable());
    }

    @Override
    public ConversionPolicy createConversionPolicy() {
        return new MetaModelBasedConversionPolicy(prologApiWrapper.getPrologApi()
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
        if (priority != null) {
            int priorityNumeric = SWIPrologExecutableProvider.PRIORITY_LOWEST; 
            if (priority instanceof Integer) {
                priorityNumeric = (Integer)priority;
            } else if (priority instanceof String) {
                try {
                    priorityNumeric = Integer.parseInt((String)priority);                    
                } catch (NumberFormatException e) {
                    // just ignore it and use default priority
                }
            }
            executableProviders.add(new PrioritizedExecutableProvider<SWIPrologExecutableProvider>(priorityNumeric, provider));
        }
    }

    public void removeProvider(SWIPrologExecutableProvider provider) {
        executableProviders.removeIf(p -> p.getProvider().equals(provider));
    }

}
