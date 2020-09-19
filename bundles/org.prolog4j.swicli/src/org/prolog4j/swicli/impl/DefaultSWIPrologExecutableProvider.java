package org.prolog4j.swicli.impl;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.prolog4j.swicli.SWIPrologExecutable;
import org.prolog4j.swicli.SWIPrologExecutableProvider;

@Component(property = SWIPrologExecutableProvider.PRIORITY_PROPERTY + " = "
        + SWIPrologExecutableProvider.PRIORITY_LOWEST)
public class DefaultSWIPrologExecutableProvider implements SWIPrologExecutableProvider {

    @Override
    public Optional<SWIPrologExecutable> getExecutable() {
        return Optional.of(new SWIPrologExecutable() {

            @Override
            public String getPath() {
                return "swipl";
            }
        });
    }

}
