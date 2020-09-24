package org.prolog4j.swicli.impl;

import java.io.StringReader;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.xtext.parser.IParseResult;
import org.palladiosimulator.supporting.prolog.api.PrologAPI;
import org.palladiosimulator.supporting.prolog.model.prolog.CompoundTerm;
import org.prolog4j.AbstractProver;
import org.prolog4j.ConversionPolicy;
import org.prolog4j.Query;
import org.prolog4j.swicli.SWIPrologExecutable;

public class SWIPrologCLIProver extends AbstractProver {

	private static final long serialVersionUID = -48719179409278734L;

	private final Set<String> predicateProperties = new LinkedHashSet<>();
	private final StringBuilder theory = new StringBuilder();
	private final PrologAPI prologApi;
	private final SWIPrologExecutable executable;
	
	public SWIPrologCLIProver(ConversionPolicy conversionPolicy, PrologAPI prologApi, SWIPrologExecutable executable) {
		super(conversionPolicy);
		this.prologApi = prologApi;
		this.executable = executable;
	}

	@Override
	public Query query(String goal) {
	    String theoryPrefix = predicateProperties.stream().collect(Collectors.joining(System.lineSeparator())) + System.lineSeparator();
	    return new SWIPrologCLIQuery(getConversionPolicy(), prologApi, executable, theoryPrefix + theory.toString(), goal);
	}

    @Override
    public void assertz(String fact, Object... args) {
        QueryReplacer queryReplacer = new QueryReplacer(getConversionPolicy(), prologApi, fact);
        String newFact = queryReplacer.getQueryString(args).trim();
        if (newFact.endsWith(".")) {
            newFact = newFact.substring(0, newFact.length() - 1);
        }
        IParseResult parsedFact = prologApi.getParser().parse(prologApi.getParser().getGrammarAccess().getCompoundTermRule(), new StringReader(newFact));
        CompoundTerm ct = (CompoundTerm) parsedFact.getRootASTElement();
        String factName = ct.getValue();
        int factArity = ct.getArguments().size();
        String dynamicStatement = String.format(":- dynamic(%s/%d).", factName, factArity);
        predicateProperties.add(dynamicStatement);
        String assertzGoal = String.format(":- assertz(%s).", newFact);
        addTheory(assertzGoal);
    }

    @Override
    public void retract(String fact) {
        var newFact = fact.trim();
        if (newFact.endsWith(".")) {
            newFact = newFact.substring(0, newFact.length() - 1);
        }
        String retractClause = String.format(":- retract(%s).", newFact);
        addTheory(retractClause);
    }

    @Override
	public void loadLibrary(String library) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addTheory(String theory) {
		this.theory.append(theory);
		this.theory.append(System.lineSeparator());
	}

	@Override
	public void addTheory(String... theory) {
		for (var theoryPart : theory) {
			addTheory(theoryPart);
		}
	}

}
