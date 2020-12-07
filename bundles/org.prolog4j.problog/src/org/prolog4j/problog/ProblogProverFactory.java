package org.prolog4j.problog;

import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.prolog4j.ConversionPolicy;
import org.prolog4j.IProverFactory;
import org.prolog4j.Prover;
import org.prolog4j.base.PrioritizedExecutableProvider;
import org.prolog4j.base.PrologAPIWrapper;
import org.prolog4j.problog.impl.ProblogConversionPolicy;
import org.prolog4j.problog.impl.ProblogProver;

@Component(immediate = true, property = { "id=org.prolog4j.problog.proverfactory", "name=ProbLog Interpreter", "needsNativeExecutables=true"})
public class ProblogProverFactory  implements IProverFactory {

	private final SortedSet<PrioritizedExecutableProvider<ProblogExecutableProvider>> executableProviders = new TreeSet<>();
	private PrologAPIWrapper apiWrapper = new PrologAPIWrapper();

	@Override
	public Prover createProver() {
		return new ProblogProver(apiWrapper, createConversionPolicy(), getExecutable());
	}

	@Override
	public ConversionPolicy createConversionPolicy() {
		return new ProblogConversionPolicy(apiWrapper.getPrologApi().getParser());
	}
	
	protected ProblogExecutable getExecutable() {
        for (var executableProvider : executableProviders) {
            Optional<ProblogExecutable> executable = executableProvider.getProvider().getExecutable();
            if (!executable.isEmpty()) {
                return executable.get();
            }
        }
        return null;
    }
	
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addProvider(ProblogExecutableProvider provider, Map<Object, Object> properties) {
        var priority = properties.get(ProblogExecutableProvider.PRIORITY_PROPERTY);
        if (priority != null) {
            int priorityNumeric = ProblogExecutableProvider.PRIORITY_LOWEST; 
            if (priority instanceof Integer) {
                priorityNumeric = (Integer)priority;
            } else if (priority instanceof String) {
                try {
                    priorityNumeric = Integer.parseInt((String)priority);                    
                } catch (NumberFormatException e) {
                    // just ignore it and use default priority
                }
            }
            executableProviders.add(new PrioritizedExecutableProvider<ProblogExecutableProvider>(priorityNumeric, provider));
        }
    }

    public void removeProvider(ProblogExecutableProvider provider) {
        executableProviders.removeIf(p -> p.getProvider().equals(provider));
    }
}
