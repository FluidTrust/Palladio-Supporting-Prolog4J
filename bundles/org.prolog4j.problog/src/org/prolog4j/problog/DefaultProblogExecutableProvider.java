package org.prolog4j.problog;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;

//TODO: Test auf installiertes Prolog (siehe JProblog... oder einfach JProblog benutzen?!)

@Component(property = ProblogExecutableProvider.PRIORITY_PROPERTY + " = "
        + ProblogExecutableProvider.PRIORITY_LOWEST)
public class DefaultProblogExecutableProvider implements ProblogExecutableProvider {

	@Override
	public Optional<ProblogExecutable> getExecutable() {
		return Optional.of(new ProblogExecutable() {

			@Override
			public String execute(String problogProgram) {
				return null;
			}
        });
	}

}
