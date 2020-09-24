package org.prolog4j.problog.util;

import java.util.function.Supplier;

import org.palladiosimulator.supporting.prolog.services.PrologGrammarAccess;

import com.google.inject.Inject;

public class PrologGrammarAccessProvider implements Supplier<PrologGrammarAccess> {
    
    @Inject
    private PrologGrammarAccess grammarAccess;

	@Override
	public PrologGrammarAccess get() {
		return grammarAccess;
	}

}
