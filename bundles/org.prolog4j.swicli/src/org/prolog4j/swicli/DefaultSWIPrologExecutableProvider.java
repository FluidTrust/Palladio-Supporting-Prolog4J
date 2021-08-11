package org.prolog4j.swicli;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.Arrays;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

@Component(scope = ServiceScope.SINGLETON, property = SWIPrologExecutableProvider.PRIORITY_PROPERTY + " = "
        + (SWIPrologExecutableProvider.PRIORITY_LOWEST / 2))
public class DefaultSWIPrologExecutableProvider implements SWIPrologExecutableProvider {

    private volatile Optional<SWIPrologExecutable> executable;

    @Override
    public Optional<SWIPrologExecutable> getExecutable() {
        var localExecutable = executable;
        if (localExecutable == null) {
            synchronized (this) {
                localExecutable = executable;
                if (localExecutable == null) {
                    localExecutable = createExecutable();
                    executable = localExecutable;
                }
            }
        }
        return localExecutable;
    }

    protected Optional<SWIPrologExecutable> createExecutable() {
        var command = "swipl";
        var pb = new ProcessBuilder(Arrays.asList(command, "--version"));
        pb.redirectErrorStream(true);
        pb.redirectOutput(Redirect.DISCARD);
        try {
            var process = pb.start();
            process.waitFor();
            if (process.exitValue() != 0) {
                return Optional.empty();
            }
        } catch (IOException | InterruptedException e) {
            return Optional.empty();
        }

        return Optional.of(new SWIPrologExecutable() {
            @Override
            public String getPath() {
                return "swipl";
            }
        });
    }

}
